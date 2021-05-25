package com.liuyue.daydaybook.presenter.contract;

import com.liuyue.basemvplib.impl.IPresenter;
import com.liuyue.basemvplib.impl.IView;
import com.liuyue.daydaybook.widget.recycler.expandable.bean.RecyclerViewData;

import java.util.List;

public interface FindBookContract {
    interface Presenter extends IPresenter {

        void initData();

    }

    interface View extends IView {

        void upData(List<RecyclerViewData> group);

    }
}
