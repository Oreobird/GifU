package com.zgs.gifu.utils;

import android.content.Context;
import android.os.Bundle;

import com.zgs.gifu.entity.ImageObj;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zgs on 2016/12/9.
 */

public class TransitionData {
    private static final String EXTRA_IMAGE_LEFT = "_left";
    private static final String EXTRA_IMAGE_TOP = "_top";
    private static final String EXTRA_IMAGE_WIDTH = "_width";
    private static final String EXTRA_IMAGE_HEIGHT = "_height";
    private static final String EXTRA_IMAGE_POSITION = "_position";
    private static final String EXTRA_IMAGEOBJ_LIST = "_imageObjList";

    private final int thumbnailTop;
    private final int thumbnailLeft;
    private final int thumbnailWidth;
    private final int thumbnailHeight;
    public final int position;
    private String appId;
    public List<ImageObj> imageObjList;

    public TransitionData(Context context, int thumbnailLeft, int thumbnailTop,
                          int thumbnailWidth, int thumbnailHeight, int position,
                          List<ImageObj> imageObjList) {
        setAppId(context);

        this.thumbnailLeft = thumbnailLeft;
        this.thumbnailTop = thumbnailTop;
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;

        this.position = position;
        this.imageObjList = imageObjList;
    }

    public TransitionData(Context context, Bundle bundle) {
        setAppId(context);
        thumbnailTop = bundle.getInt(appId + EXTRA_IMAGE_TOP);
        thumbnailLeft = bundle.getInt(appId + EXTRA_IMAGE_LEFT);
        thumbnailWidth = bundle.getInt(appId + EXTRA_IMAGE_WIDTH);
        thumbnailHeight = bundle.getInt(appId + EXTRA_IMAGE_HEIGHT);

        position = bundle.getInt(appId + EXTRA_IMAGE_POSITION);
        imageObjList = (List<ImageObj>)bundle.getSerializable(appId + EXTRA_IMAGEOBJ_LIST);

    }

    private void setAppId(Context context) {
        appId = context.getPackageName();
    }


    public Bundle getBundle() {
        final Bundle bundle = new Bundle();

        bundle.putInt(appId + EXTRA_IMAGE_LEFT, thumbnailLeft);
        bundle.putInt(appId + EXTRA_IMAGE_TOP, thumbnailTop);
        bundle.putInt(appId + EXTRA_IMAGE_WIDTH, thumbnailWidth);
        bundle.putInt(appId + EXTRA_IMAGE_HEIGHT, thumbnailHeight);
        bundle.putInt(appId + EXTRA_IMAGE_POSITION, position);
        bundle.putSerializable(appId + EXTRA_IMAGEOBJ_LIST, (Serializable)imageObjList);
        return bundle;

    }
}
