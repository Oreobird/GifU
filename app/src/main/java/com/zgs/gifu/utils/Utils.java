package com.zgs.gifu.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import com.zgs.gifu.constant.AppConstants;
import com.zgs.gifu.entity.ImageObj;

import java.util.ArrayList;
import java.util.List;

public class Utils {

	public static boolean containObj(List<ImageObj> imgList, ImageObj imgObj) {
		return !(imgList == null || imgList.size() == 0
				|| imgObj == null || imgList.indexOf(imgObj) == -1);
	}

	public static List<ImageObj> getImages(Context context) {
		List<ImageObj> list = new ArrayList<>();
		
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		
		String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
				MediaStore.Images.Media.DATE_ADDED};
		String sortOrder = MediaStore.Images.Media.DATE_ADDED + " desc";

		Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);
		if (cursor != null) {
			int iId = cursor.getColumnIndex(MediaStore.Images.Media._ID);
			int iPath = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
			int iDate = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				String id = cursor.getString(iId);
				String path = cursor.getString(iPath);
				String date = cursor.getString(iDate);
				//Log.e("getImages", "-----gif path: " + path);
				ImageObj imageObj = new ImageObj(id, path, date);
				list.add(imageObj);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return list;
	}

	public static List<ImageObj> getImages(Context context, int type) {
		List<ImageObj> list = new ArrayList<>();

		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
								MediaStore.Images.Media.DATE_ADDED};
		String sortOrder = MediaStore.Images.Media.DATE_ADDED + " desc";

		Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);
		if (cursor != null) {
			int iId = cursor.getColumnIndex(MediaStore.Images.Media._ID);
			int iPath = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
			int iDate = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				String id = cursor.getString(iId);
				String path = cursor.getString(iPath);
				String date = cursor.getString(iDate);

				if (FileUtil.isGif(path)) {
					//Log.e("getImages", "-----gif path: " + path);
					ImageObj imageObj;
					switch (type) {
						case AppConstants.ALL_GIF:
							imageObj = new ImageObj(id, path, date);
							list.add(imageObj);
							break;
						case AppConstants.APP_GIF:
							if (path.contains(AppConstants.GIF_TAG)) {
								imageObj = new ImageObj(id, path, date);
								list.add(imageObj);
							}
							break;
						default:
							break;
					}

				}
				cursor.moveToNext();
			}
			cursor.close();
		}
		return list;
	}

	public static Bitmap getVidioThumbnail(String filePath, int width, int height,
								 int kind) {
		Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	public static List<ImageObj> getVideos(Context context, int type) {
		List<ImageObj> list = new ArrayList<>();

		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

		String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA,
				MediaStore.Video.Media.DATE_ADDED};
		String sortOrder = MediaStore.Video.Media.DATE_ADDED + " desc";

		Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);
		if (cursor != null) {
			int iId = cursor.getColumnIndex(MediaStore.Video.Media._ID);
			int iPath = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
			int iDate = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				String id = cursor.getString(iId);
				String path = cursor.getString(iPath);
				String date = cursor.getString(iDate);

				if (FileUtil.isMP4(path)) {
					//Log.e("getImages", "-----gif path: " + path);
					ImageObj imageObj;
					switch (type) {
						case AppConstants.ALL_GIF:
							imageObj = new ImageObj(id, path, date);
							list.add(imageObj);
							break;
						case AppConstants.APP_GIF:
							if (path.contains(AppConstants.GIF_TAG)) {
								imageObj = new ImageObj(id, path, date);
								list.add(imageObj);
							}
							break;
						default:
							break;
					}

				}
				cursor.moveToNext();
			}
			cursor.close();
		}
		return list;
	}

	public static ImageObj getLastestImage(Context context) {
		ImageObj lastestImageObj = null;

		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
				MediaStore.Images.Media.DATE_ADDED};
		String sortOrder = MediaStore.Images.Media.DATE_ADDED + " desc";

		Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);
		if (cursor != null) {
			int iId = cursor.getColumnIndex(MediaStore.Images.Media._ID);
			int iPath = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
			int iDate = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				String id = cursor.getString(iId);
				String path = cursor.getString(iPath);
				String date = cursor.getString(iDate);
				if (path != null && (path.endsWith(".gif") || path.endsWith(".GIF"))) {
					if (path.contains(AppConstants.GIF_TAG)) {
						lastestImageObj = new ImageObj(id, path, date);
						break;
					}
				}
				cursor.moveToNext();
			}
			cursor.close();
		}
		return lastestImageObj;
	}

	public static int calculateBitmapSize(Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		
		if (height > reqHeight || width > reqWidth) {
			while ((height / inSampleSize) > reqHeight
					&& (width / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}
	
	public static int computeSampleSize(Options options,
	        int minSideLength, int maxNumOfPixels) {
	    int initialSize = computeInitialSampleSize(options, minSideLength,
	            maxNumOfPixels);
	    int roundedSize;
	    if (initialSize <= 8) {
	        roundedSize = 1;
	        while (roundedSize < initialSize) {
	            roundedSize <<= 1;
	        }
	    } else {
	        roundedSize = (initialSize + 7) / 8 * 8;
	    }

	    return roundedSize;
	}


	private static int computeInitialSampleSize(Options options,
	        int minSideLength, int maxNumOfPixels) {
	    double w = options.outWidth;
	    double h = options.outHeight;
	    int lowerBound = (maxNumOfPixels == -1) ? 1 :
	            (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
	    int upperBound = (minSideLength == -1) ? AppConstants.GIF_WIDTH :
	            (int) Math.min(Math.floor(w / minSideLength),
	            Math.floor(h / minSideLength));

	    if (upperBound < lowerBound) {
	        // return the larger one when there is no overlapping zone.
	        return lowerBound;
	    }

	    if ((maxNumOfPixels == -1) &&
	            (minSideLength == -1)) {
	        return 1;
	    } else if (minSideLength == -1) {
	        return lowerBound;
	    } else {
	        return upperBound;
	    }
	}

	public static String getVersion(Context context) {

		PackageInfo packageInfo;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return "1.0";
	}

}
