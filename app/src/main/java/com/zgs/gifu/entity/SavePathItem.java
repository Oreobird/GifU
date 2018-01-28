package com.zgs.gifu.entity;

/**
 * Created by zgs on 2017/1/23.
 */


public class SavePathItem extends MultipleItem {
    private String savePath;

    public SavePathItem(int itemType, String content, String savePath) {
        super(itemType, content);
        this.savePath = savePath;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }
}