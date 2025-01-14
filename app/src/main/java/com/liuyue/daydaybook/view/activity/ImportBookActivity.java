package com.liuyue.daydaybook.view.activity;

import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.liuyue.daydaybook.R;
import com.liuyue.daydaybook.base.BaseTabActivity;
import com.liuyue.daydaybook.databinding.ActivityImportBookBinding;
import com.liuyue.daydaybook.presenter.ImportBookPresenter;
import com.liuyue.daydaybook.presenter.contract.ImportBookContract;
import com.liuyue.daydaybook.utils.theme.ThemeStore;
import com.liuyue.daydaybook.view.fragment.BaseFileFragment;
import com.liuyue.daydaybook.view.fragment.FileCategoryFragment;
import com.liuyue.daydaybook.view.fragment.LocalBookFragment;

import java.io.File;
import java.util.Arrays;
import java.util.List;


/**
 * 导入本地书籍
 */
public class ImportBookActivity extends BaseTabActivity<ImportBookContract.Presenter> implements ImportBookContract.View {
    private static final String TAG = "ImportBookActivity";

    private ActivityImportBookBinding binding;
    private LocalBookFragment mLocalFragment;
    private FileCategoryFragment mCategoryFragment;
    private BaseFileFragment mCurFragment;

    private BaseFileFragment.OnFileCheckedListener mListener = new BaseFileFragment.OnFileCheckedListener() {
        @Override
        public void onItemCheckedChange(boolean isChecked) {
            changeMenuStatus();
        }

        @Override
        public void onCategoryChanged() {
            //状态归零
            mCurFragment.setCheckedAll(false);
            //改变菜单
            changeMenuStatus();
            //改变是否能够全选
            changeCheckedAllStatus();
        }
    };


    @Override
    protected ImportBookContract.Presenter initInjector() {
        return new ImportBookPresenter();
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivityImportBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        setupActionBar();

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void bindView() {
        super.bindView();
        mTlIndicator.setSelectedTabIndicatorColor(ThemeStore.accentColor(this));
        mTlIndicator.setTabTextColors(getResources().getColor(R.color.tv_text_default), ThemeStore.accentColor(this));
    }

    @Override
    protected List<Fragment> createTabFragments() {
        mCategoryFragment = new FileCategoryFragment();
        mLocalFragment = new LocalBookFragment();
        return Arrays.asList(mCategoryFragment, mLocalFragment);
    }

    @Override
    protected List<String> createTabTitles() {
        return Arrays.asList(getString(R.string.files_tree), getString(R.string.intelligent_import));
    }

    @Override
    protected void bindEvent() {
        binding.fileSystemCbSelectedAll.setOnClickListener(
                (view) -> {
                    //设置全选状态
                    boolean isChecked = binding.fileSystemCbSelectedAll.isChecked();
                    mCurFragment.setCheckedAll(isChecked);
                    //改变菜单状态
                    changeMenuStatus();
                }
        );

        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurFragment = (BaseFileFragment) mFragmentList.get(position);
                //改变菜单状态
                changeMenuStatus();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        binding.fileSystemBtnAddBook.setOnClickListener(
                (v) -> {
                    //获取选中的文件
                    List<File> files = mCurFragment.getCheckedFiles();
                    //转换成CollBook,并存储
                    mPresenter.importBooks(files);
                }
        );

        binding.fileSystemBtnDelete.setOnClickListener(
                (v) -> {
                    //弹出，确定删除文件吗。
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.del_file))
                            .setMessage(getString(R.string.sure_del_file))
                            .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
                                //删除选中的文件
                                mCurFragment.deleteCheckedFiles();
                                //提示删除文件成功
                                toast(R.string.del_file_success);
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel), null)
                            .show();
                }
        );
        mCategoryFragment.setOnFileCheckedListener(mListener);
        mLocalFragment.setOnFileCheckedListener(mListener);
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        mCurFragment = (BaseFileFragment) mFragmentList.get(0);
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.book_local);
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

    /**
     * 改变底部选择栏的状态
     */
    private void changeMenuStatus() {

        //点击、删除状态的设置
        if (mCurFragment.getCheckedCount() == 0) {
            binding.fileSystemBtnAddBook.setText(getString(R.string.nb_file_add_shelf));
            //设置某些按钮的是否可点击
            setMenuClickable(false);

            if (binding.fileSystemCbSelectedAll.isChecked()) {
                mCurFragment.setChecked(false);
                binding.fileSystemCbSelectedAll.setChecked(mCurFragment.isCheckedAll());
            }

        } else {
            binding.fileSystemBtnAddBook.setText(getString(R.string.nb_file_add_shelves, mCurFragment.getCheckedCount()));
            setMenuClickable(true);

            //全选状态的设置

            //如果选中的全部的数据，则判断为全选
            if (mCurFragment.getCheckedCount() == mCurFragment.getCheckableCount()) {
                //设置为全选
                mCurFragment.setChecked(true);
                binding.fileSystemCbSelectedAll.setChecked(mCurFragment.isCheckedAll());
            }
            //如果曾今是全选则替换
            else if (mCurFragment.isCheckedAll()) {
                mCurFragment.setChecked(false);
                binding.fileSystemCbSelectedAll.setChecked(mCurFragment.isCheckedAll());
            }
        }

        //重置全选的文字
        if (mCurFragment.isCheckedAll()) {
            binding.fileSystemCbSelectedAll.setText(R.string.cancel);
        } else {
            binding.fileSystemCbSelectedAll.setText(getString(R.string.select_all));
        }

    }

    private void setMenuClickable(boolean isClickable) {

        //设置是否可删除
        binding.fileSystemBtnDelete.setEnabled(isClickable);
        binding.fileSystemBtnDelete.setClickable(isClickable);

        //设置是否可添加书籍
        binding.fileSystemBtnAddBook.setEnabled(isClickable);
        binding.fileSystemBtnAddBook.setClickable(isClickable);
    }

    /**
     * 改变全选按钮的状态
     */
    private void changeCheckedAllStatus() {
        //获取可选择的文件数量
        int count = mCurFragment.getCheckableCount();

        //设置是否能够全选
        if (count > 0) {
            binding.fileSystemCbSelectedAll.setClickable(true);
            binding.fileSystemCbSelectedAll.setEnabled(true);
        } else {
            binding.fileSystemCbSelectedAll.setClickable(false);
            binding.fileSystemCbSelectedAll.setEnabled(false);
        }
    }

    @Override
    public void addSuccess() {
        //设置HashMap为false
        mCurFragment.setCheckedAll(false);
        //改变菜单状态
        changeMenuStatus();
        //改变是否可以全选
        changeCheckedAllStatus();
    }

    @Override
    public void addError(String msg) {

    }
}
