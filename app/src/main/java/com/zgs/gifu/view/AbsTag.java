package com.zgs.gifu.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

/**
 * Created by zgs on 2016/12/22.
 */

public abstract class AbsTag implements Cloneable {

    protected TagView mTagView;
    protected int mParentWidth;
    protected int mParentHeight;

    protected PointF mDeleteBtnCenter;
    protected PointF mEditBtnCenter;

    protected Paint mOutlinePaint;
    protected float mRotation;

    protected float mAngle = 0.0f;
    protected float mScale = 1.0f;
    protected float mMaxScale = 1.0f;
    protected float mCenterX;
    protected float mCenterY;

    protected float getmMinX() {
        return mMinX;
    }

    protected float getmMinY() {
        return mMinY;
    }

    protected float getmCenterY() {
        return mCenterY;
    }

    protected float getmCenterX() {
        return mCenterX;
    }

    protected float getmMaxX() {
        return mMaxX;
    }

    protected float getmMaxY() {
        return mMaxY;
    }

    protected float mMinX;
    protected float mMinY;
    protected float mMaxX;
    protected float mMaxY;
    private static final float MIN_SCALE = 0.1f;

    public abstract PointF calculateSize();
    public abstract void drawGraphic(Canvas canvas);
    public abstract void updateColor(int color);
    public abstract void updateTypeface(Typeface typeface);

    public AbsTag(TagView tagView) {
        this.mTagView = tagView;

        if (this.mTagView != null) {
            this.mParentWidth = this.mTagView.getWidth();
            this.mParentHeight = this.mTagView.getHeight();
        }
    }

    public void init() {
        // Set up outline paint
        mOutlinePaint = createPaint();
        this.mCenterX = this.mParentWidth / 2;
        this.mCenterY = this.mParentHeight / 2;
        setPosAndScale(this.mCenterX, this.mCenterY, this.mScale, this.mAngle);
        calculateMaxScale();
    }

    public void calculateMaxScale() {
        float f1 = (this.mMaxX - this.mMinX) / this.mScale;
        float f2 = (this.mMaxY - this.mMinY) / this.mScale;
        this.mMaxScale = Math.min(1.5f * this.mParentWidth / f1, 1.5f * this.mParentHeight / f2);
        if (this.mScale > this.mMaxScale) {
            this.mScale = this.mMaxScale;
            updateBounds();
            updateDeleteBtnCenter();
            updateEditBtnCenter();
            if (mTagView != null) {
                this.mTagView.invalidate();
            }
        }
    }

    protected void setPosAndScale(float centerX, float centerY, float scale, float angle) {
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        this.mScale = this.mScale < MIN_SCALE ? MIN_SCALE : Math.min(scale, this.mMaxScale);
        this.mAngle = Math.abs(angle) < 0.01f ? 0 : angle;
        updateRotation();
        updateBounds();
        updateDeleteBtnCenter();
        updateEditBtnCenter();
    }

    public void updateDeleteBtnCenter() {
        this.mDeleteBtnCenter = new PointF(this.mMaxX, this.mMinY);  //TODO
    }

    public void updateEditBtnCenter() {
        this.mEditBtnCenter = new PointF(this.mMaxX, this.mMaxY);  //TODO
    }

    public void updateBounds() {
        PointF pointF = calculateSize();
        this.mMinX = (this.mCenterX - pointF.x / 2.0f);
        this.mMinY = (this.mCenterY - pointF.y / 2.0f);
        this.mMaxX = (this.mCenterX + pointF.x / 2.0f);
        this.mMaxY = (this.mCenterY + pointF.y / 2.0f);
    }


    private void updateRotation() {
        this.mRotation = 180.0f * this.mAngle / 3.141593f;
    }

    private PointF rotatePoint(float x, float y)
    {
        double d1 = Math.cos(6.283185307179586D - this.mAngle);
        double d2 = Math.sin(6.283185307179586D - this.mAngle);
        double d3 = d1 * (x - this.mCenterX) - d2 * (y - this.mCenterY) + this.mCenterX;
        double d4 = d2 * (x - this.mCenterX) + d1 * (y - this.mCenterY) + this.mCenterY;
        return new PointF((float)d3, (float)d4);
    }

    private Paint createPaint() {
        Paint outlinePaint = new Paint();
        outlinePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(convertDipToPix(2, mTagView.getContext()));
        outlinePaint.setColor(Color.GRAY);
        outlinePaint.setPathEffect(new DashPathEffect(new float[] { 10.0F, 20.0F }, 0.0F));
        return outlinePaint;
    }

