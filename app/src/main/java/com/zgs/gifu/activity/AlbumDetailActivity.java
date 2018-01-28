package com.zgs.gifu.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.zgs.gifu.GifApplication;
import com.zgs.gifu.R;
import com.zgs.gifu.adapter.ViewPagerAdapter;
import com.zgs.gifu.fragment.ConfirmFragment;
import com.zgs.gifu.utils.FileUtil;
import com.zgs.gifu.utils.GifUtil;
import com.zgs.gifu.utils.TransitionData;
import com.zgs.gifu.view.HackyViewPager;

/**
 * Created by zgs on 2016/12/6.
 */
public class AlbumDetailActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private ImageButton mEditBtn;
    private ViewPagerAdapter mAdapter;
    private TransitionData transitionData;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.page_album_detail);
        initActionBar();
        ImageButton mDeleteBtn = (ImageButton) findViewById(R.id.album_detail_delete);
        mEditBtn = (ImageButton) findViewById(R.id.album_detail_edit);
        ImageButton mShareBtn = (ImageButton) findViewById(R.id.album_detail_share);

        mViewPager = (HackyViewPager) findViewById(R.id.album_detail_view);

        //获取AlbumActivity传来的参数：imageObjList，点击的位置
        final Bundle bundle = getIntent().getExtras();
        transitionData = new TransitionData(this, bundle);   //this ?

        //根据传来的参数设置viewpager中的子item
        mAdapter = new ViewPagerAdapter(this, transitionData.imageObjList, mViewPager);
        mAdapter.setmOnItemClickListener(new ViewPagerAdapter.OnItemClickListener() {
            @Override
            public void onImgClick(View view) {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(transitionData.position);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (FileUtil.isMP4(transitionData.imageObjList.get(position).getPath())) {
                    mEditBtn.setVisibility(View.GONE);
                } else {
                    mEditBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmFragment deleteConfirmFragment = ConfirmFragment.newInstance(
                        getResources().getString(R.string.delete_txt),
                        getResources().getString(R.string.cancel_txt),
                        getResources().getString(R.string.delete_txt),
                        getResources().getString(R.string.delete_confirm_msg));
                deleteConfirmFragment.setmOnDialogItemClick(mViewPager.getCurrentItem(), new ConfirmFragment.DialogFragmentClick() {
                    @Override
                    public void doPositiveClick(int position) {
                        GifUtil.with(AlbumDetailActivity.this)
                                .path(transitionData.imageObjList.get(position).getPath())
                                .updateDataStrategy(new GifUtil.UpdateDataStrategy() {
                                    @Override
                                    public void update(int position) {
                                        transitionData.imageObjList.remove(position);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                })
                                .doDelete(position);
                    }

                    @Override
                    public void doNegativeClick(int position) {
                        //Do nothing
                    }
                });

                deleteConfirmFragment.show(getFragmentManager(), "delete_confirm_fm");
            }
        });

        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GifUtil.with(AlbumDetailActivity.this)
                        .path(transitionData.imageObjList.get(mViewPager.getCurrentItem()).getPath())
                        .doShare();
            }
        });

        mEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlbumDetailActivity.this, GifViewActivity.class);
                GifApplication gifApp = (GifApplication) getApplication();
                GifUtil.with(AlbumDetailActivity.this)
                        .path(transitionData.imageObjList.get(mViewPager.getCurrentItem()).getPath())
                        .doEdit(intent, gifApp);
                finish();
            }
        });
    }


}
