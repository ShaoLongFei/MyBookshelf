package com.liuyue.daydaybook.widget.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatRadioButton;

import com.liuyue.daydaybook.utils.ScreenUtils;
import com.liuyue.daydaybook.utils.Selector;
import com.liuyue.daydaybook.utils.theme.ThemeStore;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ATERadioNoButton extends AppCompatRadioButton {

    public ATERadioNoButton(Context context) {
        super(context);
        init(context, null);
    }

    public ATERadioNoButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ATERadioNoButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setBackground(Selector.shapeBuild()
                .setCornerRadius(ScreenUtils.dpToPx(3))
                .setStrokeWidth(ScreenUtils.dpToPx(1))
                .setCheckedBgColor(ThemeStore.accentColor(context))
                .setCheckedStrokeColor(ThemeStore.accentColor(context))
                .setDefaultStrokeColor(Color.WHITE)
                .create());
    }
}
