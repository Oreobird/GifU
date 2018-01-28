package com.zgs.gifu.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zgs.gifu.GifApplication;
import com.zgs.gifu.R;
import com.zgs.gifu.adapter.GalleryAdapter;
import com.zgs.gifu.constant.AppConstants;
import com.zgs.gifu.entity.ImageObj;
import com.zgs.gifu.fragment.ConfirmFragment;
import com.zgs.gifu.utils.ActivityLauncher;
import com.zgs.gifu.utils.GifUtil;
import com.zgs.gifu.utils.Utils;

import net.youmi.android.nm.bn.BannerManager;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AlbumActivity extends BaseActivity {

    private static final int NOT_EMPTY_DATA = 1;
    private static final int EMPTY_DATA = 2;
    private RecyclerView mRecyclerView;
    private GalleryAdapter mAdapter;
    private List<ImageObj> mImageList;
    private List<ImageObj> mVideoList;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.page_album);

        initView();
        setupBanner();
    }

    private void setupBanner() {
        // 获取广告条
        View bannerView = BannerManager.getInstance(this).getBannerView(this, null);

        // 获取要嵌入广告条的布局
        LinearLayout bannerLayout = (LinearLayout) findViewById(R.id.ll_banner);

        // 将广告条加入到布局中
        bannerLayout.addView(bannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            requestExternalStoragePermission();
        } else {
            loadData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 展示广告条窗口的 onDestroy() 回调方法中调用
        BannerManager.getInstance(this).onDestroy();
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

    private void initActionBar() {
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_activity_album);
        }
    }

    private void initView() {
        initActionBar();
        mTextView = (TextView) findViewById(R.id.loading_txt);
        mRecyclerView = (RecyclerView) findViewById(R.id.album_rv);
        /* Set layout manager */
        GridLayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        mLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private static class AlbumHandler extends Handler {
        private final WeakReference<AlbumActivity> mActivity;

        private AlbumHandler(AlbumActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final AlbumActivity activity = mActivity.get();

            if (activity != null) {
                switch (msg.what) {
                    case EMPTY_DATA:
                        activity.mTextView.setText(activity.getResources().getString(R.string.empty_data));
                        break;
                    case NOT_EMPTY_DATA:
                        activity.mTextView.setVisibility(View.GONE);
                        activity.mRecyclerView.setVisibility(View.VISIBLE);
                        activity.mAdapter = new GalleryAdapter(activity.getApplicationContext(), activity.mImageList);
                        activity.mRecyclerView.setAdapter(activity.mAdapter);

                        /* Set adapter callback */
                        activity.mAdapter.setmOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
                            @Override
                            public void onImgClick(View view, int position) {
                                if (activity.mImageList == null || activity.mImageList.size() == 0
                                        || activity.mImageList.size() < position) {
                                    return;
                                }
                                final Intent intent = new Intent(activity, AlbumDetailActivity.class);
                                ActivityLauncher.with(activity)
                                        .from(view)
                                        .position(position)
                                        .dataList(activity.mImageList)
                                        .launch(intent);
                            }

                            @Override
                            public void onEditClick(View view, int position) {
                                if (activity.mImageList == null || activity.mImageList.size() == 0
                                        || activity.mImageList.size() < position) {
                                    return;
                                }
                                Intent intent = new Intent(activity, GifViewActivity.class);
                                GifApplication gifApp = (GifApplication) activity.getApplication();

                                GifUtil.with(activity)
                                        .path(activity.mImageList.get(position).getPath())
                                        .doEdit(intent, gifApp);
                            }

                            @Override
                            public void onShareClick(View view, int position) {
                                if (activity.mImageList == null || activity.mImageList.size() == 0
                                        || activity.mImageList.size() < position) {
                                    return;
                                }
                                GifUtil.with(activity)
                                        .path(activity.mImageList.get(position).getPath())
                                        .doShare();
                            }

                            @Override
                            public void onDeleteClick(View view, int position) {
                                ConfirmFragment deleteConfirmFragment = ConfirmFragment.newInstance(
                                        activity.getResources().getString(R.string.delete_txt),
                                        activity.getResources().getString(R.string.cancel_txt),
                                        activity.getResources().getString(R.string.delete_txt),
                                        activity.getResources().getString(R.string.delete_confirm_msg));
                                deleteConfirmFragment.setmOnDialogItemClick(position, new ConfirmFragment.DialogFragmentClick() {
                                    @Override
                                    public void doPositiveClick(int position) {
                                        if (activity.mImageList == null || activity.mImageList.size() == 0
                                                || activity.mImageList.size() < position) {
                                            return;
                                        }
                                        GifUtil.with(activity)
                                                .path(activity.mImageList.get(position).getPath())
                                                .updateDataStrategy(new GifUtil.UpdateDataStrategy() {
                                                    @Override
                                                    public void update(int position) {
                                                        activity.mImageList.remove(position);
                                                        activity.mAdapter.notifyItemRemoved(position);
                                                        activity.mAdapter.notifyDataSetChanged();
                                                        if (activity.mImageList != null && activity.mImageList.size() <= 0) {
                                                            activity.mRecyclerView.setVisibility(View.GONE);
                                                            activity.mTextView.setVisibility(View.VISIBLE);
                                                            activity.mTextView.setText(activity.getResources().getString(R.string.empty_data));
                                                        }
                                                    }
                                                })
                                                .doDelete(position);
                                    }

                                    @Override
                                    public void doNegativeClick(int position) {
                                        //Do nothing
                                    }
                                });
                                deleteConfirmFragment.show(activity.getFragmentManager(), "delete_confirm_fm");
                            }
                        });
                        break;
                }
            }
        }
    }

    private AlbumHandler mHandler = new AlbumHandler(this);

    private class CompareDate implements Comparator<ImageObj> {

        @Override
        public int compare(ImageObj o1, ImageObj o2) {
            return -o1.getDate().compareToIgnoreCase(o2.getDate());
        }
    }
    private void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mImageList = Utils.getImages(AlbumActivity.this, AppConstants.APP_GIF);
                mVideoList = Utils.getVideos(AlbumActivity.this, AppConstants.APP_GIF);
                mImageList.addAll(mVideoList);
                Collections.sort(mImageList, new CompareDate());
                if (mImageList != null && mImageList.size() <= 0) {
                    mHandler.sendEmptyMessage(EMPTY_DATA);
                } else {
                    mHandler.sendEmptyMessage(NOT_EMPTY_DATA);
                }
            }
        }).start();
    }

    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 0;
    private final static int READ_PHONE_STATE_CODE = 1;
    private void requestExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE},
                    REQUEST_EXTERNAL_STORAGE_PERMISSION);
        } else {
            loadData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "您没有授权存储权限，请在设置中打开授权", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    loadData();
                }
                return;
            case READ_PHONE_STATE_CODE:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "您没有授权手机信息权限，请在设置中打开授权", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    loadData();
                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
