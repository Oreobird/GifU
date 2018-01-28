package com.zgs.gifu.entity;

import android.graphics.Bitmap;

/**
 * Created by zgs on 2017/3/3.
 */

public class FrameObj implements Cloneable{
    private Bitmap bitmap;
    private boolean isTextTag;


    private boolean tagFlag;
    private boolean filterFlag;

    public FrameObj(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.isTextTag = false;
    }

    public FrameObj(Bitmap bitmap, boolean isTextTag) {
        this.bitmap = bitmap;
        this.isTextTag = isTextTag;
        this.filterFlag = false;
        this.tagFlag = false;
    }

    public void setTagFlag(boolean tagFlag) {
        this.tagFlag = tagFlag;
    }

    public boolean isFilterFlag() {
        return filterFlag;
    }

    public void setFilterFlag(boolean filter) {
        filterFlag = filter;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isTextTag() {
        return isTextTag;
    }

    public void setTextTag(boolean textTag) {
        isTextTag = textTag;
    }

    public FrameObj clone()
    {
        try
        {
            FrameObj cloned = (FrameObj) super.clone();
            cloned.bitmap = Bitmap.createBitmap(this.bitmap);
            return cloned;
        }
        catch (CloneNotSupportedException localCloneNotSupportedException) {

        }
        return null;
    }
}
