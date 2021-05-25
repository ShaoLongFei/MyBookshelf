package com.liuyue.daydaybook.presenter.contract;

import android.app.Activity;
import android.content.Intent;

import com.liuyue.basemvplib.impl.IPresenter;
import com.liuyue.basemvplib.impl.IView;
import com.liuyue.daydaybook.bean.BookChapterBean;
import com.liuyue.daydaybook.bean.BookShelfBean;
import com.liuyue.daydaybook.bean.BookSourceBean;
import com.liuyue.daydaybook.bean.BookmarkBean;
import com.liuyue.daydaybook.bean.SearchBookBean;
import com.liuyue.daydaybook.presenter.ReadBookPresenter;
import com.liuyue.daydaybook.service.ReadAloudService;

import java.util.List;

public interface ReadBookContract {
    interface View extends IView {

        String getNoteUrl();

        Boolean getAdd();

        void setAdd(Boolean isAdd);

        void changeSourceFinish(BookShelfBean book);

        void startLoadingBook();

        void upMenu();

        void openBookFromOther();

        void showBookmark(BookmarkBean bookmarkBean);

        void skipToChapter(int chapterIndex, int pageIndex);

        void onMediaButton(String cmd);

        void upAloudState(ReadAloudService.Status state);

        void upAloudTimer(String timer);

        void readAloudStart(int start);

        void readAloudLength(int readAloudLength);

        void refresh(boolean recreate);

        void finish();

        void recreate();

        void upAudioSize(int audioSize);

        void upAudioDur(int audioDur);
    }

    interface Presenter extends IPresenter {

        void loadBook(Intent intent);

        BookShelfBean getBookShelf();

        List<BookChapterBean> getChapterList();

        BookChapterBean getDurChapter();

        void setChapterList(List<BookChapterBean> chapterList);

        void saveBook();

        void saveProgress();

        void addToShelf(final ReadBookPresenter.OnAddListener Listener);

        void removeFromShelf();

        void initData(Activity activity);

        void openBookFromOther(Activity activity);

        void addDownload(int start, int end);

        void changeBookSource(SearchBookBean searchBookBean);

        void autoChangeSource();

        void saveBookmark(BookmarkBean bookmarkBean);

        void delBookmark(BookmarkBean bookmarkBean);

        void disableDurBookSource();

        BookSourceBean getBookSource();
    }
}
