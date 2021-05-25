package com.liuyue.daydaybook.presenter.contract;

import com.liuyue.basemvplib.impl.IPresenter;
import com.liuyue.basemvplib.impl.IView;

import java.io.File;
import java.util.List;

public interface ImportBookContract {

    interface Presenter extends IPresenter {

        void importBooks(List<File> books);

    }

    interface View extends IView {

        /**
         * 添加成功
         */
        void addSuccess();

        /**
         * 添加失败
         */
        void addError(String msg);
    }
}
