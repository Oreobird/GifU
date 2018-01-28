package com.zgs.gifu.entity;

/**
 * Created by zgs on 2017/1/23.
 */


public class UpdateItem extends MultipleItem {
    private String version;

    public UpdateItem(int itemType, String content, String version) {
        super(itemType, content);
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}