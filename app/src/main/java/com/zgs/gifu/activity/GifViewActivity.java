package com.zgs.gifu.activity;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.zgs.gifu.GifApplication;
import com.zgs.gifu.R;
import com.zgs.gifu.constant.AppConstants;
import com.zgs.gifu.entity.FrameObj;
import com.zgs.gifu.entity.ThreeDObj;
import com.zgs.gifu.fragment.ConfirmFragment;
import com.zgs.gifu.fragment.CutFragment;
import com.zgs.gifu.fragment.CutFragment.ThreeDCallback;
import com.zgs.gifu.fragment.EditFragment;
import com.zgs.gifu.fragment.EditFragment.EditCallback;
import com.zgs.gifu.fragment.FontFragment.FontCallback;
import com.zgs.gifu.fragment.SaveFragment;
import com.zgs.gifu.fragment.SpeedFragment;
import com.zgs.gifu.fragment.SpeedFragment.SpeedCallback;
import com.zgs.gifu.fragment.TextFragment;
import com.zgs.gifu.imageprocess.ImageFilter;
import com.zgs.gifu.utils.FileUtil;
import com.zgs.gifu.utils.GifUtil;
import com.zgs.gifu.view.AbsTag;
import com.zgs.gifu.view.GifPlayView;
import com.zgs.gifu.view.TagView;
import com.zgs.gifu.view.TextTag;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import jp.co.cyberagent.android.gpuimage.GPUImage;

