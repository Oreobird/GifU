package com.zgs.gifu.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zgs.gifu.R;

import java.util.ArrayList;

/**
 * Created by zgs on 2017/1/2.
 */

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.ViewHolder> {

    private LayoutInflater mLayoutInflater;
    private OnItemClickListener mOnItemClickListener;
    private ArrayList<Typeface> typefaceList;
    private String[] typefaceNames;

    public FontAdapter(Context context, ArrayList<Typeface> typefaceList, String[] typefaceNames) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.typefaceList = typefaceList;
        this.typefaceNames = typefaceNames;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Typeface typeface);
    }

    @Override
    public FontAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = this.mLayoutInflater.inflate(R.layout.font_cell, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FontAdapter.ViewHolder holder, int position) {
        Typeface typeface = typefaceList.get(position);
        holder.mTextView.setText(typefaceNames[position]);
        holder.mTextView.setTypeface(typeface);
    }

    @Override
    public int getItemCount() {
        return typefaceList == null ? 0 : typefaceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            mTextView = (TextView) itemView.findViewById(R.id.sample_txt);
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, mTextView.getTypeface());
                }
            });
        }
    }


}
