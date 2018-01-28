package com.zgs.gifu.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zgs.gifu.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by zgs on 2017/1/2.
 */

public class PathAdapter extends RecyclerView.Adapter<PathAdapter.ViewHolder> {

    private LayoutInflater mLayoutInflater;
    private OnItemClickListener mOnItemClickListener;
    private ArrayList<File> fileList;

    public PathAdapter(Context context, ArrayList<File> fileList) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.fileList = fileList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, String filePath);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = this.mLayoutInflater.inflate(R.layout.path_cell, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PathAdapter.ViewHolder holder, int position) {
        holder.mTextView.setText(fileList.get(position).getName());
        Log.d("PathAdapter", "===onBindViewHolder===");
    }

    @Override
    public int getItemCount() {
        return fileList == null ? 0 : fileList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            mTextView = (TextView) itemView.findViewById(R.id.path_text);
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, mTextView.getText().toString());
                }
            });
        }
    }


}
