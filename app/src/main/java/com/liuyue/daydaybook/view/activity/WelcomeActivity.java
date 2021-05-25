//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.liuyue.daydaybook.view.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.AsyncTask;

import com.liuyue.basemvplib.impl.IPresenter;
import com.liuyue.daydaybook.DbHelper;
import com.liuyue.daydaybook.R;
import com.liuyue.daydaybook.base.MBaseActivity;
import com.liuyue.daydaybook.databinding.ActivityWelcomeBinding;
import com.liuyue.daydaybook.presenter.ReadBookPresenter;
import com.liuyue.daydaybook.utils.theme.ThemeStore;

public class WelcomeActivity extends MBaseActivity<IPresenter> {

    private ActivityWelcomeBinding binding;

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AsyncTask.execute(DbHelper::getDaoSession);
        binding.tvGzh.setTextColor(ThemeStore.accentColor(this));
        binding.ivBg.setColorFilter(ThemeStore.accentColor(this));
        ValueAnimator welAnimator = ValueAnimator.ofFloat(1f, 0f).setDuration(800);
        welAnimator.setStartDelay(500);
        welAnimator.addUpdateListener(animation -> {
            float alpha = (Float) animation.getAnimatedValue();
            binding.ivBg.setAlpha(alpha);
        });
        welAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (preferences.getBoolean(getString(R.string.pk_default_read), false)) {
                    startReadActivity();
                } else {
                    startBookshelfActivity();
                }
                finish();
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        welAnimator.start();
    }

    private void startBookshelfActivity() {
        startActivityByAnim(new Intent(this, MainActivity.class), android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void startReadActivity() {
        Intent intent = new Intent(this, ReadBookActivity.class);
        intent.putExtra("openFrom", ReadBookPresenter.OPEN_FROM_APP);
        startActivity(intent);
    }

    @Override
    protected void initData() {

    }

}