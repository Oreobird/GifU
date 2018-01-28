package com.zgs.gifu.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zgs.gifu.R;
import com.zgs.gifu.imageprocess.ImageFilter;

import jp.co.cyberagent.android.gpuimage.GPUImage;

/**
 * Created by zgs on 2017/2/28.
 */
public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

    private LayoutInflater mLayoutInflater;
    private OnItemClickListener mOnItemClickListener;
    private Bitmap sample;
    private int[] filterList;
    private GPUImage gpuImage;

    public FilterAdapter(Context context, Bitmap sample, int[] filterList) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.sample = sample;
        this.filterList = filterList;
        this.gpuImage = new GPUImage(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = this.mLayoutInflater.inflate(R.layout.filter_cell, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Bitmap bitmap = ImageFilter.filterApply(gpuImage, sample, filterList[position]);
        holder.filterIV.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return filterList == null ? 0 : filterList.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView filterIV;

        public ViewHolder(View itemView) {
            super(itemView);

            filterIV = (ImageView)itemView.findViewById(R.id.filter_iv);

            filterIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getLayoutPosition();
                    mOnItemClickListener.onItemClick(v, filterList[position]);
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int filter);
    }

}
