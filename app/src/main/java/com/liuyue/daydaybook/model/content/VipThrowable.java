package com.liuyue.daydaybook.model.content;

import com.liuyue.daydaybook.MApplication;
import com.liuyue.daydaybook.R;

public class VipThrowable extends Throwable {

    private final static String tag = "VIP_THROWABLE";

    VipThrowable() {
        super(MApplication.getInstance().getString(R.string.donate_s));
    }
}
