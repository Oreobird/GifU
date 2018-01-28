package com.zgs.gifu.entity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import org.bytedeco.javacpp.opencv_core;

import java.util.LinkedList;
import java.util.List;

import static com.zgs.gifu.imageprocess.ImageFilter.IplImageToBitmap;
import static com.zgs.gifu.imageprocess.ImageFilter.bitmapToIplImage;
import static com.zgs.gifu.imageprocess.ImageFilter.getMatElement;
import static com.zgs.gifu.imageprocess.ImageFilter.setMatElement;
import static org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_RGBA2BGR;
import static org.bytedeco.javacpp.opencv_imgproc.GC_BGD;
import static org.bytedeco.javacpp.opencv_imgproc.GC_PR_BGD;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.grabCut;

/**
 * Created by zgs on 2017/3/10.
 */

public class ThreeDObj {
    private Bitmap mBoarder;
    private FrameObj[] mFrameArray;

    private boolean cut;
    private List<Mat> mCutFrameList;
    private int frameNum;
    private int width;
    private int height;

    public ThreeDObj(FrameObj[] mFrameArray, Bitmap mBoarder) {
        this.mFrameArray = mFrameArray;
        this.mBoarder = mBoarder;

        this.mCutFrameList = new LinkedList<>();
        this.frameNum = mFrameArray.length;
        this.width = mFrameArray[0].getBitmap().getWidth();
        this.height = mFrameArray[0].getBitmap().getHeight();
    }

    public boolean isCut() {
        return cut;
    }

    public Bitmap getBoarder() {
        return mBoarder;
    }

    public ThreeDObj setBoarder(Bitmap mBoarder) {
        this.mBoarder = mBoarder;
        return this;
    }

    public FrameObj[] getFrameList() {
        return mFrameArray;
    }

    public ThreeDObj setFrameList(FrameObj[] mFrameArray) {
        this.mFrameArray = mFrameArray;
        return this;
    }

    public List<opencv_core.Mat> getCutFrameList() {
        return mCutFrameList;
    }

    private void cut() {
        IplImage tmpFrame = bitmapToIplImage(mFrameArray[0].getBitmap());
        Mat cutFrame = new Mat(tmpFrame);   //IplImage to Mat
        Mat grayFrame = new Mat();
        cvtColor(cutFrame, grayFrame, COLOR_RGBA2BGR);

        Mat mask = new Mat(), bg = new Mat(), fg = new Mat();

        grabCut(grayFrame, mask,
                new opencv_core.Rect(1, 1, grayFrame.cols() - 2, grayFrame.rows() - 2),
                bg, fg,
                5, 0);

        for (int i = 0; i < frameNum; i++) {
            tmpFrame = bitmapToIplImage(mFrameArray[i].getBitmap());
            cutFrame = new Mat(tmpFrame);
            grayFrame = new Mat();
            cvtColor(cutFrame, grayFrame, COLOR_RGBA2BGR);

            grabCut(grayFrame, mask,
                    new opencv_core.Rect(1, 1, grayFrame.cols() - 2, grayFrame.rows() - 2),
                    bg, fg,
                    1, 1);

            int nrow = grayFrame.rows();
            int ncol = grayFrame.cols();
            for(int m = 0; m < nrow; m++) {
                for (int n = 0; n < ncol; n++) {
                    int val = getMatElement(mask, m, n, 0);
                    //Log.e("---------", "val:"+val);
                    if (val == GC_PR_BGD || val == GC_BGD) {
                        for (int k = 0; k < cutFrame.channels(); k++) {
                            setMatElement(cutFrame, m, n, k, (byte) 0);
                        }
                    }
                }
            }
            mCutFrameList.add(cutFrame);
        }
    }

    private FrameObj[] draw3D() {
        FrameObj[] newFrames = new FrameObj[frameNum];
        Rect dst = new Rect(0, 0, width, height);
        Bitmap mOut = null;
        Canvas canvas = null;

        for (int i = 0; i < frameNum; i++) {
            newFrames[i] = mFrameArray[i].clone();
            mOut = newFrames[i].getBitmap();
            canvas = new Canvas(mOut);
            canvas.drawBitmap(mBoarder, null, dst, null);
            if (i > frameNum / 2) {
                canvas.drawBitmap(IplImageToBitmap(new IplImage(mCutFrameList.get(i))), 0, 0, null);
            }
            newFrames[i].setBitmap(mOut);
        }
        return newFrames;
    }

    public FrameObj[] apply3D() {
        if (mCutFrameList == null || mCutFrameList.size() <= 0) {
            cut();
            cut = true;
        }
        return draw3D();
    }

    public void destroy() {
        if (mBoarder != null) {
            this.mBoarder.recycle();
        }
        if (mCutFrameList != null) {
            mCutFrameList.clear();
        }
    }
}
