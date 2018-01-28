package com.zgs.gifu.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;

import com.zgs.gifu.R;
import com.zgs.gifu.entity.ImageObj;
import com.zgs.gifu.utils.FileUtil;

import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by zgs on 2016/12/5.
 */
public class ViewPagerAdapter extends PagerAdapter {

    private List<ImageObj> mData;
    private Context mContext;
    private ViewPager viewPager;

    private ImageView mImage;
    private VideoView mVideo;
    private ImageButton mPlayBtn;
    private String mVideoPath;

    private OnItemClickListener mOnItemClickListener;
    public interface OnItemClickListener {
        void onImgClick(View view);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public ViewPagerAdapter(Context context, List<ImageObj> data, ViewPager viewPager) {
        this.mData = data;
        this.mContext = context;
        this.viewPager = viewPager;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        ImageObj imgObj = mData.get(position);
        if (FileUtil.isGif(imgObj.getPath())) {
            PhotoView photoView = new PhotoView(mContext);
            try {
                GifDrawable mGifPlaying = new GifDrawable(imgObj.getPath());
                photoView.setImageDrawable(mGifPlaying);
            } catch (IOException e) {
                e.printStackTrace();
            }

            photoView.setTag(position);

            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    mOnItemClickListener.onImgClick(view);
                }

                @Override
                public void onOutsidePhotoTap() {

                }
            });
            container.addView(photoView,
                    ViewPager.LayoutParams.MATCH_PARENT,
                    ViewPager.LayoutParams.MATCH_PARENT);

            return photoView;
        } else {
            View view = View.inflate(mContext, R.layout.video_play, null);
            mImage = (ImageView) view.findViewById(R.id.image);
            mVideo = (VideoView) view.findViewById(R.id.video);
            FrameLayout mControl = (FrameLayout) view.findViewById(R.id.control);
            mPlayBtn = (ImageButton) view.findViewById(R.id.player);

            mControl.setBackgroundColor(Color.TRANSPARENT);
            mPlayBtn.setVisibility(View.VISIBLE);
            view.setTag(position);
            mVideoPath = imgObj.getPath();
            initImage();
            initPlay();
            container.addView(view, 0);
            return view;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {

            return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        int pos = viewPager.getCurrentItem();
        if (object instanceof PhotoView) {
            PhotoView photoView = (PhotoView) object;
            if (pos == (Integer) photoView.getTag()) {
                return POSITION_NONE;  // -2
            } else {
                return POSITION_UNCHANGED; // -1
            }
        } else {
            View view = (View) object;
            if (pos == (Integer) view.getTag()) {
                return POSITION_NONE;  // -2
            } else {
                return POSITION_UNCHANGED; // -1
            }
        }
    }


    private void initImage() {
        mVideo.setVisibility(View.GONE);
        mImage.setVisibility(View.VISIBLE);

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(mVideoPath);

        Bitmap bitmap = mmr.getFrameAtTime();
        mImage.setImageBitmap(bitmap);
        mmr.release();
    }

    private void initPlay() {
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initVideo();
            }
        });
    }

    private void initVideo() {
        mVideo.setVisibility(View.VISIBLE);
        mImage.setVisibility(View.GONE);
        mVideo.setVideoPath(mVideoPath);

        mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideo.start();
                mPlayBtn.setVisibility(View.GONE);
            }
        });
        mVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayBtn.setVisibility(View.VISIBLE);
            }
        });
    }
}
