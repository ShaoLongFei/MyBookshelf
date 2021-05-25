package com.liuyue.daydaybook.view.adapter;

import com.liuyue.daydaybook.bean.BookShelfBean;
import com.liuyue.daydaybook.help.ItemTouchCallback;
import com.liuyue.daydaybook.view.adapter.base.OnItemClickListenerTwo;

import java.util.HashSet;
import java.util.List;

public interface BookShelfAdapter {

    void setArrange(boolean isArrange);

    void selectAll();

    ItemTouchCallback.OnItemTouchCallbackListener getItemTouchCallbackListener();

    List<BookShelfBean> getBooks();

    void replaceAll(List<BookShelfBean> newDataS, String bookshelfPx);

    void refreshBook(String noteUrl);

    void setItemClickListener(OnItemClickListenerTwo itemClickListener);

    HashSet<String> getSelected();

}
