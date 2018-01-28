package com.zgs.gifu;

import android.app.Application;

import com.zgs.gifu.entity.FrameObj;
import com.zgs.gifu.utils.SettingUtil;

import net.youmi.android.AdManager;

import java.util.LinkedList;
import java.util.List;

public class GifApplication extends Application {

	private List<FrameObj> gifFrameList;

	@Override
	public void onCreate() {
		super.onCreate();

		SettingUtil.feedBackInit(this);
		AdManager.getInstance(this).init("2038dc8f4d1a6c37", "386b5202c74e0bbc", false);

		/*
		if (LeakCanary.isInAnalyzerProcess(this)) {
			// This process is dedicated to LeakCanary for heap analysis.
			// You should not init your app in this process.
			return;
		}

		LeakCanary.install(this);
		*/

		setGifFrameList(new LinkedList<FrameObj>());
	}

	public List<FrameObj> getGifFrameList() {
		return gifFrameList;
	}

	public void setGifFrameList(List<FrameObj> gifFrameList) {
		this.gifFrameList = gifFrameList;
	}

	public void clearFrameList(List<FrameObj> gifFrameList) {
		this.gifFrameList.removeAll(gifFrameList);
	}

}
