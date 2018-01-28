package com.zgs.gifu.utils;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.taobao.android.SophixManager;
import com.taobao.android.listener.PatchLoadStatusListener;
import com.taobao.android.util.PatchStatus;
import com.zgs.gifu.constant.AppConstants;

/**
 * Created by zgs on 2017/1/13.
 */

public class SettingUtil {

    public static int getMaxFrames(Context context) {
        SharedPreferences settings = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt(AppConstants.MAX_FRAME_NAME, AppConstants.DEFAULT_MAX_FRAME);
    }

    public static void giveStar(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void appUpdateInit(Application application) {
        SophixManager.getInstance().setContext(application)
                .setAppVersion(Utils.getVersion(application.getApplicationContext()))
                .setPatchLoadStatusStub(new PatchLoadStatusListener() {
                    @Override
                    public void onload(final int mode, final int code, final String info, final int handlePatchVersion) {
                        // 补丁加载回调通知
                        if (code == PatchStatus.CODE_LOAD_SUCCESS) {
                            // 表明补丁加载成功
                        } else if (code == PatchStatus.CODE_LOAD_RELAUNCH) {
                            // 表明新补丁生效需要重启. 业务方可自行实现逻辑, 提示用户或者强制重启,
                            // 建议: 用户可以监听进入后台事件, 然后应用自杀
                        } else if (code == PatchStatus.CODE_LOAD_FAIL) {
                            // 内部引擎加载异常, 推荐此时清空本地补丁, 但是不清空本地版本号, 防止失败补丁重复加载
                            SophixManager.getInstance().cleanPatches();
                        } else {
                            // 其它错误信息, 查看PatchStatus类说明
                        }
                    }
                }).initialize();
        SophixManager.getInstance().queryAndLoadNewPatch();
    }

    public static void appUpdate(Context context) {
        SophixManager.getInstance().queryAndLoadNewPatch();
    }

    public static void feedBackInit(Application application) {
        FeedbackAPI.init(application, "23700654");
    }

    public static void feedBack(Context context) {
        FeedbackAPI.openFeedbackActivity();
    }

    public static void tellFriends(Context context) {
        try {
            FileUtil.share(context, "text/plain",
                    null, AppConstants.APP_LINK + " http://market.android.com/details?id=" + context.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
