package com.liuyue.daydaybook.view.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.liuyue.basemvplib.impl.IPresenter;
import com.liuyue.daydaybook.R;
import com.liuyue.daydaybook.base.MBaseActivity;
import com.liuyue.daydaybook.bean.DownloadBookBean;
import com.liuyue.daydaybook.databinding.ActivityRecyclerVewBinding;
import com.liuyue.daydaybook.service.DownloadService;
import com.liuyue.daydaybook.utils.theme.ThemeStore;
import com.liuyue.daydaybook.view.adapter.DownloadAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.liuyue.daydaybook.service.DownloadService.addDownloadAction;
import static com.liuyue.daydaybook.service.DownloadService.finishDownloadAction;
import static com.liuyue.daydaybook.service.DownloadService.obtainDownloadListAction;
import static com.liuyue.daydaybook.service.DownloadService.progressDownloadAction;
import static com.liuyue.daydaybook.service.DownloadService.removeDownloadAction;

public class DownloadActivity extends MBaseActivity<IPresenter> {

    private ActivityRecyclerVewBinding binding;
    private DownloadAdapter adapter;
    private DownloadReceiver receiver;

    public static void startThis(Activity activity) {
        Intent intent = new Intent(activity, DownloadActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivityRecyclerVewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.setSupportActionBar(binding.toolbar);
        setupActionBar();
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        receiver = new DownloadReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(addDownloadAction);
        filter.addAction(removeDownloadAction);
        filter.addAction(progressDownloadAction);
        filter.addAction(obtainDownloadListAction);
        filter.addAction(finishDownloadAction);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void bindView() {
        initRecyclerView();
    }

    private void initRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DownloadAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setItemAnimator(null);

        DownloadService.obtainDownloadList(this);
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.download_offline);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_download, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_cancel:
                DownloadService.cancelDownload(this);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class DownloadReceiver extends BroadcastReceiver {

        WeakReference<DownloadActivity> ref;

        public DownloadReceiver(DownloadActivity activity) {
            this.ref = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadAdapter adapter = ref.get().adapter;
            if (adapter == null || intent == null) {
                return;
            }
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case addDownloadAction:
                        DownloadBookBean downloadBook = intent.getParcelableExtra("downloadBook");
                        adapter.addData(downloadBook);
                        break;
                    case removeDownloadAction:
                        downloadBook = intent.getParcelableExtra("downloadBook");
                        adapter.removeData(downloadBook);
                        break;
                    case progressDownloadAction:
                        downloadBook = intent.getParcelableExtra("downloadBook");
                        adapter.upData(downloadBook);
                        break;
                    case finishDownloadAction:
                        adapter.upDataS(null);
                        break;
                    case obtainDownloadListAction:
                        ArrayList<DownloadBookBean> downloadBooks = intent.getParcelableArrayListExtra("downloadBooks");
                        adapter.upDataS(downloadBooks);
                        break;

                }
            }
        }
    }
}
