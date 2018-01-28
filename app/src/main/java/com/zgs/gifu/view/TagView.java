package com.zgs.gifu.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.zgs.gifu.R;
import com.zgs.gifu.utils.MultiTouchController;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * TODO: document your custom view class.
 */
public class TagView extends View implements MultiTouchController.MultiTouchObjectCanvas<AbsTag> {

    private Drawable mDeleteDrawable;
    private Drawable mEditDrawable;

    private ArrayList<AbsTag> mTagList = new ArrayList<>();
    private AbsTag mSelectedTag;

    MultiTouchController<AbsTag> mTouchController = new MultiTouchController<>(this);

    Thread mUIThread;

    public interface OnBtnListener {
        void handleEditBtn();
    }

    private OnBtnListener mOnBtnListener;

    public void setOnBtnListener(OnBtnListener mOnBtnListener) {
        this.mOnBtnListener = mOnBtnListener;
    }

    public TagView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public TagView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public TagView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public void addTag(AbsTag tag) {
        if (tag != null) {
            this.mTagList.add(tag);
            mSelectedTag = tag;
            invalidate();
        }
    }

    public void removeTag(AbsTag tag) {
        if (tag != null) {
            this.mTagList.remove(tag);
            this.mSelectedTag = null;
            invalidate();
        }
    }

    public void removeAllTags() {
        this.mTagList.clear();
        this.mSelectedTag = null;
        invalidate();
    }

    public AbsTag getSelectedTag() {
        return this.mSelectedTag;
    }

    public int getTagCount() {
        return this.mTagList.size();
    }

    public ArrayList<AbsTag> getTagList() {
        ArrayList<AbsTag> tagList = new ArrayList<>(getTagCount());
        Iterator localIterator = this.mTagList.iterator();

        while (localIterator.hasNext()) {
            tagList.add((AbsTag)localIterator.next());
        }

        return tagList;
    }

