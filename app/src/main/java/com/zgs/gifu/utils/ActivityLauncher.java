/*
 * Copyright (C) 2015 takahirom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.zgs.gifu.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.zgs.gifu.R;
import com.zgs.gifu.entity.ImageObj;

import java.util.List;

public class ActivityLauncher {
    private final Activity activity;
    private View fromView;
    private int position;
    private List<ImageObj> imageObjList;

    private ActivityLauncher(Activity activity) {
        this.activity = activity;
    }

    public static ActivityLauncher with(Activity activity) {
        return new ActivityLauncher(activity);
    }

    public ActivityLauncher from(View fromView) {
        this.fromView = fromView;
        return this;
    }

    public ActivityLauncher position(final int position) {
        this.position = position;
        return this;
    }

    public ActivityLauncher dataList(List<ImageObj> imageObjList) {
        this.imageObjList = imageObjList;
        return this;
    }

    private Bundle createBundle() {
        return BundleFactory.createTransitionBundle(activity, fromView, position, imageObjList);
    }

    public void launch(Intent intent) {
        intent.putExtras(createBundle());
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
