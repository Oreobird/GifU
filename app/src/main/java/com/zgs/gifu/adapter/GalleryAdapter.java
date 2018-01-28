package com.zgs.gifu.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.zgs.gifu.R;
import com.zgs.gifu.entity.ImageObj;
import com.zgs.gifu.utils.FileUtil;
import com.zgs.gifu.view.SquareImageView;

import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by zgs on 2016/11/30.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private LayoutInflater mLayoutInflater;
    private List<ImageObj> mData;

    private OnItemClickListener mOnItemClickListener;

    public GalleryAdapter(Context context, List<ImageObj> data) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private SquareImageView mImg;
        private ImageButton mEditBtn;
        private ImageButton mShareBtn;
        private ImageButton mDeleteBtn;
        private ImageButton mVideoPlay;

        public ViewHolder(View itemView) {
            super(itemView);
            mImg = (SquareImageView) itemView.findViewById(R.id.album_cell_image);
            mEditBtn = (ImageButton) itemView.findViewById(R.id.album_cell_edit);
            mShareBtn = (ImageButton) itemView.findViewById(R.id.album_cell_share);
            mDeleteBtn = (ImageButton) itemView.findViewById(R.id.album_cell_delete);
            mVideoPlay = (ImageButton) itemView.findViewById(R.id.album_cell_video_play);

            mVideoPlay.setOnClickListener(this);
            mImg.setOnClickListener(this);
            mEditBtn.setOnClickListener(this);
            mShareBtn.setOnClickListener(this);
            mDeleteBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getLayoutPosition();
            switch (v.getId()) {
                case R.id.album_cell_video_play:
                case R.id.album_cell_image:
                    mOnItemClickListener.onImgClick(v, position);
                    break;
                case R.id.album_cell_edit:
                    mOnItemClickListener.onEditClick(v, position);
                    break;
                case R.id.album_cell_share:
                    mOnItemClickListener.onShareClick(v, position);
                    break;
                case R.id.album_cell_delete:
                    mOnItemClickListener.onDeleteClick(v, position);
                    break;
                default:
                    break;
            }
        }


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.album_cell, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ImageObj imgObj = mData.get(position);
        if (FileUtil.isGif(imgObj.getPath())) {
            holder.mEditBtn.setVisibility(View.VISIBLE);
            holder.mVideoPlay.setVisibility(View.GONE);
            try {
                GifDrawable mGifPlaying = new GifDrawable(imgObj.getPath());
                holder.mImg.setImageDrawable(mGifPlaying);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (FileUtil.isMP4(imgObj.getPath())) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(imgObj.getPath());

            Bitmap bitmap = mmr.getFrameAtTime();
            holder.mImg.setImageBitmap(bitmap);
            mmr.release();
            holder.mEditBtn.setVisibility(View.GONE);
            holder.mVideoPlay.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public interface OnItemClickListener {
        void onImgClick(View view, int position);
        void onEditClick(View view, int position);
        void onShareClick(View view, int position);
        void onDeleteClick(View view, int position);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}