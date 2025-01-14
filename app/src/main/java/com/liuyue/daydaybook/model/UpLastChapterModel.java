package com.liuyue.daydaybook.model;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;
import com.liuyue.daydaybook.DbHelper;
import com.liuyue.daydaybook.MApplication;
import com.liuyue.daydaybook.bean.BookChapterBean;
import com.liuyue.daydaybook.bean.BookShelfBean;
import com.liuyue.daydaybook.bean.BookSourceBean;
import com.liuyue.daydaybook.bean.SearchBookBean;
import com.liuyue.daydaybook.constant.RxBusTag;
import com.liuyue.daydaybook.dao.SearchBookBeanDao;
import com.liuyue.daydaybook.help.BookshelfHelp;
import com.liuyue.daydaybook.utils.RxUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 更新换源列表里最新章节
 */
public class UpLastChapterModel {
    public static UpLastChapterModel model;
    private CompositeDisposable compositeDisposable;
    private ExecutorService executorService;
    private Scheduler scheduler;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<SearchBookBean> searchBookBeanList;
    private int upIndex;

    public static UpLastChapterModel getInstance() {
        if (model == null) {
            model = new UpLastChapterModel();
        }
        return model;
    }

    private UpLastChapterModel() {
        executorService = Executors.newFixedThreadPool(5);
        scheduler = Schedulers.from(executorService);
        compositeDisposable = new CompositeDisposable();
        searchBookBeanList = new ArrayList<>();
    }

    public void startUpdate() {
        if (!MApplication.getConfigPreferences().getBoolean("upChangeSourceLastChapter", false))
            return;
        if (compositeDisposable.size() > 0) return;
        List<SearchBookBean> beanList = new ArrayList<>();
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            List<BookShelfBean> bookShelfBeans = BookshelfHelp.getAllBook();
            for (BookShelfBean bookShelfBean : bookShelfBeans) {
                if (!Objects.equals(bookShelfBean.getTag(), BookShelfBean.LOCAL_TAG)) {
                    e.onNext(bookShelfBean);
                }
            }
            e.onComplete();
        }).flatMap(this::findSearchBookBean)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<SearchBookBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(SearchBookBean searchBookBean) {
                        beanList.add(searchBookBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        startUpdate(beanList);
                    }
                });
    }

    public synchronized void startUpdate(List<SearchBookBean> beanList) {
        compositeDisposable.dispose();
        executorService.shutdown();
        executorService = Executors.newFixedThreadPool(5);
        scheduler = Schedulers.from(executorService);
        compositeDisposable = new CompositeDisposable();
        this.searchBookBeanList = beanList;
        upIndex = -1;
        for (int i = 0; i < 5; i++) {
            doUpdate();
        }
    }

    private synchronized void doUpdate() {
        upIndex++;
        if (upIndex < searchBookBeanList.size()) {
            toBookshelf(searchBookBeanList.get(upIndex))
                    .flatMap(this::getChapterList)
                    .flatMap(this::saveSearchBookBean)
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<SearchBookBean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                            handler.postDelayed(() -> {
                                if (!d.isDisposed()) {
                                    d.dispose();
                                    doUpdate();
                                }
                            }, 20 * 1000);
                        }

                        @Override
                        public void onNext(SearchBookBean searchBookBean) {
                            RxBus.get().post(RxBusTag.UP_SEARCH_BOOK, searchBookBean);
                            doUpdate();
                        }

                        @Override
                        public void onError(Throwable e) {
                            doUpdate();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    private void stopUp() {
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        compositeDisposable = new CompositeDisposable();
    }

    public static void destroy() {
        if (model != null) {
            model.stopUp();
            model.executorService.shutdownNow();
            model = null;
        }
    }

    private Observable<SearchBookBean> findSearchBookBean(BookShelfBean bookShelf) {
        return Observable.create(e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.Name.eq(bookShelf.getBookInfoBean().getName())).list();
            for (SearchBookBean searchBookBean : searchBookBeans) {
                BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(searchBookBean.getTag());
                if (sourceBean == null) {
                    DbHelper.getDaoSession().getSearchBookBeanDao().delete(searchBookBean);
                } else if (System.currentTimeMillis() - searchBookBean.getUpTime() > 1000 * 60 * 60
                        && sourceBean.getEnable()) {
                    e.onNext(searchBookBean);
                }
            }
            e.onComplete();
        });
    }

    private Observable<BookShelfBean> toBookshelf(SearchBookBean searchBookBean) {
        return Observable.create(e -> {
            BookShelfBean bookShelfBean = BookshelfHelp.getBookFromSearchBook(searchBookBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    private Observable<List<BookChapterBean>> getChapterList(BookShelfBean bookShelfBean) {
        if (TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getChapterUrl())) {
            return WebBookModel.getInstance().getBookInfo(bookShelfBean)
                    .flatMap(bookShelf -> WebBookModel.getInstance().getChapterList(bookShelf));
        } else {
            return WebBookModel.getInstance().getChapterList(bookShelfBean);
        }
    }

    private Observable<SearchBookBean> saveSearchBookBean(List<BookChapterBean> chapterBeanList) {
        return Observable.create(e -> {
            BookChapterBean chapterBean = chapterBeanList.get(chapterBeanList.size() - 1);
            SearchBookBean searchBookBean = DbHelper.getDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.NoteUrl.eq(chapterBean.getNoteUrl()))
                    .unique();
            if (searchBookBean != null) {
                searchBookBean.setLastChapter(chapterBean.getDurChapterName());
                searchBookBean.setAddTime(System.currentTimeMillis());
                DbHelper.getDaoSession().getSearchBookBeanDao().insertOrReplace(searchBookBean);
                e.onNext(searchBookBean);
            }
            e.onComplete();
        });
    }
}
