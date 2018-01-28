package com.zgs.gifu.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by zgs on 2017/1/23.
 */

public class MultipleItem implements MultiItemEntity {
    public static final int MAX_FRAME = 1;
    public static final int SAVE_PATH = 2;
    public static final int UPDATE = 3;
    public static final int EMPTY = 4;
    public static final int STAR = 5;
    public static final int FRIEND = 6;
    public static final int FEEDBACK = 7;

    private int itemType;
    private String content;

    public MultipleItem(int itemType, String content) {
        this.itemType = itemType;
        this.content = content;
    }

    public MultipleItem(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
