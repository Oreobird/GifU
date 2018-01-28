package com.zgs.gifu.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zgs.gifu.R;

/**
 * Created by zgs on 2017/2/28.
 */
public class BoarderAdapter extends RecyclerView.Adapter<BoarderAdapter.ViewHolder> {

    private LayoutInflater mLayoutInflater;
    private OnItemClickListener mOnItemClickListener;
    private int[] dataList;
    private Resources mResources;

    public BoarderAdapter(Context context, int[] dataList) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mResources = context.getResources();
        this.dataList = dataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = this.mLayoutInflater.inflate(R.layout.filter_cell, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Drawable drawable = mResources.getDrawable(dataList[position]);//获取drawable
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        holder.filterIV.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.length;
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
                    mOnItemClickListener.onItemClick(v, dataList[position]);
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int boarder);
    }

}
