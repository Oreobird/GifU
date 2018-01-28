package com.zgs.gifu.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zgs.gifu.R;
import com.zgs.gifu.adapter.BoarderAdapter;

public class CutFragment extends Fragment {
	private ThreeDCallback callback;

	private RecyclerView mRecycleView;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		if (!(activity instanceof ThreeDCallback)) {
			throw new IllegalStateException("ThreeDCallback must be implemented");
		}
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_cut, container, false);

		initView(view);
		return view;
	}

	private void initView(View view) {
		mRecycleView = (RecyclerView) view.findViewById(R.id.boarder_rv);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mRecycleView.setLayoutManager(mLayoutManager);

		initData(view);
		callback = (ThreeDCallback) getActivity();
	}

	private void initData(View view) {
		initBoarder(view);
	}



	private void initBoarder(View view) {
		int[] boarderList = new int[] {
				R.drawable.frame, R.drawable.frame1,
				R.drawable.frame2, R.drawable.frame3,
				R.drawable.frame4, R.drawable.frame5,
				R.drawable.frame6
		};

		BoarderAdapter mBoarderAdapter = new BoarderAdapter(getActivity(), boarderList);
		mRecycleView.setAdapter(mBoarderAdapter);
		mBoarderAdapter.setOnItemClickListener(new BoarderAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int boarder) {
				callback.getBoarder(boarder);
			}
		});
	}


	public interface ThreeDCallback {
		void getBoarder(int boarder);
	}
}
