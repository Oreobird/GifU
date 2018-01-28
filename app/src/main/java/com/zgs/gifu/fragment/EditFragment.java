package com.zgs.gifu.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.zgs.gifu.R;
import com.zgs.gifu.activity.GifViewActivity;
import com.zgs.gifu.adapter.FrameAdapter;
import com.zgs.gifu.constant.AppConstants;
import com.zgs.gifu.entity.FrameObj;

import java.util.ArrayList;
import java.util.Arrays;

public class EditFragment extends Fragment {
	
	private LinearLayout mColorContainer;
	private Button mSelectAllBtn;

	private TextFragment mEditFm;
	private FontFragment mFontFm;

	private RecyclerView mRecycleView;
	private FrameAdapter mAdapter;
	private EditCallback callback;
	private int colorSelected;
	private FrameObj[] frameArray;
	private ArrayList<FrameObj> frameList;

	private LocalBroadcastManager lbm;
	private BroadcastReceiver mReceiver;

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			refresh(false);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		if (!(activity instanceof TextFragment.EditCallback)) {
			throw new IllegalStateException("EditCallback must be implemented");
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction(AppConstants.EDITFM_RESET_ACTION);
		mReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(AppConstants.EDITFM_RESET_ACTION)) {
					reset();
				}
			}
		};
		lbm = LocalBroadcastManager.getInstance(activity);
		lbm.registerReceiver(mReceiver, filter);
		super.onAttach(activity);
	}

	@Override
	public void onDestroyView() {
		lbm.unregisterReceiver(mReceiver);
		super.onDestroyView();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit, container, false);
		
		initView(view);
		
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		reset();
	}


	private void initView(View view) {
		mColorContainer = (LinearLayout) view.findViewById(R.id.text_color);
		ImageButton mEditBtn = (ImageButton) view.findViewById(R.id.text_edit);
		ImageButton mFormatBtn = (ImageButton) view.findViewById(R.id.text_format);
		mSelectAllBtn = (Button) view.findViewById(R.id.select_all);
		Button mSelectDoneBtn = (Button) view.findViewById(R.id.select_done);
		callback = (EditCallback) getActivity();

		addColorPicker(view);

		BtnListener btnListoner = new BtnListener();
		mEditBtn.setOnClickListener(btnListoner);
		mFormatBtn.setOnClickListener(btnListoner);
		mSelectAllBtn.setOnClickListener(btnListoner);
		mSelectDoneBtn.setOnClickListener(btnListoner);


		mRecycleView = (RecyclerView) view.findViewById(R.id.gif_frame_container);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mRecycleView.setLayoutManager(mLayoutManager);
		initData(view);
	}

	private void initData(View view) {
		frameArray = ((GifViewActivity)getActivity()).getmFrames();
		frameList = new ArrayList<>(Arrays.asList(frameArray));
		mAdapter = new FrameAdapter(getActivity(), frameArray, frameList);
		mRecycleView.setAdapter(mAdapter);
		mAdapter.setOnItemClickListener(new FrameAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int frameNum) {
				callback.itemSelected(frameNum);
			}
		});
	}

	private void addColorPicker(View view) {
		int [] mColors = new int[] {
				0xffff4444, 0xffff8800, 0xffffbb33, //redlight, orangedark, yellow
				0xff669900, 0xff99cc00,	0xff0099cc, 	//green, lightgreen, blue
				0xFF5c5cff, 0xffaa66cc, 0xfff3f3f3, //darkblue, purple, white
				0xFF8c8c8c, 0xFF000000		//gray, black
            };
		GradientDrawable mGrad;

		for (int i = 0; i < mColors.length; i++) {
			ImageButton mColorBtn = new ImageButton(mColorContainer.getContext());
			mColorBtn.setId(mColors[i]);
			mColorBtn.setBackground(getActivity().getResources().getDrawable(R.drawable.color_btn));
			mGrad = (GradientDrawable) mColorBtn.getBackground();
			mGrad.setColor(mColors[i]);
			mColorBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					colorSelected = v.getId();
					callback.getTextStrColor(colorSelected);
				}
			});
			mColorContainer.addView(mColorBtn);
		}
	}

	private class BtnListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			FragmentManager fm = getFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			
			switch (v.getId()) {
			case R.id.text_edit:
				if (mEditFm == null) {
					mEditFm = new TextFragment();
				}
				/*
				Bundle args = new Bundle();
				args.putString("textStr", "");  //new text tag
				mEditFm.setArguments(args);
				*/
				mEditFm.show(getFragmentManager(), "edit_fm");
				break;
			case R.id.text_format:
				if (mFontFm == null) {
					mFontFm = new FontFragment();
				}

				mFontFm.show(getFragmentManager(), "font_fm");
				break;
			case R.id.select_all:
				String text = mSelectAllBtn.getText().toString();
				if (text.equals(getActivity().getString(R.string.select_all))) {
					mSelectAllBtn.setText(getActivity().getString(R.string.select_none));
					callback.selectAll(true);
					mSelectAllBtn.setText(getActivity().getString(R.string.select_none));
				} else {
					mSelectAllBtn.setText(getActivity().getString(R.string.select_all));
					callback.selectAll(false);
				}
				refresh(true);
				break;
			case R.id.select_done:
				if (callback.selectDone()) {
					refresh(false);
				}
				break;
			default:
				break;
			}
			ft.commit();
		}
	}

	public interface EditCallback {
		void getTextStrColor(int color);
		boolean selectDone();
		void selectAll(boolean isSelect);
		void itemSelected(int position);
	}

	private void reset() {
		frameList.clear();
		frameArray = ((GifViewActivity)getActivity()).getmFrames();
		for (int i = 0; i < frameArray.length; i++) {
			frameList.add(frameArray[i]);
		}
		mAdapter.notifyDataSetChanged();
	}

	private void refresh(boolean force) {
		frameArray = ((GifViewActivity)getActivity()).getnewFrames();
		if (frameArray == null && !force) {
			return;
		} else if (force && frameArray == null){
			frameArray = ((GifViewActivity)getActivity()).getmFrames();
		}

		boolean hasTag = false;
		frameList.clear();
		for (int i = 0; i < frameArray.length; i++) {
			frameList.add(frameArray[i]);
			if (frameArray[i].isTextTag()) {
				hasTag = true;
			}
		}
		mAdapter.notifyDataSetChanged();
		if (!hasTag) {
			mSelectAllBtn.setText(getActivity().getString(R.string.select_all));
		}
	}
}
