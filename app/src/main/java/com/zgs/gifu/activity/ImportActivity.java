package com.zgs.gifu.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.zgs.gifu.GifApplication;
import com.zgs.gifu.R;
import com.zgs.gifu.adapter.GridAdapter;
import com.zgs.gifu.constant.AppConstants;
import com.zgs.gifu.entity.FrameObj;
import com.zgs.gifu.entity.ImageObj;
import com.zgs.gifu.fragment.ConfirmFragment;
import com.zgs.gifu.utils.GifUtil;
import com.zgs.gifu.utils.SettingUtil;
import com.zgs.gifu.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ImportActivity extends AppCompatActivity {
    private LayoutInflater mLayoutInflater;
    private GridView mGridView;
    private GridAdapter mAdapter;

    private TextView mFrameNumIndicator;
    private LinearLayout mGallery;
    private TextView mSelectedImageEmptyMsg;

    private List<ImageObj> gifImages = new LinkedList<>();
    private int frames;
    private GifApplication gifApp;

    private boolean clickReady = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.page_import);

        gifApp = (GifApplication) this.getApplication();
        gifApp.clearFrameList(gifApp.getGifFrameList());
        initView();
        if (Build.VERSION.SDK_INT >= 23) {
            requestExternalStoragePermission();
        } else {
            loadData();
        }
    }

    private void initView() {
        mGallery = (LinearLayout) findViewById(R.id.import_gallery);

        mFrameNumIndicator = (TextView) findViewById(R.id.frame_num_indicator);
        Button doneBtn = (Button) findViewById(R.id.import_done);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDone();
            }
        });

        Button cancelBtn = (Button) findViewById(R.id.import_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAllObj(gifImages);
                gifApp.clearFrameList(gifApp.getGifFrameList());
                finish();
            }
        });

        Button resetBtn = (Button) findViewById(R.id.import_reset);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doReset();
            }
        });
        mLayoutInflater = LayoutInflater.from(ImportActivity.this);
        mSelectedImageEmptyMsg = (TextView) findViewById(R.id.selected_images_empty);
        mGridView = (GridView) findViewById(R.id.import_gridview);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ImageObj imageObj = (ImageObj) mAdapter.getItem(i);
                if (clickReady) {
                    if (!Utils.containObj(gifImages, imageObj)) {
                        addObj(gifImages, imageObj);
                    } else {
                        removeObj(gifImages, imageObj);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void doDone() {
        if (gifImages.size() == 0) {
            Toast.makeText(ImportActivity.this,
                    ImportActivity.this.getResources().getString(R.string.no_image_selected),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < gifImages.size(); i++) {
            final ImageObj localImgObj = gifImages.get(i);
            Glide.with(ImportActivity.this).load(localImgObj.getPath())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .into(new SimpleTarget<Bitmap>(AppConstants.GIF_WIDTH, AppConstants.GIF_HEIGHT) {

                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                            if (localImgObj.getPath().toLowerCase().endsWith(".gif")) {
                                ArrayList<Bitmap> bitmapList = GifUtil.with(ImportActivity.this).decodeGif(localImgObj.getPath());
                                for (int i = 0; i < bitmapList.size(); i++) {
                                    gifApp.getGifFrameList().add(new FrameObj(bitmapList.get(i)));
                                }
                            } else {
                                gifApp.getGifFrameList().add(new FrameObj(resource));
                            }
                        }
                    });
        }

        if (!gifApp.getGifFrameList().isEmpty()) {
            Intent intent = new Intent(ImportActivity.this, GifViewActivity.class);
            startActivity(intent);
            removeAllObj(gifImages);
        }
    }

    private void doReset() {
        ConfirmFragment resetConfirmFragment = ConfirmFragment.newInstance(
                getResources().getString(R.string.reset_txt),
                getResources().getString(R.string.cancel_txt),
                getResources().getString(R.string.reset_txt),
                getResources().getString(R.string.reset_confirm_msg));
        resetConfirmFragment.setmOnDialogItemClick(0, new ConfirmFragment.DialogFragmentClick() {
            @Override
            public void doPositiveClick(int position) {
                removeAllObj(gifImages);
                gifApp.clearFrameList(gifApp.getGifFrameList());
            }

            @Override
            public void doNegativeClick(int position) {
                //Do nothing
            }
        });
        resetConfirmFragment.show(getFragmentManager(), "delete_confirm_fm");
    }

    private void addObj(List<ImageObj> imgList, ImageObj imgObj) {
        if (imgObj == null) {
            return;
        }

        int gifFramesNum = 1;
        if (imgList != null) {
            if (imgObj.getPath().toLowerCase().endsWith(".gif")) {
                gifFramesNum = GifUtil.with(ImportActivity.this).getGifFramesNum(imgObj.getPath());
                imgObj.setFrames(gifFramesNum);    //update frames of imgObj
            } else {
                gifFramesNum = 1;
            }

            if (frames + gifFramesNum > SettingUtil.getMaxFrames(ImportActivity.this)) {
                Toast.makeText(ImportActivity.this,
                        getResources().getString(R.string.max_image_selected),
                        Toast.LENGTH_SHORT).show();
                return;
            } else {
                frames += gifFramesNum;
            }
        }

        if (imgList != null) {
            imgList.add(imgObj);
        }

        clickReady = false;
        Glide.with(ImportActivity.this).load(imgObj.getPath())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop()
                .into(new SimpleTarget<Bitmap>(AppConstants.GIF_WIDTH, AppConstants.GIF_HEIGHT) {

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        ImageView img = new ImageView(mGallery.getContext());
                        img.setPadding(2, 2, 2, 2);
                        img.setImageBitmap(resource);
                        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
                        img.setLayoutParams(new LinearLayout.LayoutParams(px / 4 * 3, px));
                        mGallery.addView(img);
                        clickReady = true;
                    }
                });

        if (imgList != null && imgList.size() >= 1) {
            mGallery.setVisibility(View.VISIBLE);
            mFrameNumIndicator.setVisibility(View.VISIBLE);
            mFrameNumIndicator.setText(frames + "/" + SettingUtil.getMaxFrames(ImportActivity.this));
            mSelectedImageEmptyMsg.setVisibility(View.GONE);
        }

    }

    private void removeObj(List<ImageObj> imgList, ImageObj imgObj) {
        if (imgList == null || imgList.size() == 0 || imgObj == null) {
            return;
        }
        try {
            mGallery.removeViewAt(imgList.indexOf(imgObj));
        } catch (Exception e) {
            return;
        }
        //gifApp.getGifFrameList().remove(imgList.indexOf(imgObj));
        frames -= imgObj.getFrames();
        imgList.remove(imgObj);

        if (imgList.size() == 0) {
            mGallery.setVisibility(View.GONE);
            mFrameNumIndicator.setText(frames + "/" + SettingUtil.getMaxFrames(ImportActivity.this));
            mFrameNumIndicator.setVisibility(View.GONE);
            mSelectedImageEmptyMsg.setVisibility(View.VISIBLE);
        }
    }

    private void removeAllObj(List<ImageObj> imgList) {
        if (imgList == null || imgList.size() == 0) {
            return;
        }
        mGallery.removeAllViews();
        imgList.clear();
        frames = 0;
        if (imgList.size() == 0) {
            mGallery.setVisibility(View.GONE);
            mFrameNumIndicator.setVisibility(View.GONE);
            mFrameNumIndicator.setText(frames + "/" + SettingUtil.getMaxFrames(ImportActivity.this));
            mSelectedImageEmptyMsg.setVisibility(View.VISIBLE);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void loadData() {
        List<ImageObj> mImageList = Utils.getImages(ImportActivity.this);
        mAdapter = new GridAdapter(ImportActivity.this, mImageList, mLayoutInflater, gifImages);
        mGridView.setAdapter(mAdapter);
    }

    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 0;

    private void requestExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE_PERMISSION);
        } else {
            loadData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "您没有授权存储权限，请在设置中打开授权", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                loadData();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}