    protected void drawSelection(Canvas canvas, Drawable drawableDelete, Drawable drawableEdit) {
        preDraw(canvas);
        drawOutline(canvas);
        drawDeleteBtn(canvas, drawableDelete);
        drawEditBtn(canvas, drawableEdit);
        canvas.restore();
    }

    private void drawOutline(Canvas canvas) {
        canvas.drawRect(this.mMinX, this.mMinY, this.mMaxX, this.mMaxY, this.mOutlinePaint);
    }

    private void drawDeleteBtn(Canvas canvas, Drawable drawable) {
        float f1 = drawable.getIntrinsicWidth() / 2.0f;
        float f2 = drawable.getIntrinsicHeight() / 2.0f;
        Rect rect = new Rect();
        rect.left = Math.round(this.mDeleteBtnCenter.x - f1);
        rect.top = Math.round(this.mDeleteBtnCenter.y - f2);
        rect.right = Math.round(this.mDeleteBtnCenter.x + f1);
        rect.bottom = Math.round(this.mDeleteBtnCenter.y + f2);
        drawable.setBounds(rect);
        drawable.draw(canvas);
    }

    private void drawEditBtn(Canvas canvas, Drawable drawable) {
        float f1 = drawable.getIntrinsicWidth() / 2.0f;
        float f2 = drawable.getIntrinsicHeight() / 2.0f;
        Rect rect = new Rect();
        rect.left = Math.round(this.mEditBtnCenter.x - f1);
        rect.top = Math.round(this.mEditBtnCenter.y - f2);
        rect.right = Math.round(this.mEditBtnCenter.x + f1);
        rect.bottom = Math.round(this.mEditBtnCenter.y + f2);
        drawable.setBounds(rect);
        drawable.draw(canvas);
    }

    private void preDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(this.mCenterX, this.mCenterY);
        canvas.rotate(this.mRotation);
        canvas.translate(-this.mCenterX, -this.mCenterY);
    }


    protected void draw(Canvas canvas) {
        preDraw(canvas);
        drawGraphic(canvas);
        canvas.restore();
    }

    protected boolean containsPoint(float x, float y) {
        PointF pointF = rotatePoint(x, y);
        int radius = convertDipToPix(12, mTagView.getContext());
        return pointF.x >= this.mMinX - radius && pointF.x <= this.mMaxX + radius
                && pointF.y >= this.mMinY - radius && pointF.y <= this.mMaxY + radius;
    }


    private static int convertDipToPix(int paramInt, Context paramContext)
    {
        return (int)Math.ceil(TypedValue.applyDimension(1, paramInt, paramContext.getResources().getDisplayMetrics()));
    }

    protected boolean needDelete(float x, float y) {
        PointF pointF = rotatePoint(x, y);
        int radius = convertDipToPix(12, mTagView.getContext());
        float minX = this.mDeleteBtnCenter.x - 2 * radius;
        float maxX = this.mDeleteBtnCenter.x + 2 * radius;
        float minY = this.mDeleteBtnCenter.y - 2 * radius;
        float maxY = this.mDeleteBtnCenter.y + 2 * radius;
        return pointF.x <= maxX && pointF.x >= minX && pointF.y <= maxY && pointF.y >= minY;
    }

    protected boolean needEdit(float x, float y) {
        PointF pointF = rotatePoint(x, y);
        int radius = convertDipToPix(12, mTagView.getContext());
        float minX = this.mEditBtnCenter.x - 2 * radius;
        float maxX = this.mEditBtnCenter.x + 2 * radius;
        float minY = this.mEditBtnCenter.y - 2 * radius;
        float maxY = this.mEditBtnCenter.y + 2 * radius;
        return pointF.x <= maxX && pointF.x >= minX && pointF.y <= maxY && pointF.y >= minY;
    }

    public void drawOnScaledCanvas(Canvas canvas, AbsTag tag)
    {
        float scaleFactor = (float) canvas.getWidth() / this.mParentWidth;
        AbsTag cloneTag = (AbsTag) tag.clone();
        if (cloneTag != null)
        {
            cloneTag.mScale = (scaleFactor * cloneTag.mScale);
            cloneTag.mCenterX = (scaleFactor * cloneTag.mCenterX);
            cloneTag.mCenterY = (scaleFactor * cloneTag.mCenterY);
            cloneTag.updateBounds();
            cloneTag.draw(canvas);
        }
    }

    @Override
    protected Object clone() {
        try
        {
            return (AbsTag)super.clone();
        } catch (CloneNotSupportedException localCloneNotSupportedException) {

        }
        return null;
    }

}
