package com.zgs.gifu.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zgs.gifu.R;
import com.zgs.gifu.adapter.PathAdapter;
import com.zgs.gifu.constant.AppConstants;
import com.zgs.gifu.utils.RecyclerViewDivider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by zgs on 2017/1/10.
 */

public class PathSelectFragment extends DialogFragment {

    private String titleStr;
    private File currentDirectory;
    private ArrayList<File> fileList = new ArrayList<>();

    private TextView mCurPathTextView;
    private PathAdapter mAdapter;

    public static PathSelectFragment newInstance(String titleStr) {
        PathSelectFragment newFragment = new PathSelectFragment();
        Bundle bundle = new Bundle();
        bundle.putString("titleStr", titleStr);
        newFragment.setArguments(bundle);
        return newFragment;

    }

    public interface PathSelectDialogFragmentClick {
        void doPositiveClick(String selectedDir);
        void doNegativeClick();
    }

    private PathSelectDialogFragmentClick mOnDialogFragmentClick;

    public void setOnDialogFragmentClick(PathSelectDialogFragmentClick mOnDialogFragmentClick) {
        this.mOnDialogFragmentClick = mOnDialogFragmentClick;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData(true); //fouceUpdate the dir first time
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                               Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.path_select_dialog, container, false);

        initView(view);

        return view;
    }

    private void initView(View view) {
        Bundle args = getArguments();
        if (args != null) {
            this.titleStr = args.getString("titleStr");
        }

        TextView mTitleTextView = (TextView) view.findViewById(R.id.path_select_title);
        mTitleTextView.setText(this.titleStr);

        ImageButton mBackLevelBtn = (ImageButton) view.findViewById(R.id.back_level);
        mBackLevelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backLevel();
            }
        });

        mCurPathTextView = (TextView) view.findViewById(R.id.cur_path);
        Button mConfirmBtn = (Button) view.findViewById(R.id.path_select_ok);
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnDialogFragmentClick.doPositiveClick(currentDirectory.getPath());
                currentDirectory = null;
                dismiss();
            }
        });
        Button mCancelBtn = (Button) view.findViewById(R.id.path_select_cancel);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnDialogFragmentClick.doNegativeClick();
                currentDirectory = null;
                dismiss();
            }
        });

        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.path_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new RecyclerViewDivider(getActivity()));


        SharedPreferences settings = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        String curPath = settings.getString(AppConstants.SAVE_PATH_NAME,
                Environment.getExternalStorageDirectory().getPath()+"/"+AppConstants.APP_NAME);
        this.currentDirectory = new File(curPath);

        mCurPathTextView.setText(this.currentDirectory.getPath());
        mAdapter = new PathAdapter(getActivity(), fileList);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new PathAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String filePath) {
                File file = new File(currentDirectory.getPath() + "/" + filePath);
                if (file.exists() && file.isDirectory()) {
                    // update current dir
                    loadDir(file, false);
                }
            }
        });
    }

    private void loadData(boolean forceUpdate) {
        loadDir(currentDirectory, forceUpdate);
    }

    private void loadDir(File file, boolean forceUpdate) {
        if (file != null && file.exists() && file.isDirectory()) {
            if (!file.getPath().equals(currentDirectory.getPath()) || forceUpdate) {
                // 与当前目录不同
                currentDirectory = file;
                fileList.clear();
                File[] files = file.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        File tmpFile = files[i];
                        if (tmpFile.isFile() || tmpFile.getName().equals(".") || tmpFile.getName().equals(".")) {
                            continue;
                        }
                        fileList.add(new File(tmpFile.getPath()));
                    }
                }
                sortList();
                mCurPathTextView.setText(this.currentDirectory.getPath());
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    public void sortList() {
        FileItemComparator comparator = new FileItemComparator();
        Collections.sort(fileList, comparator);
    }
    public class FileItemComparator implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
        }
    }

    private void backLevel() {
        File file = this.currentDirectory;
        // 如果当前目录不为空且父目录不为空，则打开父目录
        if (file != null && file.getParentFile() != null) {
            loadDir(file.getParentFile(), false);
        }
    }

}
