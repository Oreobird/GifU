package com.zgs.gifu.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.Toast;

import com.zgs.gifu.GifApplication;
import com.zgs.gifu.R;
import com.zgs.gifu.constant.AppConstants;
import com.zgs.gifu.entity.FrameObj;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import pl.droidsonroids.gif.GifDrawable;

public class GifUtil {

	private Context mContext;
	private String mPath;

	public GifUtil(Context context) {
		this.mContext = context;
	}

	public static GifUtil with(Context context) {
		return new GifUtil(context);
	}

	public GifUtil path(String path) {
		this.mPath = path;
		return this;
	}

	private static Bitmap zoomBitmap(Bitmap bitmap, int gifwidth, int gifheight, boolean recycle){
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float)gifwidth / width);
		float scaleHeight = ((float)gifheight / height);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		if (recycle) {
			bitmap.recycle();
		}
		return newbmp;
	}
	
	public static Bitmap resizeBitmapByCenterCrop1(Bitmap src,
					int destWidth, int destHeight, boolean recycle) {
	    if (src == null || destWidth == 0 || destHeight == 0) {
	        return null;
	    }
	    
	    int srcWidth = src.getWidth();
	    int srcHeight = src.getHeight();
	    Bitmap newBmp = src;
	    if (srcWidth < srcHeight) {
    		int cropWidth = srcHeight - srcWidth;
    		newBmp = Bitmap.createBitmap(src, 0, cropWidth / 2, srcWidth, srcWidth);
    	} else if (srcWidth > srcHeight) {
    		int cropHeight = srcWidth - srcHeight;
    		newBmp = Bitmap.createBitmap(src, cropHeight/2, 0, srcHeight, srcHeight);
    	}
	    if (recycle && newBmp != null) {
	    	src.recycle();
	    }
	    return zoomBitmap(newBmp, destWidth, destHeight, recycle);
	}

	public static Bitmap resizeBitmapByCenterCrop(Bitmap src,
												  int destWidth, int destHeight, boolean recycle) {
		if (src == null || destWidth == 0 || destHeight == 0) {
			return null;
		}

		int srcWidth = src.getWidth();
		int srcHeight = src.getHeight();
		Bitmap newBmp = src;
		if ((float) srcWidth / srcHeight != 0.75) {
			if (srcWidth <= srcHeight) {
				int cropHeight = srcHeight - srcWidth;
				newBmp = Bitmap.createBitmap(src, srcWidth / 8, cropHeight / 2, srcWidth * 3 / 4, srcWidth);
			} else if (srcWidth > srcHeight) {
				int cropWidth = srcWidth - srcHeight * 3 / 4;
				newBmp = Bitmap.createBitmap(src, cropWidth / 2, 0, srcHeight * 3 / 4, srcHeight);
			}
		}
		if (recycle && newBmp != null) {
			src.recycle();
		}
		return zoomBitmap(newBmp, destWidth, destHeight, recycle);
	}

	public static void createGif(String filePath, FrameObj[] frames, int order, int fps,
									int width, int height) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnimatedGifEncoder localAnimatedGifEncoder = new AnimatedGifEncoder();
        localAnimatedGifEncoder.start(baos);//start
        localAnimatedGifEncoder.setRepeat(0);//set gif start play time, 0: start now
        localAnimatedGifEncoder.setDelay(fps);
		if (order == AppConstants.FORWARD) {
			for (int i = 0; i < frames.length; i++) {
				Bitmap zoomBmp = resizeBitmapByCenterCrop(frames[i].getBitmap(), width, height, false);
				localAnimatedGifEncoder.addFrame(zoomBmp);
			}
		} else {
			for (int i = frames.length - 1; i >= 0; i--) {
				Bitmap zoomBmp = resizeBitmapByCenterCrop(frames[i].getBitmap(), width, height, false);
				localAnimatedGifEncoder.addFrame(zoomBmp);
			}
		}

        localAnimatedGifEncoder.finish();//finish

        FileOutputStream fos = new FileOutputStream(filePath);
        baos.writeTo(fos);
        baos.flush();
        fos.flush();
        baos.close();
        fos.close();

	}

	public static void createVideo(String filePath, FrameObj[] frames, int order, int fps,
								 int width, int height) throws IOException {
		MP4VideoEncoder encoder = new MP4VideoEncoder(new File(filePath), fps);
		if (order == AppConstants.FORWARD) {
			for (int i = 0; i < frames.length; i++) {
				Bitmap zoomBmp = resizeBitmapByCenterCrop(frames[i].getBitmap(), width, height, false);
				encoder.encodeFrame(zoomBmp);
				if (i == 0 || i == frames.length - 1) {
					encoder.encodeFrame(zoomBmp);
				}
			}
		} else {
			for (int i = frames.length - 1; i >= 0; i--) {
				Bitmap zoomBmp = resizeBitmapByCenterCrop(frames[i].getBitmap(), width, height, false);
				encoder.encodeFrame(zoomBmp);
				if (i == 0 || i == frames.length - 1) {
					encoder.encodeFrame(zoomBmp);
				}
			}
		}
		encoder.finish();
	}

	public int getGifFramesNum(String path) {
		int frames = 0;
		GifDrawable mGifPlaying;
		try {
			mGifPlaying = new GifDrawable(path);
			if (mGifPlaying != null) {
				frames = mGifPlaying.getNumberOfFrames();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return frames;
	}

	public ArrayList<Bitmap> decodeGif(String path) {
		GifDrawable mGifPlaying;
		ArrayList<Bitmap> bitmapList = new ArrayList<>();
		try {
			mGifPlaying = new GifDrawable(path);
			if (mGifPlaying != null) {
				// Need to do in task or not? TODO
				for (int i = 0; i < mGifPlaying.getNumberOfFrames(); i++) {
					Bitmap bitmap = mGifPlaying.seekToFrameAndGet(i);
					bitmapList.add(bitmap);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmapList;
	}

	public void doEdit(Intent intent, GifApplication mGifApp) {
		if (mGifApp != null && mGifApp.getGifFrameList().size() > 0) {
			mGifApp.clearFrameList(mGifApp.getGifFrameList());
		}

		ArrayList<Bitmap> bitmapList = decodeGif(mPath);
		for (int i = 0; i < bitmapList.size(); i++) {
			if (mGifApp != null) {
				mGifApp.getGifFrameList().add(new FrameObj(bitmapList.get(i)));
			}
		}

		mContext.startActivity(intent);
	}


	public void doShare() {
		String media = (FileUtil.isGif(mPath)) ? "image/gif" : "video/mp4";
		FileUtil.share(mContext, media, mPath, null);
	}


	public void doDelete(int position) {
		boolean isSuccess = FileUtil.delete(mContext, mPath);
		if (isSuccess) {
			if (mUpdateDataStrategy != null) {
				mUpdateDataStrategy.update(position);
			}

			Toast.makeText(mContext,
					mContext.getResources().getString(R.string.file_delete_success),
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mContext,
					mContext.getResources().getString(R.string.file_delete_failed),
					Toast.LENGTH_SHORT).show();
		}
	}

	public interface UpdateDataStrategy {
		void update(int position);
	}

	public GifUtil updateDataStrategy(GifUtil.UpdateDataStrategy mUpdateDataSet) {
		this.mUpdateDataStrategy = mUpdateDataSet;
		return this;
	}

	private GifUtil.UpdateDataStrategy mUpdateDataStrategy;

}