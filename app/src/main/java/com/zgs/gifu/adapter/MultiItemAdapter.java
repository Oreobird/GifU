package com.zgs.gifu.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zgs.gifu.R;
import com.zgs.gifu.constant.AppConstants;
import com.zgs.gifu.entity.MaxFrameItem;
import com.zgs.gifu.entity.MultipleItem;
import com.zgs.gifu.entity.SavePathItem;
import com.zgs.gifu.entity.UpdateItem;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by zgs on 2017/1/2.
 */


public class MultiItemAdapter extends BaseMultiItemQuickAdapter<MultipleItem, BaseViewHolder> {
    private Context mContext;

    public MultiItemAdapter(Context context, List data) {
        super(data);
        this.mContext = context;
        addItemType(MultipleItem.MAX_FRAME, R.layout.setting_max_frame);
        addItemType(MultipleItem.SAVE_PATH, R.layout.setting_save_path);
        addItemType(MultipleItem.UPDATE, R.layout.setting_update);
        addItemType(MultipleItem.EMPTY, R.layout.setting_empty_item);
        addItemType(MultipleItem.STAR, R.layout.setting_common_item);
        addItemType(MultipleItem.FRIEND, R.layout.setting_common_item);
        addItemType(MultipleItem.FEEDBACK, R.layout.setting_common_item);
    }

    @Override
    protected void convert(BaseViewHolder holder, MultipleItem item) {
        switch (holder.getItemViewType()) {
            case MultipleItem.MAX_FRAME:
                final TextView maxFrameTextView = holder.getView(R.id.max_frames_text);
                holder.setText(R.id.max_frame_title, item.getContent())
                        .setText(R.id.max_frames_text, ""+((MaxFrameItem) item).getMaxFrame())
                        .setProgress(R.id.max_frames_seekbar, ((MaxFrameItem) item).getProgress(), AppConstants.MAX_FRAME);
                ((SeekBar) holder.getView(R.id.max_frames_seekbar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        int curValue = AppConstants.MIN_FRAME + progress;
                        maxFrameTextView.setText("" + curValue);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        SharedPreferences settings = mContext.getSharedPreferences(AppConstants.PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt(AppConstants.MAX_FRAME_NAME, seekBar.getProgress() + AppConstants.MIN_FRAME);
                        editor.apply();
                    }
                });
                break;
            case MultipleItem.SAVE_PATH:
                holder.setText(R.id.save_path_title, item.getContent())
                        .setText(R.id.save_path, ((SavePathItem) item).getSavePath());

                break;
            case MultipleItem.UPDATE:
                holder.setText(R.id.update_item_title, item.getContent())
                        .setText(R.id.update_version, ((UpdateItem) item).getVersion());
                break;
            case MultipleItem.STAR:
                holder.setText(R.id.common_item_title, item.getContent());
                break;
            case MultipleItem.FRIEND:
                holder.setText(R.id.common_item_title, item.getContent());
                break;
            case MultipleItem.FEEDBACK:
                holder.setText(R.id.common_item_title, item.getContent());
                break;
            case MultipleItem.EMPTY:
                break;
        }
    }

}

