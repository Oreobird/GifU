package com.zgs.gifu.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zgs.gifu.R;
import com.zgs.gifu.adapter.FontAdapter;
import com.zgs.gifu.utils.FontUtil;

import java.util.ArrayList;

/**
 * Created by zgs on 2017/1/2.
 */
public class FontFragment extends DialogFragment {
    private RecyclerView mRecyclerView;

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        if (!(activity instanceof FontCallback)) {
            throw new IllegalStateException("EditCallback must be implemented");
        }
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    private void loadData() {
        ArrayList<Typeface> typefaceList = FontUtil.getAllTypeface(getActivity());
        String[] typefaceNames = FontUtil.getAllTypefaceName(getActivity());

        FontAdapter mAdapter = new FontAdapter(getActivity(), typefaceList, typefaceNames);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new FontAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Typeface typeface) {
                FontCallback callback = (FontCallback) getActivity();
                callback.getTextFontType(typeface);
                dismiss();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_font, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.font_rv);
        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        return view;
    }

    public interface FontCallback {
        void getTextFontType(Typeface fontType);
    }
}
