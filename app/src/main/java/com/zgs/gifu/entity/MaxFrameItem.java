package com.zgs.gifu.entity;

import com.zgs.gifu.constant.AppConstants;

/**
 * Created by zgs on 2017/1/23.
 */

public class MaxFrameItem extends MultipleItem {
    private int maxFrame;
    private int progress;

    public MaxFrameItem(int itemType, String content, int maxFrame) {
        super(itemType, content);
        this.maxFrame = maxFrame;
        this.progress = maxFrame - AppConstants.MIN_FRAME;
    }

    public int getMaxFrame() {
        return maxFrame;
    }

    public void setMaxFrame(int maxFrame) {
        this.maxFrame = maxFrame;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}