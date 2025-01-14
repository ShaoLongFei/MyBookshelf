package com.liuyue.daydaybook.view.activity;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;

import com.liuyue.basemvplib.impl.IPresenter;
import com.liuyue.daydaybook.R;
import com.liuyue.daydaybook.base.MBaseActivity;
import com.liuyue.daydaybook.databinding.ActivitySettingsBinding;
import com.liuyue.daydaybook.utils.theme.ThemeStore;
import com.liuyue.daydaybook.view.fragment.ThemeSettingsFragment;

/**
 * Created by GKF on 2017/12/16.
 * 设置
 */

public class ThemeSettingActivity extends MBaseActivity<IPresenter> {

    private ActivitySettingsBinding binding;

    public static void startThis(Context context) {
        context.startActivity(new Intent(context, ThemeSettingActivity.class));
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.setSupportActionBar(binding.toolbar);
        setupActionBar();
        ThemeSettingsFragment settingsFragment = new ThemeSettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.settingsFrameLayout, settingsFragment)
                .commit();

    }

    @Override
    protected void initData() {

    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.theme_setting);
        }
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void initImmersionBar() {
        super.initImmersionBar();
    }
}
