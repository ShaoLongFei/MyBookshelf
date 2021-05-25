package com.liuyue.daydaybook.model.analyzeRule;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.liuyue.daydaybook.DbHelper;
import com.liuyue.daydaybook.MApplication;
import com.liuyue.daydaybook.R;
import com.liuyue.daydaybook.bean.BookSourceBean;
import com.liuyue.daydaybook.bean.CookieBean;
import com.liuyue.daydaybook.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static android.text.TextUtils.isEmpty;
import static com.liuyue.daydaybook.constant.AppConstant.DEFAULT_USER_AGENT;
import static com.liuyue.daydaybook.constant.AppConstant.MAP_STRING;

/**
 * Created by GKF on 2018/3/2.
 * 解析Headers
 */

public class AnalyzeHeaders {
    private static SharedPreferences preferences = MApplication.getConfigPreferences();

    public static Map<String, String> getMap(BookSourceBean bookSourceBean) {
        Map<String, String> headerMap = new HashMap<>();
        if (bookSourceBean != null) {
            String headers = bookSourceBean.getHttpUserAgent();
            if (!isEmpty(headers)) {
                if (StringUtils.isJsonObject(headers)) {
                    Map<String, String> map = new Gson().fromJson(headers, MAP_STRING);
                    headerMap.putAll(map);
                } else {
                    headerMap.put("User-Agent", headers);
                }
            } else {
                headerMap.put("User-Agent", getDefaultUserAgent());
            }
            CookieBean cookie = DbHelper.getDaoSession().getCookieBeanDao().load(bookSourceBean.getBookSourceUrl());
            if (cookie != null) {
                headerMap.put("Cookie", cookie.getCookie());
            }
        } else {
            headerMap.put("User-Agent", getDefaultUserAgent());
        }
        return headerMap;
    }

    private static String getDefaultUserAgent() {
        return preferences.getString(MApplication.getInstance().getString(R.string.pk_user_agent), DEFAULT_USER_AGENT);
    }
}
