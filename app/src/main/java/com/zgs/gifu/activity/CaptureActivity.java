package com.zgs.gifu.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zgs.gifu.GifApplication;
import com.zgs.gifu.R;
import com.zgs.gifu.entity.FrameObj;
import com.zgs.gifu.entity.ImageObj;
import com.zgs.gifu.utils.CameraHelper;
import com.zgs.gifu.utils.DisplayUtil;
import com.zgs.gifu.utils.ImageUtil;
import com.zgs.gifu.utils.SettingUtil;
import com.zgs.gifu.utils.Utils;
import com.zgs.gifu.view.CaptureMaskView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class CaptureActivity extends BaseActivity implements SurfaceHolder.Callback,
        Camera.PreviewCallback {
    private static final String TAG = "**GifU**";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private CameraHelper mCameraHelper;
    private int mCameraId = 1;
    private int mScreenWidth;
    private int mScreenHeight;

    private ImageUtil mImageUtil;
    private CaptureMaskView mMaskView;
    private ImageButton flashBtn;
    private ImageButton switchCamBtn;
    private ImageButton recordBtn;
    //private ImageButton ratioBtn;
    private ImageView albumView;
    private TextSwitcher modeSwitcher;
    private ImageButton doneBtn;
    private ProgressBar proBar;

    private int captureMode = 0;	/* 0:auto, 1:manully */
    private int captureState = 1;	/* 0:capturing, 1:pause, 2:done */
    private int captureRatio = 1;   /* 0:1:1, 1:3:4 */
    private boolean flashOn = false;

    private Timer timer;
    private TimerTask timerTask;

    private GifApplication gifApp;
    private ImageObj mImageObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                       		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

		setContentView(R.layout.page_capture);

        gifApp = (GifApplication) this.getApplication();
        gifApp.clearFrameList(gifApp.getGifFrameList());
        mImageUtil = new ImageUtil();
        initView();
    }

    @Override
    public void onResume() {
        if (gifApp.getGifFrameList().size() <= 0) {
            proBar.setProgress(0);
        }

    	super.onResume();
        if (mCameraId == 1) {
            flashBtn.setVisibility(View.INVISIBLE);
        }
        if (captureMode == 0) {
            modeSwitcher.setText(getResources().getString(R.string.auto_mode));
        } else {
            modeSwitcher.setText(getResources().getString(R.string.manual_mode));
        }
        if (captureState == 1) {
            recordBtn.setImageResource(R.drawable.ic_stop);
        }
        if (flashOn) {
            flashOn = false;
            flashBtn.setImageResource(R.drawable.camera_flash_off);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            requestExternalStoragePermission();
        } else {
            loadData();
        }
    }

    private static class CaptureHandler extends Handler {
        private final WeakReference<CaptureActivity> mActivity;

        private CaptureHandler(CaptureActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CaptureActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 0:
                        activity.albumView.setImageResource(R.drawable.sample);
                        break;
                    case 1:
                        Glide.with(activity).load(activity.mImageObj.getPath())
                                .asBitmap()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .centerCrop()
                                .into(activity.albumView);
                        break;
                }
            }
        }
    }

    private CaptureHandler mHandler = new CaptureHandler(this);


    private void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mImageObj = Utils.getLastestImage(CaptureActivity.this);
                if (mImageObj == null) {
                    mHandler.sendEmptyMessage(0);
                } else {
                    mHandler.sendEmptyMessage(1);
                }
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mCameraHelper.closeCamera(mCamera);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (captureState == 0 && captureMode == 0) {
            stopTimer();
            switchCamBtn.setVisibility(View.VISIBLE);
            recordBtn.setImageResource(R.drawable.ic_start);

            captureState = 1;
        }
    }

    private void setRatioMask(int mode) {
        int[] flashPostion = new int[2];
        flashBtn.getLocationOnScreen(flashPostion);
        Rect screenCenterRect = createTargetRect(0, flashPostion[1], mode);

        mMaskView.setTargetRect(screenCenterRect);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mMaskView != null) {
            setRatioMask(captureRatio);
        }
    }

    private void initView() {
        flashBtn = (ImageButton) findViewById(R.id.capture_flash);
        switchCamBtn = (ImageButton) findViewById(R.id.capture_switch_cam);
        //ratioBtn = (ImageButton) findViewById(R.id.capture_ratio);
        modeSwitcher = (TextSwitcher) findViewById(R.id.capture_mode);
        recordBtn = (ImageButton) findViewById(R.id.capture_record);
        albumView = (ImageView) findViewById(R.id.capture_album);
        ImageButton closeBtn = (ImageButton) findViewById(R.id.capture_close);
        doneBtn = (ImageButton) findViewById(R.id.capture_done);
        proBar = (ProgressBar) findViewById(R.id.capture_proBar);
        mMaskView = (CaptureMaskView) findViewById(R.id.view_mask);

        modeSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView tv = new TextView(CaptureActivity.this);
                tv.setTextSize(20);
                tv.setTextColor(Color.WHITE);
                return tv;
            }
        });
        BtnListeners btnListener = new BtnListeners();
        flashBtn.setOnClickListener(btnListener);
        switchCamBtn.setOnClickListener(btnListener);
        //ratioBtn.setOnClickListener(btnListener);
        modeSwitcher.setOnClickListener(btnListener);
        albumView.setOnClickListener(btnListener);
        closeBtn.setOnClickListener(btnListener);
        recordBtn.setOnClickListener(btnListener);
        doneBtn.setOnClickListener(btnListener);


        mSurfaceView = (SurfaceView) findViewById(R.id.camera_view);

        if (Build.VERSION.SDK_INT >= 23) {
            requestCameraPermission();
        } else {
            initCamera();
        }
    }

    private void initCamera() {
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mCameraHelper = new CameraHelper(this);
        mCameraId = mCameraHelper.getFrontCameraId();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = mCameraHelper.openCamera(mCameraId);
        mCameraHelper.setParameters(mCamera, mScreenWidth, mScreenHeight);
        mCameraHelper.startPreview(mCamera, mSurfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //mCameraHelper.closeCamera(mCamera);   //close的动作放于onPause()，避免在快速切换其他app时无法调用camera
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        decodeYUV420SPAndRotate(data);
    }

    private void decodeYUV420SPAndRotate(byte[] data) {
        if (gifApp.getGifFrameList().size() < SettingUtil.getMaxFrames(this) && mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            //预览尺寸
            int preWidth = params.getPreviewSize().width;
            int preHeight = params.getPreviewSize().height;
            //计算preview size与screen size比例
            float hRatio =  (float) preWidth / (float) mScreenHeight;
            float wRatio = (float) preHeight / (float) mScreenWidth;

            Bitmap mBitmap;

            int[] dataArray = new int[data.length];
            mImageUtil.decodeYUV420SP(dataArray, data, preWidth, preHeight);
            mBitmap = Bitmap.createBitmap(dataArray, preWidth, preHeight, Bitmap.Config.ARGB_8888);

            if (mBitmap != null) {
                Matrix m = new Matrix();
                float degree;

                if (this.getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_PORTRAIT) {
                    degree = mCameraId == 0 ? 90 : 270;
                } else {
                    degree = mCameraId == 0 ? 0 : 0;
                }

                int targetWidth = (int) ((float) mMaskView.getTargetRect().width() * wRatio);
                int targetHeight = (int) ((float) mMaskView.getTargetRect().height() * hRatio);
                int deltaY = (int) ((float) (mScreenHeight - mMaskView.getTargetRect().bottom) * hRatio);
                int x;
                if (mCameraId == 0) {
                    x = (mBitmap.getWidth() - deltaY) / 2 - targetHeight / 2;
                } else {
                    x = (mBitmap.getWidth() + deltaY) / 2 - targetHeight / 2;  //前置摄像头，??
                }
                int y = mBitmap.getHeight() / 2 - targetWidth / 2;

                m.setRotate(degree, (float) mBitmap.getWidth() / 2, (float) mBitmap.getHeight() / 2);
                try {
                    mBitmap = Bitmap.createBitmap(mBitmap, x, y, targetHeight, targetWidth, m, true);
                    gifApp.getGifFrameList().add(new FrameObj(mBitmap));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class BtnListeners implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.capture_flash:
                    switchFlash();
                    break;
                case R.id.capture_switch_cam:
                    switchCamera();
                    break;
                //case R.id.capture_ratio:
                //    setRatio();
                //    break;
                case R.id.capture_mode:
                    setMode();
                    break;
                case R.id.capture_album:
                    goGallery();
                    break;
                case R.id.capture_close:
                    close();
                    break;
                case R.id.capture_record:
                    record();
                    break;
                case R.id.capture_done:
                    done();
                    break;
                default:
                    break;
            }
        }

        private void goGallery() {
            destroy();
            Intent intent = new Intent(CaptureActivity.this, AlbumActivity.class);
            startActivity(intent);
        }

        /*
        private void setRatio() {
            if (captureRatio == 0) {
                captureRatio = 1;
                ratioBtn.setImageResource(R.drawable.btn_mode_manual_selector);

            } else if (captureRatio == 1) {
                captureRatio = 0;
                ratioBtn.setImageResource(R.drawable.btn_mode_auto_selector);
            }
            setRatioMask(captureRatio);
        }
        */
        private void switchFlash() {
            if (mCameraHelper != null) {
                if (flashOn) {
                    mCameraHelper.turnFlashOff(mCamera);
                    flashOn = false;
                    flashBtn.setImageResource(R.drawable.camera_flash_off);
                } else {
                    mCameraHelper.turnFlashOn(mCamera);
                    flashOn = true;
                    flashBtn.setImageResource(R.drawable.camera_flash_on);
                }
            }
        }

        private void switchCamera() {
            if (mCameraId == 1) {
                /* switch front to back */
                mCameraId = mCameraHelper.getBackCameraId();
                flashBtn.setVisibility(View.VISIBLE);

            } else {
                /* switch back to front */
                mCameraId = mCameraHelper.getFrontCameraId();
                flashBtn.setVisibility(View.INVISIBLE);
                if (flashOn) {
                    flashOn = false;
                    flashBtn.setImageResource(R.drawable.camera_flash_off);
                }
            }

            mCameraHelper.closeCamera(mCamera);
            mCamera = mCameraHelper.openCamera(mCameraId);
            mCameraHelper.setParameters(mCamera, mScreenWidth, mScreenHeight);
            mCameraHelper.startPreview(mCamera, mSurfaceHolder);
        }
    }

    private Rect createTargetRect(int widthMargin, int baseLine, int mode){
        int screenWidth =  DisplayUtil.getScreenMetrics(CaptureActivity.this).x;
        int targetWidth = screenWidth - 2 * widthMargin;
        int targetHeight = 0;
        Rect targetRect = new Rect();

        targetRect.left = widthMargin;
        targetRect.right = screenWidth - widthMargin;
        switch (mode) {
            case 0:	/* 1:1 */
                targetHeight = targetWidth;
                targetRect.top = (baseLine - targetHeight) / 2;
                break;
            case 1: /* 4:3 */
                targetHeight = targetWidth / 3 * 4;
                if (targetHeight > baseLine) {
                    targetHeight = baseLine;
                    targetWidth = targetHeight * 4 / 3;
                    if (targetWidth < screenWidth) {
                        int margin = (screenWidth - targetWidth) / 2;
                        targetRect.left = margin;
                        targetRect.right = screenWidth - margin;
                    }
                }
                targetRect.top = 0;
                break;
        }

        targetRect.bottom = targetRect.top + targetHeight;

        return targetRect;
    }


    private void setMode() {
        if (captureMode == 0) {
            captureMode = 1;
            modeSwitcher.setText(getResources().getString(R.string.manual_mode));
        } else {
            captureMode = 0;
            modeSwitcher.setText(getResources().getString(R.string.auto_mode));
        }
    }

    private void close() {
        destroy();
        if (gifApp.getGifFrameList().size() <= 0) {
            finish();
        } else {
            gifApp.clearFrameList(gifApp.getGifFrameList());
        }
    }

    private void done() {
        destroy();
        if (!gifApp.getGifFrameList().isEmpty()) {
            Intent intent = new Intent(CaptureActivity.this, GifViewActivity.class);
            startActivity(intent);
        }
    }

    private void destroy() {
        stopTimer();
        captureState = 1;
        proBar.setProgress(0);
        if (flashOn) {
            mCameraHelper.turnFlashOff(mCamera);
            flashOn = false;
            flashBtn.setImageResource(R.drawable.camera_flash_off);
        }
        switchCamBtn.setVisibility(View.VISIBLE);
        doneBtn.setVisibility(View.INVISIBLE);
        modeSwitcher.setVisibility(View.VISIBLE);
        recordBtn.setImageResource(R.drawable.ic_stop);
    }

    private void record() {

        modeSwitcher.setVisibility(View.INVISIBLE);
        doneBtn.setVisibility(View.VISIBLE);
        proBar.setMax(SettingUtil.getMaxFrames(CaptureActivity.this));

        if (captureMode == 0) { /* auto mode */
            if (captureState == 0) { /* capture to pause */
                captureState = 1;
                switchCamBtn.setVisibility(View.VISIBLE);
                recordBtn.setImageResource(R.drawable.ic_stop);
                stopTimer();
            } else { /* pause to capture */
                captureState = 0;
                switchCamBtn.setVisibility(View.INVISIBLE);
                recordBtn.setImageResource(R.drawable.ic_start);
                startTimer();
            }
        } else {
            recordBtn.setImageResource(R.drawable.ic_stop);
            captureOnce();
            if (proBar.getProgress() == proBar.getMax()) {
                done();
            }
            recordBtn.setImageResource(R.drawable.ic_start);
        }

    }

    private void captureOnce() {
        try {
            if (mCamera != null) {
                mCamera.setOneShotPreviewCallback(this);
                proBar.incrementProgressBy(1);
            }
        } catch (Exception e) {

        }
    }

    private void startTimer() {
        if (timer == null) {
            timer = new Timer();
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (gifApp.getGifFrameList().size() < SettingUtil.getMaxFrames(CaptureActivity.this)) {
                    captureOnce();
                } else {
                    timer.cancel();
                    timer = null;
                    /*
                    Message msg = new Message();
                    msg.what = 0;
                    captureHandler.sendMessage(msg);
                    */
                }
            }
        };
        timer.schedule(timerTask, 100, 300);  // faster cause unmatch gif size and progress, use jni to decode YUV
    }
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private static final int REQUEST_CAMERA_PERMISSION = 0;
    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1;

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            initCamera();
        }
    }
    private void requestExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE_PERMISSION);
        } else {
            loadData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "您没有授权开启摄像头权限，请在设置中打开授权", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                initCamera();
            }
            return;
        } else if (requestCode == REQUEST_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "您没有授权开启读SD卡权限，请在设置中打开授权", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                loadData();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
