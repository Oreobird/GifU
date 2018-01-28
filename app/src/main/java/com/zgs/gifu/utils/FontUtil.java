package com.zgs.gifu.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zgs on 2016/12/30.
 */

public class FontUtil {

    private static String getFileNameNoExt(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static String[] getAllTypefaceName(Context context) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] fileNames = assetManager.list("fonts");
            for (int i = 0; i < fileNames.length; i++) {
                fileNames[i] = getFileNameNoExt(fileNames[i]);
            }
            return fileNames;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Typeface> getAllTypeface(Context context) {
        ArrayList<Typeface> typefaces = new ArrayList<>();
        AssetManager assetManager = context.getAssets();
        try {
            String[] fileNames = assetManager.list("fonts");
            for (int i = 0; i < fileNames.length; i++) {
                typefaces.add(getTypeface(assetManager, fileNames[i]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return typefaces;
    }

    private static Typeface getTypeface(AssetManager assetManager, String name) {
        return name == null ? null : Typeface.createFromAsset(assetManager, "fonts/" + name);
    }

    public static void setTextSizeToBound(Context context, Paint textPaint, String text, int boundWidth, int boundHeight) {
        if ((textPaint != null) && (!TextUtils.isEmpty(text))) {
            adjustTextSizeToBound(context, textPaint, text, boundWidth, boundHeight, 1, Math.min(boundWidth, boundHeight));
        }
    }


    private static Point measureView(Paint paint, String textStr)
    {
        Rect localRect = new Rect();
        paint.getTextBounds(textStr, 0, textStr.length(), localRect);
        return new Point(localRect.width(), localRect.height());
    }

    private static void adjustTextSizeToBound(Context context, Paint textPaint, String text, int boundWidth, int boundHeight, int start, int end) {
        int deltaWidth;
        int deltaHeight;

        int mid = (end + start) / 2;

        textPaint.setTextSize(mid); //设置为中值

        if (end >= start) {
            Point localPoint = measureView(textPaint, text);   //设置完字体大小后，测试字符所在的矩形长宽
            deltaWidth = localPoint.x - boundWidth; //与设定的界限值比较
            deltaHeight = localPoint.y - boundHeight;

            if ((deltaWidth <= 0) && (deltaHeight <= 0)) {
                //如果字符实际长宽都比界限值小，则放大字符
                adjustTextSizeToBound(context, textPaint, text, boundWidth, boundHeight, mid + 1, end);

            } else if ((deltaWidth >= -DisplayUtil.dip2px(context, 4)) || (deltaHeight >= -DisplayUtil.dip2px(context, 4))) {
                adjustTextSizeToBound(context, textPaint, text, boundWidth, boundHeight, start, mid - 1);
            }
        }
    }
}
