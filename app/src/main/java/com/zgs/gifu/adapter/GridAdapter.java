package com.zgs.gifu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zgs.gifu.R;
import com.zgs.gifu.entity.ImageObj;
import com.zgs.gifu.utils.Utils;

import java.util.List;

/**
 * Created by zgs on 2017/1/1.
 */

public class GridAdapter extends BaseAdapter {
    private List<ImageObj> mImageList;
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<ImageObj> mGifImages;

    public GridAdapter(Context context, List<ImageObj> imageList,
                       LayoutInflater layoutInflater, List<ImageObj> gifImages) {
        this.mContext = context;
        this.mImageList = imageList;
        this.mLayoutInflater = layoutInflater;
        this.mGifImages = gifImages;
    }

    @Override
    public int getCount() {
        return mImageList == null ? 0 : mImageList.size();
    }

    @Override
    public Object getItem(int position) {
        return mImageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        ImageObj imageObj = (ImageObj)getItem(position);
        String path = imageObj.getPath();

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.cell, parent, false);
            viewHolder.imageView = (ImageView)convertView.findViewById(R.id.cell_iv);
            viewHolder.selectedIcon = (ImageView)convertView.findViewById(R.id.cell_icon);
            viewHolder.gifText = (TextView)convertView.findViewById(R.id.cell_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        boolean isSelected = Utils.containObj(mGifImages, imageObj);

        ((FrameLayout) convertView).setForeground(isSelected ? mContext.getResources().getDrawable(R.drawable.gridview_item_selected) : null);
        viewHolder.selectedIcon.setVisibility(isSelected ? View.VISIBLE : View.GONE);

			/* Refresh only one cell in grid view */
        if (viewHolder.imgObj == null || !viewHolder.imgObj.equals(imageObj)) {

            Glide.with(mContext)
                    .load(path)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .centerCrop()
                    .placeholder(R.drawable.loading)
                    .into(viewHolder.imageView);

            viewHolder.imgObj = imageObj;

            if (viewHolder.imgObj.getPath().toLowerCase().endsWith(".gif")) {
                viewHolder.gifText.setVisibility(View.VISIBLE);
            } else {
                viewHolder.gifText.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView imageView;
        ImageObj imgObj;	/* memory problem */
        ImageView selectedIcon;
        TextView gifText;
    }
}
