package com.zgs.gifu.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.zgs.gifu.utils.FontUtil;

/**
 * Created by zgs on 2016/12/23.
 */

public class TextTag extends AbsTag {

    private String mText;

    private Rect mTextBounds;
    private Paint mTextPaint;
    private float mTextSize;
    private Path mPath;
    private float mBaseTextSize;

    public TextTag(TagView tagView, String text, float textSize) {
        super(tagView);
        this.mText = text;
        initTextPaint();
        if (textSize <= 0.0F) {
            updateTextSize();
            super.init();
        } else {
            this.mBaseTextSize = 2.0f * textSize;
            this.mTextPaint.setTextSize(this.mBaseTextSize);
        }

    }

    private void updateTextSize() {
        int i = 8;
        FontUtil.setTextSizeToBound(this.mTagView.getContext(), this.mTextPaint, this.mText,
                i*this.mParentWidth/12, i*this.mParentHeight/12);
        this.mBaseTextSize = this.mTextPaint.getTextSize();
    }



    private void initTextPaint() {
        this.mTextPaint = new Paint();
        this.mTextPaint.setTextAlign(Paint.Align.LEFT);
        this.mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        this.mTextPaint.setStyle(Paint.Style.FILL);
        this.mTextPaint.setStrokeWidth(5);
        this.mTextPaint.setColor(Color.RED);
        //this.mTextPaint.setTypeface(Typeface.DEFAULT); //TODO
        this.mTextPaint.setTextSize(this.mTextSize);
        updateColor(Color.WHITE);
    }

    @Override
    public void updateColor(int color) {
        this.mTextPaint.setColor(color);
        this.mTagView.invalidate();
    }

    @Override
    public void updateTypeface(Typeface typeface) {
        this.mTextPaint.setTypeface(typeface);
        updateBounds();
        super.updateDeleteBtnCenter();
        super.updateEditBtnCenter();
        super.calculateMaxScale();
        this.mTagView.invalidate();
    }

    public void setTextStr(String text) {
        this.mText = text;
        updateBounds();
        super.updateDeleteBtnCenter();
        super.updateEditBtnCenter();
        super.calculateMaxScale();
        this.mTagView.invalidate();
    }

    public String getTextStr() {
        return this.mText;
    }


    public Rect getTextBounds() {
        return mTextBounds;
    }

    public void updateBounds() {
        this.mTextSize = (this.mScale * this.mBaseTextSize);
        this.mTextPaint.setTextSize(this.mTextSize);
        super.updateBounds();
        float f1 = this.mMinX;  //TODO
        float f2 = this.mMinY + (this.mTextBounds.height() - this.mTextBounds.bottom); //TODO
        this.mPath = new Path();
        this.mTextPaint.getTextPath(this.mText, 0, this.mText.length(), f1, f2, this.mPath);
        this.mPath.close();
    }


    @Override
    public PointF calculateSize() {
        this.mTextBounds = new Rect();
        this.mTextPaint.getTextBounds(this.mText, 0, this.mText.length(), this.mTextBounds);
        PointF pointF = new PointF();
        pointF.x = (this.mTextPaint.measureText(this.mText));
        pointF.y = (this.mTextBounds.height());
        return pointF;
    }

    @Override
    public void drawGraphic(Canvas canvas) {
        canvas.drawPath(this.mPath, this.mTextPaint);
    }
}
