package com.zgs.gifu.utils;

/**
 * Created by zgs on 2016/12/14.
 */

public class ImageUtil {
    static {
        System.loadLibrary("image_util");
    }

    public native void decodeYUV420SP(int[] outData, byte[] buf, int width, int height);


}
