package com.zgs.gifu.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.zgs.gifu.constant.AppConstants;
import com.zgs.gifu.entity.FrameObj;

public class GifPlayView extends android.support.v7.widget.AppCompatImageView {

	private FrameObj[] mFrames;
	private boolean isRunning;
	private int speed;
	private int order;
	private int curIndex;
	private Thread gifPlayThread;
	private Paint paint;
	private Rect dst;
	private int boundHeight = 1440;
	private int boundWidth = 1080;

	public Thread getGifPlayThread() {
		return gifPlayThread;
	}

	public void setGifPlayThread(Thread gifPlayThread) {
		this.gifPlayThread = gifPlayThread;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setmFrames(FrameObj[] mFrames) {
		this.mFrames = mFrames;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = 1000 / speed;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public GifPlayView(Context context) {
		super(context);
	}
	
	public GifPlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	 
	public void initData(FrameObj[] frames, int speed, int order) {
		setmFrames(frames);
		setSpeed(speed);
		setOrder(order);
		dst = new Rect(0, 0, boundWidth, boundHeight);
	}
	
	public void gifPlay() {
		if (mFrames != null && mFrames.length > 0) {
			gifPlayThread = new Thread(new gifPlayRunnable());
			isRunning = true;
			gifPlayThread.start();
		}
	}
	
	public void gifStop() {
		isRunning = false;
	}

	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		gifStop();
	}

	private class gifPlayRunnable implements Runnable {

		@Override
		public void run() {
			while (isRunning) {
				if (order == AppConstants.FORWARD) {
					curIndex = (curIndex + 1) % mFrames.length;
				} else {
					curIndex = (curIndex - 1) % mFrames.length;
					if (curIndex < 0) {
						curIndex = mFrames.length - 1;
					}
				}
				//Log.d("gifmanager", "curIndex:"+curIndex);
				//Log.d("gifmanager", "speed:"+speed);
				GifPlayView.this.postInvalidate();
				try {
					Thread.sleep(speed);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	 
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		//Log.d("gifmanager", "onDraw: l:"+dst.left+" r:"+dst.right+" t:"+dst.top+" b:"+dst.bottom);
		if (mFrames != null && curIndex <= mFrames.length - 1 && curIndex >= 0) {
			canvas.drawBitmap(mFrames[curIndex].getBitmap(), null, dst, paint);
		}
	}

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		boundWidth = Math.min(widthSize, heightSize);
		boundWidth = measureSize(widthMeasureSpec, boundWidth);
		boundHeight = widthSize * 4 / 3;
		boundHeight = measureSize(heightMeasureSpec, boundHeight);
        setMeasuredDimension(boundWidth, boundHeight);
    }

	private int measureSize(int measureSpec, int defaultValue) {
		int result;
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