    private float distance4PointF(PointF pf1, PointF pf2) {
        float disX = pf2.x - pf1.x;
        float disY = pf2.y - pf1.y;
        return (float)Math.sqrt(disX * disX + disY * disY);
    }
    public static double radianToDegree(double radian) {
        return radian * 180 / Math.PI;
    }
    private PointF mCenterPoint = new PointF();
    private PointF mPreMovePointF = new PointF();
    private PointF mCurMovePointF = new PointF();
    private float mDegree = 0;
    private float mScale = 0;
    int status = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean eat = false;
        mPreMovePointF.set(event.getX(), event.getY());
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            AbsTag tag = this.mSelectedTag;
            status = 0;
            if (tag != null) {
                if (tag.needDelete(event.getX(), event.getY())) {
                    this.removeTag(tag);
                    eat = true;
                } else if (tag.needEdit(event.getX(), event.getY())) {
                    mOnBtnListener.handleEditBtn();
                    //status = 1;
                }
            }
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (status == 1 && mSelectedTag != null) {
                int pointerCount = event.getPointerCount();
                // Handle history first (we sometimes get history with ACTION_MOVE events)
                int histLen = event.getHistorySize() / pointerCount;
                for (int histIdx = 0; histIdx <= histLen; histIdx++) {
                    float scale = 1f;
                    int tagWidth = ((TextTag) mSelectedTag).getTextBounds().width();
                    int tagHeight = ((TextTag) mSelectedTag).getTextBounds().height();
                    int halfBitmapWidth = tagWidth / 2;
                    int halfBitmapHeight = tagHeight / 2;
                    //图片某个点到图片中心的距离
                    float bitmapToCenterDistance = (float) Math.sqrt(halfBitmapWidth * halfBitmapWidth + halfBitmapHeight * halfBitmapHeight);
                    //移动的点到图片中心的距离
                    mCenterPoint.set(mSelectedTag.getmCenterX(), mSelectedTag.getmCenterY());
                    mCurMovePointF.set(event.getX(), event.getY());

                    float moveToCenterDistance = distance4PointF(mCenterPoint, mCurMovePointF);
                    //计算缩放比例
                    scale = moveToCenterDistance / bitmapToCenterDistance;
                    //缩放比例的界限判断
                    if (scale <= 0.2f) {
                        scale = 0.2f;
                    } else if (scale >= 2.0f) {
                        scale = 1.5f;
                    }
                    // 角度
                    double a = distance4PointF(mCenterPoint, mPreMovePointF);
                    double b = distance4PointF(mPreMovePointF, mCurMovePointF);
                    double c = distance4PointF(mCenterPoint, mCurMovePointF);
                    double cosb = (a * a + c * c - b * b) / (2 * a * c);
                    if (cosb >= 1) {
                        cosb = 1f;
                    }
                    double radian = Math.acos(cosb);
                    float newDegree = (float) radianToDegree(radian);
                    //center -> proMove的向量， 我们使用PointF来实现
                    PointF centerToProMove = new PointF((mPreMovePointF.x - mCenterPoint.x), (mPreMovePointF.y - mCenterPoint.y));
                    //center -> curMove 的向量
                    PointF centerToCurMove = new PointF((mCurMovePointF.x - mCenterPoint.x), (mCurMovePointF.y - mCenterPoint.y));
                    //向量叉乘结果, 如果结果为负数， 表示为逆时针， 结果为正数表示顺时针
                    float result = centerToProMove.x * centerToCurMove.y - centerToProMove.y * centerToCurMove.x;
                    if (result < 0) {
                        newDegree = -newDegree;
                    }
                    mDegree = mDegree + newDegree;
                    mDegree = 0;
                    mScale = scale;
                    mSelectedTag.setPosAndScale(mSelectedTag.getmCenterX(), mSelectedTag.getmCenterY(),
                            mScale,
                            mDegree);
                }
                invalidate();
                return true;
            }

        }
        eat = this.mTouchController.onTouchEvent(event);

        return eat;
    }

    @Override
    public void invalidate() {
        if (Thread.currentThread() == this.mUIThread) {
            super.invalidate();
        }
    }

    @Override
    public AbsTag getDraggableObjectAtPoint(MultiTouchController.PointInfo touchPoint) {
        float x = touchPoint.getX();
        float y = touchPoint.getY();
        for (int i = this.mTagList.size() - 1; i >= 0; i--) {
            AbsTag tag = this.mTagList.get(i);
            if (tag.containsPoint(x, y)) {
                return tag;
            }
        }
        this.mSelectedTag = null;
        invalidate();
        return null;
    }

    @Override
    public void getPositionAndScale(AbsTag obj, MultiTouchController.PositionAndScale objPosAndScaleOut) {
        objPosAndScaleOut.set(obj.mCenterX, obj.mCenterY, true, obj.mScale, false, 1.0f, 1.0f, true, obj.mAngle);
    }

    @Override
    public boolean setPositionAndScale(AbsTag obj, MultiTouchController.PositionAndScale newObjPosAndScale, MultiTouchController.PointInfo touchPoint) {
        obj.setPosAndScale(newObjPosAndScale.getXOff(),
                            newObjPosAndScale.getYOff(),
                            newObjPosAndScale.getScale(),
                            newObjPosAndScale.getAngle());
        invalidate();
        return true;
    }

    @Override
    public void selectObject(AbsTag obj, MultiTouchController.PointInfo touchPoint) {
        if (obj != null) {
            this.mSelectedTag = obj;
        }
        invalidate();
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        mDeleteDrawable = getResources().getDrawable(R.drawable.delete);
        mEditDrawable = getResources().getDrawable(R.drawable.edit);
        this.mUIThread = Thread.currentThread();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Iterator localIterator = this.mTagList.iterator();
        while (localIterator.hasNext()) {
            ((AbsTag) localIterator.next()).draw(canvas);
        }

        if (mSelectedTag != null) {
            mSelectedTag.drawSelection(canvas, mDeleteDrawable, mEditDrawable);
        }
    }

    public void reset() {
        mTagList.clear();
        mSelectedTag = null;
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int boundWidth = Math.min(widthSize, heightSize);
        boundWidth = measureSize(widthMeasureSpec, boundWidth);
        int boundHeight = widthSize * 4 / 3;
        boundHeight = measureSize(heightMeasureSpec, boundHeight);
        setMeasuredDimension(boundWidth, boundHeight);
    }

    private int measureSize(int measureSpec, int defaultValue) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultValue;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
}
