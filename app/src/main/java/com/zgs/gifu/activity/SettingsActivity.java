package com.zgs.gifu.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.zgs.gifu.R;
import com.zgs.gifu.adapter.MultiItemAdapter;
import com.zgs.gifu.constant.AppConstants;
import com.zgs.gifu.entity.EmptyItem;
import com.zgs.gifu.entity.MaxFrameItem;
import com.zgs.gifu.entity.MultipleItem;
import com.zgs.gifu.entity.SavePathItem;
import com.zgs.gifu.entity.UpdateItem;
import com.zgs.gifu.fragment.PathSelectFragment;
import com.zgs.gifu.utils.RecyclerViewDivider;
import com.zgs.gifu.utils.SettingUtil;
import com.zgs.gifu.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private PathSelectFragment mPathSelectFragment;

    private RecyclerView mRecyclerView;
    private MultiItemAdapter mAdapter;
    private List<MultipleItem> dataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.page_settings);
        initView();
        loadData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_settings);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.setting_rv);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(SettingsActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new RecyclerViewDivider(SettingsActivity.this));
    }

    public void loadData() {
        SharedPreferences settings = SettingsActivity.this.getSharedPreferences(AppConstants.PREFS_NAME, MODE_PRIVATE);
        int curProgress = settings.getInt(AppConstants.MAX_FRAME_NAME, AppConstants.DEFAULT_MAX_FRAME);
        String curPath = settings.getString(AppConstants.SAVE_PATH_NAME,
                Environment.getExternalStorageDirectory().getPath()+"/"+AppConstants.APP_NAME);

        dataList.add(new EmptyItem(MultipleItem.EMPTY));
        dataList.add(new MaxFrameItem(MultipleItem.MAX_FRAME, "最大帧数:", curProgress));
        dataList.add(new SavePathItem(MultipleItem.SAVE_PATH, "存储路径:", curPath));
        dataList.add(new EmptyItem(MultipleItem.EMPTY));
        dataList.add(new UpdateItem(MultipleItem.UPDATE, "版本更新", Utils.getVersion(this)));
        dataList.add(new MultipleItem(MultipleItem.STAR, "去评分"));
        dataList.add(new MultipleItem(MultipleItem.FRIEND, "分享给朋友"));
        dataList.add(new MultipleItem(MultipleItem.FEEDBACK, "反馈"));

        mAdapter = new MultiItemAdapter(this, dataList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, final int position) {
                switch (adapter.getItemViewType(position)) {
                    case MultipleItem.MAX_FRAME:
                        break;
                    case MultipleItem.SAVE_PATH:
                        if (mPathSelectFragment == null) {
                            mPathSelectFragment = PathSelectFragment.newInstance(
                                    getResources().getString(R.string.select_path));
                            mPathSelectFragment.setOnDialogFragmentClick(new PathSelectFragment.PathSelectDialogFragmentClick() {
                                @Override
                                public void doPositiveClick(String selectedDir) {
                                    SharedPreferences settings = SettingsActivity.this.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putString(AppConstants.SAVE_PATH_NAME, selectedDir);
                                    editor.apply();
                                    try {
                                        if (dataList.size() > 0 && position < dataList.size()) {
                                            ((SavePathItem) dataList.get(position)).setSavePath(selectedDir);
                                            mAdapter.notifyItemChanged(position);
                                        }
                                    } catch (Exception e) {

                                    }
                                }

                                @Override
                                public void doNegativeClick() {

                                }
                            });
                        }
                        mPathSelectFragment.show(SettingsActivity.this.getSupportFragmentManager(), "path_select_dialog");
                        break;
                    case MultipleItem.UPDATE:
                        SettingUtil.appUpdate(SettingsActivity.this);
                        break;
                    case MultipleItem.STAR:
                        SettingUtil.giveStar(SettingsActivity.this);
                        break;
                    case MultipleItem.FRIEND:
                        SettingUtil.tellFriends(SettingsActivity.this);
                        break;
                    case MultipleItem.FEEDBACK:
                        SettingUtil.feedBack(SettingsActivity.this);
                        break;
                    case MultipleItem.EMPTY:
                        break;
                }
            }
        });
    }

}
