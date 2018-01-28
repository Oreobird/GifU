package com.zgs.gifu.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.zgs.gifu.utils.DisplayUtil;

public class CaptureMaskView extends android.support.v7.widget.AppCompatImageView {
	private Paint mTargetPaint;
	private Paint mMaskPaint;
	private int mScreenWidth, mScreenHeight;
	private Rect mTargetRect = null;
	
	public CaptureMaskView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
		Point p = DisplayUtil.getScreenMetrics(context);
		mScreenWidth = p.x;
		mScreenHeight = p.y;
	}

	private void initPaint() {
		mTargetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTargetPaint.setColor(Color.LTGRAY);
		mTargetPaint.setStyle(Style.STROKE);
		mTargetPaint.setStrokeWidth(3f);
		mTargetPaint.setAlpha(30);
		
		mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMaskPaint.setColor(0xff212121);
		mMaskPaint.setStyle(Style.FILL);
		mMaskPaint.setAlpha(200);
	}

	public void setTargetRect(Rect r){
		this.mTargetRect = r;
		postInvalidate();
	}

	public Rect getTargetRect() {
		return this.mTargetRect;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if (mTargetRect == null) {
			return;
		}
		/* draw mask area */
		canvas.drawRect(0, 0, mScreenWidth, mTargetRect.top, mMaskPaint);
		canvas.drawRect(0, mTargetRect.bottom + 1, mScreenWidth, mScreenHeight, mMaskPaint);
		canvas.drawRect(0, mTargetRect.top, mTargetRect.left - 1, mTargetRect.bottom + 1, mMaskPaint);
		canvas.drawRect(mTargetRect.right + 1, mTargetRect.top, mScreenWidth, mTargetRect.bottom + 1, mMaskPaint);
		
		/* draw target area */
		canvas.drawRect(mTargetRect, mTargetPaint);
		super.onDraw(canvas);
	}
}
