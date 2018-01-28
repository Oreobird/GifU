package com.zgs.gifu.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.taobao.android.SophixManager;
import com.taobao.android.listener.PatchLoadStatusListener;
import com.taobao.android.util.PatchStatus;
import com.zgs.gifu.R;
import com.zgs.gifu.adapter.MenuAdapter;
import com.zgs.gifu.entity.MenuItem;
import com.zgs.gifu.utils.PermissionHelper;
import com.zgs.gifu.utils.Utils;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {
    private static final Class<?>[] ACTIVITY = {CaptureActivity.class, ImportActivity.class, AlbumActivity.class};
    private static final String[] TITLE = {"拍摄制作", "导入制作", "Gif相册"};
    private static final int[] IMG = {R.drawable.ic_capture,R.drawable.ic_import,R.drawable.ic_album};
    private ArrayList<MenuItem> mDataList;
    private RecyclerView mRecyclerView;
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.page_menu);
        initView();
        initData();
        initAdapter();

        if (Build.VERSION.SDK_INT >= 23) {
            permissionHelper = new PermissionHelper(this);
            permissionHelper.setOnApplyPermissionListener(new PermissionHelper.OnApplyPermissionListener() {
                @Override
                public void onAfterApplyAllPermission() {
                    initHotfix();
                }
            });
            permissionHelper.applyPermissions();
        } else {
            initHotfix();
        }
    }

    private void initHotfix() {
        SophixManager.getInstance().setContext(this.getApplication())
                .setAppVersion(Utils.getVersion(this))
                .setAesKey(null)
                .setEnableDebug(false)
                .setPatchLoadStatusStub(new PatchLoadStatusListener() {
                    @Override
                    public void onload(final int mode, final int code, final String info, final int handlePatchVersion) {
                        if (code == PatchStatus.CODE_LOAD_SUCCESS) {
                            Log.d("补丁加载成功：code=", code + "--------");
                        } else if (code == PatchStatus.CODE_LOAD_RELAUNCH) {
                            Log.d("新补丁生效需要重启：code=", code + "--------");
                        } else if (code == PatchStatus.CODE_LOAD_FAIL) {
                            //SophixManager.getInstance().cleanPatches();
                            Log.d("补丁加载失败：code=", code + "--------");
                        } else {
                            Log.d("补丁加载：code=", code + "--------");
                        }
                    }
                }).initialize();
        SophixManager.getInstance().queryAndLoadNewPatch();
    }
    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(MenuActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.menu_rv);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
    }

    private void initAdapter() {
        BaseQuickAdapter menuAdapter = new MenuAdapter(R.layout.menu_item_view, mDataList);
        menuAdapter.openLoadAnimation();

        mRecyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(MenuActivity.this, ACTIVITY[position]);
                startActivity(intent);
            }
        });

        mRecyclerView.setAdapter(menuAdapter);
    }


    private void initData() {
        mDataList = new ArrayList<>();
        for (int i = 0; i < TITLE.length; i++) {
            MenuItem item = new MenuItem();
            item.setTitle(TITLE[i]);
            item.setActivity(ACTIVITY[i]);
            item.setImageResource(IMG[i]);
            mDataList.add(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissionHelper != null) {
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (permissionHelper != null) {
            permissionHelper.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
