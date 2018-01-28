package com.zgs.gifu.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import com.zgs.gifu.R;
import com.zgs.gifu.constant.AppConstants;

public class SaveFragment extends Fragment {
    private SaveCallback saveCallback;

    private int mShareType = AppConstants.AS_GIF;

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        if (!(activity instanceof SaveFragment.SaveCallback)) {
            throw new IllegalStateException("ShareCallback must be implemented");
        }
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_save, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        RadioButton gifBtn = (RadioButton) view.findViewById(R.id.gif);
        RadioButton mp4Btn = (RadioButton) view.findViewById(R.id.mp4);
        Button saveBtn = (Button) view.findViewById(R.id.save);
        Button shareBtn = (Button) view.findViewById(R.id.share);

        saveCallback = (SaveFragment.SaveCallback) getActivity();

        gifBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShareType = AppConstants.AS_GIF;
            }
        });
        mp4Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShareType = AppConstants.AS_MP4;
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCallback.getShareType(mShareType, 0);
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCallback.getShareType(mShareType, 1);
            }
        });
    }

    public interface SaveCallback {
        void getShareType(int shareType, int action);
    }
}
