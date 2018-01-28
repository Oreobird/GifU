package com.zgs.gifu.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;

import com.zgs.gifu.R;
import com.zgs.gifu.adapter.FilterAdapter;
import com.zgs.gifu.constant.AppConstants;

public class SpeedFragment extends Fragment {
	

	private SeekBar mSpeedBar;
	private CheckBox mOrderSwitch;
	private SpeedCallback speedCallback;
	private FilterCallback filterCallback;

	private RecyclerView mRecycleView;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		if (!(activity instanceof SpeedCallback)) {
			throw new IllegalStateException("SpeedCallback must be implemented");
		}
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_speed, container, false);
		initView(view);
		return view;
	}

	private void initView(View view) {
		mRecycleView = (RecyclerView) view.findViewById(R.id.filter_rv);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mRecycleView.setLayoutManager(mLayoutManager);

		initData(view);

		speedCallback = (SpeedCallback) getActivity();
		filterCallback = (FilterCallback) getActivity();

		mOrderSwitch = (CheckBox) view.findViewById(R.id.gv_order);
		mOrderSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				speedCallback.getOrder(isChecked ? AppConstants.BACKWARD : AppConstants.FORWARD);
			}

		});

		mSpeedBar = (SeekBar)view.findViewById(R.id.gv_speed_bar);
		mSpeedBar.setProgress(AppConstants.INITIAL_SPEED);
		mSpeedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				speedCallback.getSpeed(progress + 1);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

	}

	private void initData(View view) {
		initFilter(view);
	}

	private void initFilter(View view) {
		int[] filterList = new int[] {
				AppConstants.NONE,
				AppConstants.GAUSSIAN_BLUR, AppConstants.EXPOSURE,
				AppConstants.SHARPEN, AppConstants.CONTRAST, AppConstants.BRIGHTNESS,
				AppConstants.SATURATION,

				AppConstants.COLORINVERT, AppConstants.MONOCHROME, AppConstants.GRAYSCALE,
				AppConstants.CROSSHATCH, AppConstants.EMBOSS,

				AppConstants.SOBEL_EDGE, AppConstants.LAPLACIAN, AppConstants.SKETCH,
				AppConstants.WEAK_PIXEL_INCLUSION,

				AppConstants.GLASSSPHERE, AppConstants.BULGE_DISTORTION, AppConstants.SWIRL
		};

		Drawable drawable = getActivity().getResources().getDrawable(R.drawable.sample);//获取drawable
		Bitmap sample = ((BitmapDrawable) drawable).getBitmap();
		FilterAdapter mAdapter = new FilterAdapter(getActivity(), sample, filterList);
		mRecycleView.setAdapter(mAdapter);
		mAdapter.setOnItemClickListener(new FilterAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int filter) {
				filterCallback.getFilter(filter);
			}
		});
	}

	public void reset() {
		mOrderSwitch.setChecked(false);
		mSpeedBar.setProgress(AppConstants.INITIAL_SPEED);
	}

	public interface SpeedCallback {
		void getOrder(int order);
		void getSpeed(int speed);
	}

	public interface FilterCallback {
		void getFilter(int filter);
	}
}
