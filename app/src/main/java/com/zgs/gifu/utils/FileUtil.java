package com.zgs.gifu.utils;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import com.zgs.gifu.constant.AppConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by zgs on 2016/12/1.
 */

public class FileUtil {

    public static File copyAssetsToSdcard(Context context, String type, String fileName) {
        String dir = Environment.getExternalStorageDirectory().getPath() + "/GifU/" + type;
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        String filePath = dir + "/" + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            return dirFile;
        } else {
            try {
                file.createNewFile();
                InputStream in = context.getResources().getAssets().open("" + type + "/" + fileName);
                int length = in.available();
                byte[] buf = new byte[length];
                in.read(buf);
                OutputStream out = new FileOutputStream(file);
                out.write(buf);
                out.flush();
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dirFile;
    }

    public static boolean notifyChange(Context context, String filePath) {
        if (filePath.isEmpty()) {
            return false;
        }
        File f = new File(filePath);

        if (f.exists()) {
            Uri uri = Uri.fromFile(f);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(uri);
            context.sendBroadcast(intent);
            return true;
        }
        return false;
    }


    public static String getName(int type) {
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat f = new SimpleDateFormat("yyMMdd-HHmmss", Locale.getDefault());
        String nowStr = f.format(now);
        //SharedPreferences settings = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        //String type = settings.getString(AppConstants.FILE_EXT, AppConstants.AS_GIF);
        String shareType = (type == AppConstants.AS_GIF) ? ".gif" : ".mp4";
        return AppConstants.GIF_TAG + nowStr + shareType;
    }

    public static String getPath(Context context, String fileName) {
        SharedPreferences settings = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        String dir = settings.getString(AppConstants.SAVE_PATH_NAME,
                Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.APP_NAME);
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }
        return dir + "/" + fileName;
    }

    public static boolean delete(Context context, String filePath) {
        if (isGif(filePath)) {
            return deleteImage(context, filePath);
        }
        return deleteVideo(context, filePath);
    }

    public static boolean isGif(String path) {
        return path != null && (path.endsWith(".gif") ||
                path.endsWith(".GIF") ||
                path.endsWith(".Gif"));
    }

    public static boolean isMP4(String path) {
        return path != null && (path.endsWith(".mp4") ||
                path.endsWith(".MP4") ||
                path.endsWith(".Mp4"));
    }

    private static boolean deleteVideo(Context context, String filePath) {
        if (filePath.isEmpty()) {
            return false;
        }

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        ContentResolver contentResolver = context.getContentResolver();
        String where = MediaStore.Video.Media.DATA + "='" + filePath + "'";

        int deleteRow = contentResolver.delete(uri, where, null);

        return deleteRow > 0;
    }

    private static boolean deleteImage(Context context, String filePath) {
        if (filePath.isEmpty()) {
            return false;
        }

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        ContentResolver contentResolver = context.getContentResolver();
        String where = MediaStore.Images.Media.DATA + "='" + filePath + "'";

        int deleteRow = contentResolver.delete(uri, where, null);

        return deleteRow > 0;
    }

    public static boolean share(Context context, String filter, String imagePath, String shareMsg) {
        Uri u = null;

        if (!TextUtils.isEmpty(imagePath)) {
            File f = new File(imagePath);
            if (!f.exists() || !f.isFile()) {
                return false;
            }
            u = Uri.fromFile(f);
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(filter);

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resInfo = pm.queryIntentActivities(intent, 0);

        if (!resInfo.isEmpty()) {

            List<LabeledIntent> targetShareIntents = new ArrayList<>();

            for (ResolveInfo info : resInfo) {
                String packageName = info.activityInfo.packageName;
                String className = info.activityInfo.name;

                if (className.equalsIgnoreCase("com.sina.weibo.composerinde.ComposerDispatchActivity") ||
                        className.equalsIgnoreCase("com.tencent.mm.ui.tools.ShareImgUI") ||
                        className.equalsIgnoreCase("com.tencent.mm.ui.tools.ShareToTimeLineUI") ||
                        className.equalsIgnoreCase("com.tencent.mobileqq.activity.JumpActivity")) {

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setComponent(new ComponentName(packageName, info.activityInfo.name));

                    shareIntent.setType(filter)
                            .putExtra(Intent.EXTRA_SUBJECT, "Share")
                            .putExtra(Intent.EXTRA_TEXT,
                                    !TextUtils.isEmpty(shareMsg) ? shareMsg : AppConstants.APP_NAME + " Share");

                    if (u != null) {
                        shareIntent.putExtra(Intent.EXTRA_STREAM, u);
                    }

                    targetShareIntents.add(new LabeledIntent(shareIntent, info.activityInfo.packageName, info.loadLabel(pm), info.icon));
                }
            }
            if (!targetShareIntents.isEmpty()) {
                Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Share via");
                if (chooserIntent == null) {
                    return false;
                }
                LabeledIntent[] extraIntents = targetShareIntents.toArray(new LabeledIntent[targetShareIntents.size()]);
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
                try {
                    context.startActivity(chooserIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(context, "Can't find share component to share!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        return true;
    }

    public static void share(Context context, Class target, String filePath) {
        Intent intent = new Intent();
        intent.setClass(context, target);
        Bundle mBundle = new Bundle();
        mBundle.putString("fileToShare", filePath);
        intent.putExtras(mBundle);
        context.startActivity(intent);
    }
}