public class GifViewActivity extends AppCompatActivity
		implements SpeedCallback,SpeedFragment.FilterCallback,
		TextFragment.EditCallback, EditCallback,
		FontCallback, ThreeDCallback, SaveFragment.SaveCallback {

	private GifPlayView mGifPlayView;

	private ProgressDialog progressDialog;
	//private RadioButton imgFilterBtn;
	private SpeedFragment mSpeedFragment;
	private SaveFragment mSaveFragment;
	private EditFragment mEditFragment;
	private CutFragment mCutFragment;

	private TagView mTagView;

	private int mFilter;
	private FrameObj[] newFrames;
	private FrameObj[] mFrames;
	private ThreeDObj mThreeDObj;
	private int mBoarder;
	private int mShareType;

	private GifApplication gifApp;
	private String mSavePath = null;
	private boolean isShare = false;

	private TextFragment mEditFm;

	public FrameObj[] getmFrames() {
		return mFrames;
	}
	public FrameObj[] getnewFrames() {
		return newFrames;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case android.R.id.home:
				finish();
				return true;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!mGifPlayView.isRunning() && mGifPlayView.getGifPlayThread() == null) {
			mGifPlayView.gifPlay();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		gifApp.clearFrameList(gifApp.getGifFrameList());
		mThreeDObj.destroy();
		super.onDestroy();
		mGifPlayView.gifStop();
		if (mGifPlayView.getGifPlayThread() != null) {
			mGifPlayView.setGifPlayThread(null);
		}

	}

	@Override
	public void onBackPressed() {
		gifApp.clearFrameList(gifApp.getGifFrameList());
		mThreeDObj.destroy();
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mGifPlayView.gifStop();
		if (mGifPlayView.getGifPlayThread() != null) {
			mGifPlayView.setGifPlayThread(null);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.page_gifview);
		
		initView(savedInstanceState);
	}
	
	
	private FrameObj[] getFramesArray() {
		gifApp = (GifApplication) getApplication();
		int frameNum = gifApp == null ? 0 : gifApp.getGifFrameList().size();
		return frameNum <= 0 ? null : gifApp.getGifFrameList().toArray(new FrameObj[frameNum]);
	}
	
	private void initData() {
		this.mFrames = getFramesArray();
		if (mFrames != null) {
			for (int i = 0; i < this.mFrames.length; i++) {
				mFrames[i].setBitmap(GifUtil.resizeBitmapByCenterCrop(mFrames[i].getBitmap(),
						AppConstants.GIF_WIDTH, AppConstants.GIF_HEIGHT, false));
			}
		}

		Drawable drawable = getResources().getDrawable(R.drawable.frame1);//获取drawable
		Bitmap boarder = ((BitmapDrawable) drawable).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
		mThreeDObj = new ThreeDObj(mFrames, boarder);
	}

	
	private void setDefaultFragment(Bundle savedInstanceState) {
		if(savedInstanceState == null) {
			FragmentManager fm = getFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			if (mSpeedFragment == null) {
				mSpeedFragment = new SpeedFragment();
			} 
			if (fm.findFragmentByTag("speed") == null) {
				ft.add(R.id.fragment_container, mSpeedFragment, "speed");
			}
			hideAllFragment(ft);
			ft.show(mSpeedFragment).commit();
		}
	}
	
	private void hideAllFragment(FragmentTransaction transaction) {
		if (mSpeedFragment != null) {
			transaction.hide(mSpeedFragment);
		}
		if (mEditFragment != null) {
			transaction.hide(mEditFragment);
		}
		if (mCutFragment != null) {
			transaction.hide(mCutFragment);
		}
		if (mSaveFragment != null) {
			transaction.hide(mSaveFragment);
		}
	}
	
	private void initView(Bundle savedInstanceState) {
		//initActionBar();
		ImageButton doneBtn = (ImageButton)findViewById(R.id.gv_save);
		ImageButton closeBtn = (ImageButton) findViewById(R.id.gv_reset);
		RadioButton speedBtn = (RadioButton) findViewById(R.id.gv_speed);
		RadioButton textBtn = (RadioButton) findViewById(R.id.gv_edit);
		//imgFilterBtn = (RadioButton) findViewById(R.id.gv_image_filter);
		ImageButton imgFilterBtn = (ImageButton) findViewById(R.id.gv_image_filter);

		BtnListeners btnListener = new BtnListeners();
    	closeBtn.setOnClickListener(btnListener);
    	doneBtn.setOnClickListener(btnListener);
    	
    	speedBtn.setOnClickListener(btnListener);
    	textBtn.setOnClickListener(btnListener);
    	imgFilterBtn.setOnClickListener(btnListener);

		initData();
		mGifPlayView = (GifPlayView)findViewById(R.id.gif_playview);

		mTagView = (TagView) findViewById(R.id.tagview);
		mTagView.setOnBtnListener(new TagView.OnBtnListener() {
			@Override
			public void handleEditBtn() {
				FragmentManager fm = getFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();

				if (mEditFm == null) {
					mEditFm = new TextFragment();
				}

				Bundle args = new Bundle();
				args.putString("textStr", ((TextTag)mTagView.getSelectedTag()).getTextStr());  //new text tag
				mEditFm.setArguments(args);

				mEditFm.show(getFragmentManager(), "edit_fm");
				ft.commit();
			}
		});

		mGifPlayView.initData(mFrames, AppConstants.INITIAL_SPEED, AppConstants.FORWARD);	//speed
		mGifPlayView.gifPlay();
		setDefaultFragment(savedInstanceState);

	}

	private class BtnListeners implements OnClickListener {
		@Override
		public void onClick(View v) {
			FragmentManager fm = getFragmentManager();
			FragmentTransaction transaction = fm.beginTransaction();

			switch (v.getId()) {
			case R.id.gv_reset:
				doReset();
				break;
			case R.id.gv_save:
				hideAllFragment(transaction);
				if (mSaveFragment == null) {
					mSaveFragment = new SaveFragment();
				}
				if (fm.findFragmentByTag("save") == null) {
					transaction.add(R.id.fragment_container, mSaveFragment, "save");
				}
				transaction.show(mSaveFragment);
				break;
			case R.id.gv_speed:
				hideAllFragment(transaction);
				if (mSpeedFragment == null) {
					mSpeedFragment = new SpeedFragment();
				} 
				if (fm.findFragmentByTag("speed") == null) {
					transaction.add(R.id.fragment_container, mSpeedFragment, "speed");
				}
				transaction.show(mSpeedFragment);
				break;
			case R.id.gv_edit:
				hideAllFragment(transaction);
				if (mEditFragment == null) {
					mEditFragment = new EditFragment();
				}
				if (fm.findFragmentByTag("text") == null) {
					transaction.add(R.id.fragment_container, mEditFragment, "text");
				}
				transaction.show(mEditFragment);
				break;

			case R.id.gv_image_filter:
				hideAllFragment(transaction);
				if (mCutFragment == null) {
					mCutFragment = new CutFragment();
				}
				if (fm.findFragmentByTag("imgfilter") == null) {
					transaction.add(R.id.fragment_container, mCutFragment, "imgfilter");
				}
				transaction.show(mCutFragment);
				break;
			default:
				break;
			}
			transaction.commit();
		}
	}

	private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 0;

	private void requestExternalStoragePermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					REQUEST_EXTERNAL_STORAGE_PERMISSION);
		} else {
			doSave(mShareType);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case REQUEST_EXTERNAL_STORAGE_PERMISSION:
				if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "您没有授权写SD卡权限，请在设置中打开授权", Toast.LENGTH_SHORT).show();
				} else {
					doSave(mShareType);
				}
				return;
			default:
		}
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	private void doSave(int shareType) {
		progressDialog = ProgressDialog.show(GifViewActivity.this, null, this.getResources().getString(R.string.gif_making));
		mSavePath = FileUtil.getPath(this, FileUtil.getName(shareType));
		Thread gifMakerThread = new Thread(new gifMakerRunnable());
		gifMakerThread.start();
	}
	
	private void doReset() {
		ConfirmFragment resetConfirmFragment = ConfirmFragment.newInstance(
				getResources().getString(R.string.reset_txt),
				getResources().getString(R.string.cancel_txt),
				getResources().getString(R.string.reset_txt),
				getResources().getString(R.string.reset_confirm_msg));
		resetConfirmFragment.setmOnDialogItemClick(0, new ConfirmFragment.DialogFragmentClick() {
			@Override
			public void doPositiveClick(int position) {
				//reset data
				mGifPlayView.setmFrames(mFrames);

				LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(GifViewActivity.this);
				lbm.sendBroadcast(new Intent(AppConstants.EDITFM_RESET_ACTION));

				if (newFrames != null) {
					for (int i = 0; i < newFrames.length; i++) {
						newFrames[i].getBitmap().recycle();
						newFrames[i].setBitmap(null);
						newFrames[i] = null;
					}
					newFrames = null;
				}

				if (mSpeedFragment != null) {
					mSpeedFragment.reset();
				}
				if (mTagView != null) {
					mTagView.reset();
				}

			}

			@Override
			public void doNegativeClick(int position) {
				//Do nothing
			}
		});
		resetConfirmFragment.show(getFragmentManager(), "delete_confirm_fm");
	}

	private void doShare(int shareType) {
		if (mSavePath == null) {
			isShare = true;
			if (Build.VERSION.SDK_INT >= 23) {
				requestExternalStoragePermission();
			} else {
				doSave(shareType);
			}
		} else {
			String media = (shareType == AppConstants.AS_GIF) ? "image/gif" : "video/mp4";
			FileUtil.share(GifViewActivity.this, media, mSavePath, null);
		}
	}

	private static class GifViewHandler extends Handler {
		private final WeakReference<GifViewActivity> mActivity;

		private GifViewHandler(GifViewActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			GifViewActivity activity = mActivity.get();
			if (activity != null) {
				if (activity.progressDialog != null && activity.progressDialog.isShowing()) {
					activity.progressDialog.dismiss();
				}
				switch (msg.what) {
					case 1:
					{
						if (activity.isShare) {
							activity.isShare = false;
							String media = (activity.mShareType == AppConstants.AS_GIF) ? "image/gif" : "video/mp4";
							FileUtil.share(activity, media, activity.mSavePath, null);
						} else {
							Toast.makeText(activity,
									activity.getResources().getString(R.string.gif_make_success),
									Toast.LENGTH_SHORT).show();
						}
						break;
					}
					case 2:
					{
						if (activity.isShare) {
							activity.isShare = false;
						}
						Toast.makeText(activity,
								activity.getResources().getString(R.string.gif_make_failed),
								Toast.LENGTH_SHORT).show();
						break;
					}
					case 3:
					{
						activity.mGifPlayView.setmFrames(activity.newFrames);
					}
				}
			}
		}
	}

	private GifViewHandler mHandler = new GifViewHandler(this);

	/*
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			switch (msg.what) {
				case 1:
				{
					if (isShare) {
						isShare = false;
						String media = (mShareType == AppConstants.AS_GIF) ? "image/gif" : "video/mp4";
						FileUtil.share(GifViewActivity.this, media, mSavePath, null);
					} else {
						Toast.makeText(GifViewActivity.this,
								GifViewActivity.this.getResources().getString(R.string.gif_make_success),
								Toast.LENGTH_SHORT).show();
					}
					break;
				} 
				case 2:
				{
					if (isShare) {
						isShare = false;
					}
					Toast.makeText(GifViewActivity.this,
							GifViewActivity.this.getResources().getString(R.string.gif_make_failed),
							Toast.LENGTH_SHORT).show();
					break;
				}
				case 3:
				{
					mGifPlayView.setmFrames(newFrames);
				}
			}
		}
	};
	*/

	private class gifMakerRunnable implements Runnable {

		@Override
		public void run() {
			boolean isSuccess = (mShareType == AppConstants.AS_GIF) ? imageToGif(mSavePath) : imageToVideo(mSavePath);
			try {
				Thread.sleep(500);
				Message msg = new Message();

				if (isSuccess) {
					msg.what = 1;
				} else {
					msg.what = 2;
				}
				mHandler.sendMessage(msg);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void drawTagsOnBitmap(Bitmap bitmap, ArrayList<AbsTag> tagList) {
		if (bitmap != null && tagList != null) {
			Canvas canvas = new Canvas(bitmap);
			for (int i = 0; i < tagList.size(); i++) {
				AbsTag tag = tagList.get(i);
				tag.drawOnScaledCanvas(canvas, tag);
			}
		}
	}

	private void drawTagsOnGif(Bitmap[] bitmaps, ArrayList<AbsTag> tagList) {
		if (bitmaps != null && tagList != null) {
			for (int i = 0; i < bitmaps.length; i++) {
				drawTagsOnBitmap(bitmaps[i], tagList);
			}
		}
	}
	private boolean imageToGif(String filePath) {
		int fps = mGifPlayView.getSpeed();

		if (newFrames != null) {
			//drawTagsOnGif(newBitmaps, mTagView.getTagList());
			try {
				GifUtil.createGif(filePath, newFrames, mGifPlayView.getOrder(), fps,
								AppConstants.GIF_WIDTH, AppConstants.GIF_HEIGHT);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			//drawTagsOnGif(mBitmaps, mTagView.getTagList());
			try {
				GifUtil.createGif(filePath, mFrames, mGifPlayView.getOrder(), fps,
								AppConstants.GIF_WIDTH, AppConstants.GIF_HEIGHT);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return FileUtil.notifyChange(this, filePath);
	}

	private boolean imageToVideo(String filePath) {
		int fps = mGifPlayView.getSpeed();
		if (newFrames != null) {
			try {
				GifUtil.createVideo(filePath, newFrames, mGifPlayView.getOrder(), 1000 / fps,
						AppConstants.GIF_WIDTH, AppConstants.GIF_HEIGHT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				GifUtil.createVideo(filePath, mFrames, mGifPlayView.getOrder(), 1000 / fps,
						AppConstants.GIF_WIDTH, AppConstants.GIF_HEIGHT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return FileUtil.notifyChange(this, filePath);
	}

	@Override
	public void updateTextStr(int mode, String text) {
		if (mode == AppConstants.TAG_CREATE) {
			AbsTag tag = new TextTag(mTagView, text, 0.0f);
			mTagView.addTag(tag);
		} else {
			((TextTag)mTagView.getSelectedTag()).setTextStr(text);
		}
	}

	@Override
	public void getTextFontType(Typeface typeface) {
		AbsTag tag = mTagView.getSelectedTag();
		if (tag != null && typeface != null) {
			tag.updateTypeface(typeface);
		}
	}

	@Override
	public void getTextStrColor(int color) {
		AbsTag tag = mTagView.getSelectedTag();
		if (tag != null) {
			tag.updateColor(color);
		}
	}

	@Override
	public boolean selectDone() {
		boolean hasTag = false;
		for (int j = 0; j < mFrames.length; j++) {
			if (mFrames[j].isTextTag()) {
				hasTag = true;
			}
		}
		if (!hasTag) {
			Toast.makeText(GifViewActivity.this,
					GifViewActivity.this.getResources().getString(R.string.select_frame),
					Toast.LENGTH_SHORT).show();
			return false;
		}

		if (newFrames == null) {
			newFrames = new FrameObj[mFrames.length];
		}

		for (int j = 0; j < mFrames.length; j++) {
			if (newFrames[j] == null) {
				newFrames[j] = mFrames[j].clone();
			}
			if (mFrames[j].isTextTag()) {
				drawTagsOnBitmap(newFrames[j].getBitmap(), mTagView.getTagList());
				mFrames[j].setTextTag(false);
				newFrames[j].setTextTag(false);
				//为了区分是否已经加text，filter和3d的状态
				mFrames[0].setTagFlag(true);
				newFrames[0].setTagFlag(true);
			}
		}

		mTagView.removeAllTags();
		mGifPlayView.setmFrames(newFrames);
		return true;
	}

	@Override
	public void selectAll(boolean isSelect) {
		int frameNum = mFrames.length;
		for (int i = 0; i < frameNum; i++) {
			mFrames[i].setTextTag(isSelect);
			if (newFrames != null) {
				newFrames[i].setTextTag(isSelect);
			}
		}
	}

	@Override
	public void itemSelected(int position) {
		mFrames[position].setTextTag(!mFrames[position].isTextTag());
	}

	@Override
	public void getOrder(int order) {
		mGifPlayView.setOrder(order);
	}

	@Override
	public void getSpeed(int speed) {
		mGifPlayView.setSpeed(speed);
	}

	@Override
	public void getBoarder(int boarder) {
		progressDialog = ProgressDialog.show(GifViewActivity.this, null, this.getResources().getString(R.string.image_process));
		mBoarder = boarder;
		new Thread(new Runnable() {
			@Override
			public void run() {
				Drawable drawable = GifViewActivity.this.getResources().getDrawable(mBoarder);//获取drawable
				Bitmap boarder = ((BitmapDrawable) drawable).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
				if (newFrames == null) {
					newFrames = new FrameObj[mFrames.length];
				} else if (!mThreeDObj.isCut() && newFrames[0].isFilterFlag()){	//已filter但没cut的会以mFrames为原始图
					mThreeDObj.setFrameList(newFrames);
				}
				newFrames = mThreeDObj.setBoarder(boarder).apply3D();
				Message msg = new Message();
				msg.what = 3;
				mHandler.sendMessage(msg);
			}
		}).start();
	}

	@Override
	public void getFilter(int filter) {
		progressDialog = ProgressDialog.show(GifViewActivity.this, null, this.getResources().getString(R.string.image_process));
		mFilter = filter;
		new Thread(new Runnable() {
			@Override
			public void run() {
				GPUImage gpuImage = new GPUImage(GifViewActivity.this);
				if (newFrames == null) {
					newFrames = new FrameObj[mFrames.length];
					newFrames = ImageFilter.filterApplyAll(gpuImage, mFrames, mFilter);
					newFrames[0].setFilterFlag(true);
				} else {
					if (mFilter == AppConstants.NONE) {
						newFrames = mFrames;
					} else {
						newFrames = ImageFilter.filterApplyAll(gpuImage, newFrames, mFilter);
						newFrames[0].setFilterFlag(true);
					}
				}
				Message msg = new Message();
				msg.what = 3;
				mHandler.sendMessage(msg);
			}
		}).start();
	}


	@Override
	public void getShareType(int shareType, int action) {
		mShareType = shareType;
		if (action == 0) {//save
			if (Build.VERSION.SDK_INT >= 23) {
				requestExternalStoragePermission();
			} else {
				doSave(shareType);
			}
		} else {
			doShare(shareType);
		}

	}
}
