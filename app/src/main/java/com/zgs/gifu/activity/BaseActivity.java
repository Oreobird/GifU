package com.zgs.gifu.activity;

import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.VelocityTracker;

/**
 * Created by zgs on 2017/2/5.
 */

public class BaseActivity extends AppCompatActivity {
    private static final int YSPEED_MIN = 1000;
    private static final int XDISTANCE_MIN = 50;
    private static final int YDISTANCE_MIN = 100;
    private float xDown;
    private float yDown;

    private VelocityTracker mVelocityTracker;


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        createVelocityTracker(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDown = ev.getRawX();
                yDown = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float xMove = ev.getRawX();
                float yMove = ev.getRawY();

                int distanceX = (int) (xMove - xDown);
                int distanceY = (int) (yMove - yDown);

                int ySpeed = getScrollVelocity();

                if (distanceX > XDISTANCE_MIN &&
                        (distanceY < YDISTANCE_MIN && distanceY > -YDISTANCE_MIN) &&
                        ySpeed < YSPEED_MIN) {
                    finish();
                }
                break;
            case MotionEvent.ACTION_UP:
                recycleVelocityTracker();
                break;
            default:
                break;
        }
        
        return super.dispatchTouchEvent(ev);
    }

    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getYVelocity();
        return Math.abs(velocity);
    }

    private void createVelocityTracker(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }


}
