package com.zgs.gifu.imageprocess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;

import com.zgs.gifu.constant.AppConstants;
import com.zgs.gifu.entity.FrameObj;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBulgeDistortionFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageCrosshatchFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageEmbossFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageExposureFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageGaussianBlurFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGlassSphereFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageLaplacianFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageMonochromeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSketchFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSobelEdgeDetection;
import jp.co.cyberagent.android.gpuimage.GPUImageSwirlFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageWeakPixelInclusionFilter;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;

public class ImageFilter {

	public ImageFilter(Context context) {

	}


	private static GPUImageFilter getFilter(int filter) {
		switch (filter) {
			case AppConstants.SOBEL_EDGE:
				return new GPUImageSobelEdgeDetection();
			case AppConstants.GAUSSIAN_BLUR:
				return new GPUImageGaussianBlurFilter();
			case AppConstants.EXPOSURE:
				return new GPUImageExposureFilter();
			case AppConstants.SHARPEN:
				return new GPUImageSharpenFilter(2f);
			case AppConstants.MONOCHROME:
				return new GPUImageMonochromeFilter();
			case AppConstants.GRAYSCALE:
				return new GPUImageGrayscaleFilter();
			case AppConstants.GLASSSPHERE:
				return new GPUImageGlassSphereFilter();
			case AppConstants.COLORINVERT:
				return new GPUImageColorInvertFilter();
			case AppConstants.CONTRAST:
				return new GPUImageContrastFilter(2f);
			case AppConstants.BRIGHTNESS:
				return new GPUImageBrightnessFilter(0.5f);
			case AppConstants.BULGE_DISTORTION:
				return new GPUImageBulgeDistortionFilter(0.5f, 0.5f, new PointF(0.5f, 0.5f));
			case AppConstants.CROSSHATCH:
				return new GPUImageCrosshatchFilter();
			case AppConstants.EMBOSS:
				return new GPUImageEmbossFilter();
			case AppConstants.LAPLACIAN:
				return new GPUImageLaplacianFilter();
			case AppConstants.SATURATION:
				return new GPUImageSaturationFilter(10f);
			case AppConstants.SKETCH:
				return new GPUImageSketchFilter();
			case AppConstants.SWIRL:
				return new GPUImageSwirlFilter();
			case AppConstants.WEAK_PIXEL_INCLUSION:
				return new GPUImageWeakPixelInclusionFilter();
			case AppConstants.SIMPLE_EDGE_DETECTION:
				GPUImageFilterGroup fg = new GPUImageFilterGroup();
				fg.addFilter(new GPUImageGaussianBlurFilter());
				fg.addFilter(new GPUImageContrastFilter(2f));
				fg.addFilter(new GPUImageWeakPixelInclusionFilter());
				fg.addFilter(new GPUImageSobelEdgeDetection());
				return fg;
		}
		return null;
	}

	public static Bitmap filterApply(GPUImage gpuImage, Bitmap bitmap, int filter) {
		if (gpuImage != null && bitmap != null && filter == AppConstants.NONE) {
			return bitmap;
		}

		if (gpuImage != null) {
			gpuImage.setImage(bitmap);
			gpuImage.setFilter(getFilter(filter));
			bitmap = gpuImage.getBitmapWithFilterApplied();
		}
		return bitmap;
	}

	public static FrameObj[] filterApplyAll(GPUImage gpuImage, FrameObj[] frames, int filter) {
		if (gpuImage != null && frames != null && filter == AppConstants.NONE) {
			return frames;
		}
		FrameObj[] newFrames = null;

		if (frames != null) {
			newFrames = new FrameObj[frames.length];
			for (int i = 0; i < frames.length; i++) {
				newFrames[i] = new FrameObj(filterApply(gpuImage, frames[i].getBitmap(), filter));
			}
		}
		return newFrames;
	}

	public static Bitmap IplImageToBitmap(IplImage iplImage) {
		Bitmap bitmap;
		bitmap = Bitmap.createBitmap(iplImage.width(), iplImage.height(),
				Bitmap.Config.ARGB_8888);
		bitmap.copyPixelsFromBuffer(iplImage.getByteBuffer());
		return bitmap;
	}

	public static opencv_core.IplImage bitmapToIplImage(Bitmap bitmap) {
		IplImage iplImage;
		iplImage = IplImage.create(bitmap.getWidth(), bitmap.getHeight(), IPL_DEPTH_8U, 4);
		bitmap.copyPixelsToBuffer(iplImage.getByteBuffer());
		return iplImage;
	}

	public static int getMatElement(Mat img,int row,int col,int channel){
		//获取字节指针
		BytePointer bytePointer = img.ptr(row, col);
		int value = bytePointer.get(channel);
		if(value<0){
			value=value+256;
		}
		return value;
	}
	public static void setMatElement(Mat img,int row,int col, int channel, byte val){
		//获取字节指针
		BytePointer bytePointer = img.ptr(row, col);
		bytePointer.put(channel, val);
	}

}
