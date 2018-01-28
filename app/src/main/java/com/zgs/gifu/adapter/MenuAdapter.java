package com.zgs.gifu.adapter;

/**
 * Created by zgs on 2017/1/23.
 */


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zgs.gifu.R;
import com.zgs.gifu.entity.MenuItem;

import java.util.List;

/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public class MenuAdapter extends BaseQuickAdapter<MenuItem, BaseViewHolder> {
    public MenuAdapter(int layoutResId, List data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, MenuItem item) {
        holder.setText(R.id.text, item.getTitle());
        holder.setImageResource(R.id.icon, item.getImageResource());

    }
}