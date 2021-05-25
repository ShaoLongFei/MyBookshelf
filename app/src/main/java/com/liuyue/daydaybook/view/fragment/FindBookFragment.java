package com.liuyue.daydaybook.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.liuyue.daydaybook.MApplication;
import com.liuyue.daydaybook.R;
import com.liuyue.daydaybook.base.MBaseFragment;
import com.liuyue.daydaybook.base.observer.MySingleObserver;
import com.liuyue.daydaybook.bean.BookSourceBean;
import com.liuyue.daydaybook.bean.FindKindBean;
import com.liuyue.daydaybook.bean.FindKindGroupBean;
import com.liuyue.daydaybook.databinding.FragmentBookFindBinding;
import com.liuyue.daydaybook.model.BookSourceManager;
import com.liuyue.daydaybook.presenter.FindBookPresenter;
import com.liuyue.daydaybook.presenter.contract.FindBookContract;
import com.liuyue.daydaybook.utils.ACache;
import com.liuyue.daydaybook.utils.theme.ThemeStore;
import com.liuyue.daydaybook.view.activity.ChoiceBookActivity;
import com.liuyue.daydaybook.view.activity.SourceEditActivity;
import com.liuyue.daydaybook.view.adapter.FindKindAdapter;
import com.liuyue.daydaybook.view.adapter.FindLeftAdapter;
import com.liuyue.daydaybook.view.adapter.FindRightAdapter;
import com.liuyue.daydaybook.widget.recycler.expandable.OnRecyclerViewListener;
import com.liuyue.daydaybook.widget.recycler.expandable.bean.RecyclerViewData;
import com.liuyue.daydaybook.widget.recycler.sectioned.GridSpacingItemDecoration;
import com.liuyue.daydaybook.widget.recycler.sectioned.SectionedSpanSizeLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class FindBookFragment extends MBaseFragment<FindBookContract.Presenter> implements FindBookContract.View,
        OnRecyclerViewListener.OnItemClickListener,
        OnRecyclerViewListener.OnItemLongClickListener {


    private FragmentBookFindBinding binding;
    private FindLeftAdapter findLeftAdapter;
    private FindRightAdapter findRightAdapter;
    private FindKindAdapter findKindAdapter;
    private LinearLayoutManager leftLayoutManager;
    private RecyclerView.LayoutManager rightLayoutManager;
    private List<RecyclerViewData> data = new ArrayList<>();

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentBookFindBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected FindBookContract.Presenter initInjector() {
        return new FindBookPresenter();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void bindView() {
        super.bindView();
        binding.rvFindRight.addItemDecoration(new GridSpacingItemDecoration(10));
        binding.refreshLayout.setColorSchemeColors(ThemeStore.accentColor(MApplication.getInstance()));
        binding.refreshLayout.setOnRefreshListener(() -> {
            refreshData();
            binding.refreshLayout.setRefreshing(false);
        });
        leftLayoutManager = new LinearLayoutManager(getContext());
        initRecyclerView();
    }

    /**
     * 首次逻辑操作
     */
    @Override
    protected void firstRequest() {
        super.firstRequest();
        refreshData();
    }

    public void refreshData() {
        if (mPresenter != null) {
            mPresenter.initData();
        }
    }

    @Override
    public void upData(List<RecyclerViewData> group) {
        this.data = group;
        upStyle();
        upUI();
    }

    public void upStyle() {
        if (binding.emptyView.rlEmptyView == null) return;
        initRecyclerView();
        if (isFlexBox()) {
            findRightAdapter.setData(data);
            findLeftAdapter.setData(data);
        } else {
            findKindAdapter.setAllDatas(data);
        }
        upUI();
    }

    public void upUI() {
        if (binding.emptyView.rlEmptyView == null) return;
        if (data.size() == 0) {
            binding.emptyView.tvEmpty.setText(R.string.no_find);
            binding.emptyView.rlEmptyView.setVisibility(View.VISIBLE);
        } else {
            binding.emptyView.rlEmptyView.setVisibility(View.GONE);
        }
        if (isFlexBox()) {
            binding.emptyView.rlEmptyView.setVisibility(View.GONE);
            if (data.size() <= 1 | !showLeftView()) {
                binding.rvFindLeft.setVisibility(View.GONE);
                binding.vwDivider.setVisibility(View.GONE);
            } else {
                binding.rvFindLeft.setVisibility(View.VISIBLE);
                binding.vwDivider.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean isFlexBox() {
        return preferences.getBoolean("findTypeIsFlexBox", true);
    }

    private boolean showLeftView() {
        return preferences.getBoolean("showFindLeftView", true);
    }

    private void initRecyclerView() {
        if (binding.rvFindRight == null) return;
        if (isFlexBox()) {
            findKindAdapter = null;
            findLeftAdapter = new FindLeftAdapter(getActivity(), pos -> {
                int counts = 0;
                for (int i = 0; i < pos; i++) {
                    //position 为点击的position
                    counts += findRightAdapter.getData().get(i).getChildList().size();
                }
                ((ScrollLinearLayoutManger) rightLayoutManager).scrollToPositionWithOffset(counts + pos, 0);
            });
            binding.rvFindLeft.setLayoutManager(leftLayoutManager);
            binding.rvFindLeft.setAdapter(findLeftAdapter);

            findRightAdapter = new FindRightAdapter(Objects.requireNonNull(getActivity()), this);
            //设置header
            rightLayoutManager = new ScrollLinearLayoutManger(getActivity(), 3);
            ((ScrollLinearLayoutManger) rightLayoutManager).setSpanSizeLookup(new SectionedSpanSizeLookup(findRightAdapter, (ScrollLinearLayoutManger) rightLayoutManager));
            binding.rvFindRight.setLayoutManager(rightLayoutManager);
            binding.rvFindRight.setLayoutManager(rightLayoutManager);
            binding.rvFindRight.setItemViewCacheSize(10);
            binding.rvFindRight.setItemAnimator(null);
            binding.rvFindRight.setHasFixedSize(true);
            binding.rvFindRight.setAdapter(findRightAdapter);
        } else {
            rightLayoutManager = new LinearLayoutManager(getContext());
            binding.rvFindLeft.setVisibility(View.GONE);
            binding.vwDivider.setVisibility(View.GONE);
            findLeftAdapter = null;
            findRightAdapter = null;
            findKindAdapter = new FindKindAdapter(getContext(), new ArrayList<>());
            findKindAdapter.setOnItemClickListener(this);
            findKindAdapter.setOnItemLongClickListener(this);
            findKindAdapter.setCanExpandAll(false);
            binding.rvFindRight.setLayoutManager(rightLayoutManager);
            binding.rvFindRight.setAdapter(findKindAdapter);
        }
    }

    @Override
    public void onGroupItemClick(int position, int groupPosition, View view) {

    }

    @Override
    public void onChildItemClick(int position, int groupPosition, int childPosition, View view) {
        FindKindBean kindBean = (FindKindBean) findKindAdapter.getAllDatas().get(groupPosition).getChild(childPosition);

        Intent intent = new Intent(getContext(), ChoiceBookActivity.class);
        intent.putExtra("url", kindBean.getKindUrl());
        intent.putExtra("title", kindBean.getKindName());
        intent.putExtra("tag", kindBean.getTag());
        startActivityByAnim(intent, view, "sharedView", android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onGroupItemLongClick(int position, int groupPosition, View view) {
        if (getActivity() == null) return;
        FindKindGroupBean groupBean;
        if (isFlexBox()) {
            groupBean = (FindKindGroupBean) findRightAdapter.getData().get(groupPosition).getGroupData();
        } else {
            groupBean = (FindKindGroupBean) findKindAdapter.getAllDatas().get(groupPosition).getGroupData();
        }
        BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(groupBean.getGroupTag());
        if (sourceBean == null) {
            return;
        }
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenu().add(Menu.NONE, R.id.menu_edit, Menu.NONE, R.string.edit);
        popupMenu.getMenu().add(Menu.NONE, R.id.menu_top, Menu.NONE, R.string.to_top);
        popupMenu.getMenu().add(Menu.NONE, R.id.menu_del, Menu.NONE, R.string.delete);
        popupMenu.getMenu().add(Menu.NONE, R.id.menu_clear, Menu.NONE, R.string.clear_cache);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_edit:
                    SourceEditActivity.startThis(this, sourceBean);
                    break;
                case R.id.menu_top:
                    BookSourceManager.toTop(sourceBean)
                            .subscribe(new MySingleObserver<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {
                                    refreshData();
                                }
                            });
                    break;
                case R.id.menu_del:
                    BookSourceManager.removeBookSource(sourceBean);
                    refreshData();
                    break;
                case R.id.menu_clear:
                    ACache.get(getActivity(), "findCache").remove(sourceBean.getBookSourceUrl());
                    break;
            }
            return true;
        });
        popupMenu.show();

    }

    @Override
    public void onChildItemLongClick(int position, int groupPosition, int childPosition, View view) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SourceEditActivity.EDIT_SOURCE) {
                refreshData();
            }
        }
    }

    @SuppressWarnings("unused")
    public static class ScrollLinearLayoutManger extends GridLayoutManager {

        public ScrollLinearLayoutManger(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public ScrollLinearLayoutManger(Context context, int spanCount) {
            super(context, spanCount);
        }

        public ScrollLinearLayoutManger(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }

        private class CenterSmoothScroller extends LinearSmoothScroller {

            CenterSmoothScroller(Context context) {
                super(context);
            }

            @Nullable
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return ScrollLinearLayoutManger.this.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
            }

            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return 0.2f;
            }

            @Override
            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;
            }
        }

    }
}
