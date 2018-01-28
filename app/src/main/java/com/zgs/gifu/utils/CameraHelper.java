package com.zgs.gifu.utils;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.util.List;

/**
 * Created by zgs on 2017/2/22.
 */
public class CameraHelper {
    private static final String TAG = "[CameraHelper]";
    private int mBackCameraId;
    private int mFrontCameraId;
    private Context mContext;
    private static int mPictureWidth = 1280;
    private static int mPictureHeigth = 720;

    public CameraHelper(Context context) {
        this.mContext = context;
        int mCameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mCameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCameraId = i;
            }
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mBackCameraId = i;
            }
        }
        if (mFrontCameraId == -1) {
            mFrontCameraId = 0;
        }
    }

    public int getFrontCameraId() {
        return mFrontCameraId;
    }

    public int getBackCameraId() {
        return mBackCameraId;
    }

    public Camera openCamera(int cameraId) {
        Camera camera = null;
        try {
            camera = Camera.open(cameraId);
        } catch (Exception e) {
            return null;
        }

        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();

            if (parameters != null) {
                List<String> list = parameters.getSupportedFocusModes();
                if (list.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }

                int min = parameters.getMinExposureCompensation();
                int max = parameters.getMaxExposureCompensation();
                if (min < 1 && max >= 1) {
                    parameters.setExposureCompensation(1);
                }
                // parameters.setPreviewFpsRange(30000, 60000);
                int displayRotation = getCameraDisplayOrientation(cameraId);
                camera.setDisplayOrientation(displayRotation);
                camera.setParameters(parameters);
            }
        }
        return camera;
    }

    public void closeCamera(Camera camera) {
        if (camera != null) {
            try {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setParameters(Camera camera, int previewWidth, int previewHeight) {
        if (camera != null) {
            Camera.Parameters parameters;
            parameters = camera.getParameters();

            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            Camera.Size optimalSize = getOptimalPreviewSize(sizes, previewWidth, previewHeight);
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);

            sizes = parameters.getSupportedPictureSizes();
            Camera.Size pictureSize = getOptimalPreviewSize(sizes, mPictureWidth, mPictureHeigth);
            mPictureWidth = pictureSize.width;
            mPictureHeigth = pictureSize.height;
            parameters.setPictureSize(mPictureWidth, mPictureHeigth);

            parameters.setPictureFormat(ImageFormat.JPEG);

            camera.setParameters(parameters);
        }
    }

    public void startPreview(Camera camera, SurfaceHolder holder) {
        if (camera != null && holder != null) {
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (Exception e){
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    private int getCameraDisplayOrientation(final int cameraId) {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);

        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private void configFlashLight(Camera camera, boolean flashOn) {
        if (camera == null) {
            return;
        }

        Camera.Parameters parameters = camera.getParameters();

        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (flashModes == null) {
            // Use the screen as a flashlight (next best thing)
            return;
        }
        String flashMode = parameters.getFlashMode();

        if (flashOn) {
            if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                // Turn on the flash
                try {
                    if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        camera.setParameters(parameters);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "FLASH_MODE_TORCH not supported");
                }
            }
        } else {
            if (!Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
                try {
                    if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        camera.setParameters(parameters);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "FLASH_MODE_OFF not supported");
                }
            }
        }
    }
    public void turnFlashOn(Camera camera) {
        if (camera != null) {
            configFlashLight(camera, true);
        }
    }

    public void turnFlashOff(Camera camera) {
        if (camera != null) {
            configFlashLight(camera, false);
        }
    }
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
