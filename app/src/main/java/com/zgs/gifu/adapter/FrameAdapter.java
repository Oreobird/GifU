package com.zgs.gifu.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zgs.gifu.R;
import com.zgs.gifu.entity.FrameObj;

import java.util.ArrayList;

/**
 * Created by zgs on 2017/3/3.
 */
public class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.ViewHolder> {
    private FrameObj[] frameArray;
    private LayoutInflater mLayoutInflater;
    private OnItemClickListener mOnItemClickListener;
    private ArrayList<FrameObj> frameList;

    public FrameAdapter(Context context, FrameObj[] frameArray, ArrayList<FrameObj> frameList) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.frameArray = frameArray;
        this.frameList = frameList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = this.mLayoutInflater.inflate(R.layout.frame_cell, parent, false);
        return new FrameAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //holder.filterIV.setImageBitmap(frameArray[position].getBitmap());
        Bitmap bmp = frameList.get(position).getBitmap();
        holder.frameIV.setImageBitmap(bmp);

        if (frameList.get(position).isTextTag()) {
            holder.frameIcon.setVisibility(View.VISIBLE);
        } else {
            holder.frameIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return frameArray == null ? 0 : frameArray.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView frameIV;
        private ImageView frameIcon;

        public ViewHolder(View itemView) {
            super(itemView);

            frameIV = (ImageView)itemView.findViewById(R.id.frame_iv);
            frameIcon = (ImageView)itemView.findViewById(R.id.frame_icon);

            frameIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (frameIcon.getVisibility() == View.GONE) {
                        frameIcon.setVisibility(View.VISIBLE);
                    } else {
                        frameIcon.setVisibility(View.GONE);
                    }
                    int position = getLayoutPosition();
                    mOnItemClickListener.onItemClick(v, position);
                }
            });
        }
    }

    public void setOnItemClickListener(FrameAdapter.OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int frameNum);
    }
}
