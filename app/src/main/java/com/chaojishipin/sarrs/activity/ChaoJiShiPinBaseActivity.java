package com.chaojishipin.sarrs.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import com.chaojishipin.sarrs.ChaoJiShiPinApplication;
import com.chaojishipin.sarrs.R;
import com.chaojishipin.sarrs.bean.UpgradeInfo;
import com.chaojishipin.sarrs.http.volley.RequestListener;
import com.chaojishipin.sarrs.interfaces.INetWorkObServe;
import com.chaojishipin.sarrs.manager.NetworkManager;
import com.chaojishipin.sarrs.receiver.NetWorkStateReceiver;
import com.chaojishipin.sarrs.receiver.PackageStartReceiver;
import com.chaojishipin.sarrs.receiver.SdcardRecever;
import com.chaojishipin.sarrs.thirdparty.umeng.UMengAnalysis;
import com.chaojishipin.sarrs.utils.AllActivityManager;
import com.chaojishipin.sarrs.utils.ConstantUtils;
import com.chaojishipin.sarrs.utils.LogUtil;
import com.chaojishipin.sarrs.utils.UpgradeHelper;
import com.chaojishipin.sarrs.widget.NetStateView;
import com.chaojishipin.sarrs.widget.PopupDialog;
import com.chaojishipin.sarrs.widget.PublicLoadLayout;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import de.greenrobot.event.EventBus;

public abstract class ChaoJiShiPinBaseActivity extends FragmentActivity implements INetWorkObServe{

    public static final String TAG = "ChaoJiShiPinBaseActivity";
    /**
     * 顶部标题栏布局
     */
    private RelativeLayout mTitleBarLayout;
    /**
     * 内容部分
     */
    public PublicLoadLayout mContentLayout;

    private LinearLayout mRightBtnLayout;
    private LinearLayout mLeftBtnLayout;
    private TextView mTitle;

    protected NetWorkStateReceiver mNetWorkReceiver;

    protected SdcardRecever sdcardRecever;

    protected PackageStartReceiver mPkgReceiver;
    /**
     * 创建用于处理消息的handler
     */
    public UIHandler mHandler;

    protected LayoutInflater mInflater;
    protected NetStateView.onNetErrorListener mNetListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.baseactivity_layout);
        setHandler();
        //Activity的管理类
        AllActivityManager.getInstance().addActivity(this);
        initView();
        setListener();
        //registInfo(this);
    }

    /**
     * 注册信息
     */
    protected void registInfo(Context context) {
        //注册EventBus
        EventBus.getDefault().register(context);
    }


    public void setNetErrorListener(NetStateView.onNetErrorListener mNetListener) {

        this.mNetListener = mNetListener;
    }

    public NetStateView.onNetErrorListener getNetErrorListener() {

        return mNetListener;
    }

    protected void unRegistInfo() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //注册网络切换等监听
        registerReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        UMengAnalysis.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("AAAAAonPause");
//        UMengAnalysis.onPause(this);
        //切换至后台是无需监听网络变化
        unRegisterReceiver();

    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("AAAAAonStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("AAAAAonDestroy");
        unRegisterReceiver();
        // unRegistInfo();
        fixInputMethodManagerLeak(this);
        AllActivityManager.getInstance().finishActivity(this);

    }


    public static void fixInputMethodManagerLeak(Context context) {
        if (context == null) {
            return;
        }
        try {
            // 对 mCurRootView mServedView mNextServedView 进行置空...
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null) {
                return;
            }// author:sodino mail:sodino@qq.com

            Object obj_get = null;
            Field f_mCurRootView = imm.getClass().getDeclaredField("mCurRootView");
            Field f_mServedView = imm.getClass().getDeclaredField("mServedView");
            Field f_mNextServedView = imm.getClass().getDeclaredField("mNextServedView");

            if (f_mCurRootView.isAccessible() == false) {
                f_mCurRootView.setAccessible(true);
            }
            obj_get = f_mCurRootView.get(imm);
            if (obj_get != null) { // 不为null则置为空
                f_mCurRootView.set(imm, null);
            }

            if (f_mServedView.isAccessible() == false) {
                f_mServedView.setAccessible(true);
            }
            obj_get = f_mServedView.get(imm);
            if (obj_get != null) { // 不为null则置为空
                f_mServedView.set(imm, null);
            }

            if (f_mNextServedView.isAccessible() == false) {
                f_mNextServedView.setAccessible(true);
            }
            obj_get = f_mNextServedView.get(imm);
            if (obj_get != null) { // 不为null则置为空
                f_mNextServedView.set(imm, null);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void setHandler() {
        mHandler = new UIHandler(this);
    }

    private void initView() {
        mInflater = LayoutInflater.from(this);
        mTitleBarLayout = (RelativeLayout) findViewById(R.id.baseactivity_titlebar);
        mContentLayout = (PublicLoadLayout) findViewById(R.id.baseactivity_content_area);
        mRightBtnLayout = (LinearLayout) findViewById(R.id.baseactivity_right_layout);
        mLeftBtnLayout = (LinearLayout) findViewById(R.id.baseactivity_left_layout);
        mTitle = (TextView) findViewById(R.id.baseactivity_title);
        View contentView = setContentView();
        if (null != contentView) {
            mContentLayout.addContent(contentView);
        }
    }
    private void setListener() {
        mRightBtnLayout.setOnClickListener(mOnClickListener);
        mLeftBtnLayout.setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.baseactivity_left_layout:
                    onClickLeftButton();
                    break;
                case R.id.baseactivity_right_layout:
                    onClickRightButton();
                    break;
                default:
                    break;
            }
        }
    };

    protected void setTitleBarVisibile(boolean flag) {
        if (null != mTitleBarLayout) {
            if (flag) {
                mTitleBarLayout.setVisibility(View.VISIBLE);
            } else {
                mTitleBarLayout.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 设置顶部Title名称
     *
     * @param resid
     */
    protected void setTitleText(int resid) {
        if (null != mTitle) {
            mTitle.setText(resid);
        }
    }

    /**
     * 设置内容区域背景色
     *
     * @param resid
     */
    protected void setContentBgColor(int resid) {
        mContentLayout.setBackgroundResource(resid);
    }
    public static final String pkg_add="android.intent.action.PACKAGE_ADDED";
    public static final String pkg_removed="android.intent.action.PACKAGE_REMOVED";
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        mNetWorkReceiver = new NetWorkStateReceiver();
        mNetWorkReceiver.setmNetWorkObserve(this);
        registerReceiver(mNetWorkReceiver, filter);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter2.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter2.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter2.addAction(Intent.ACTION_MEDIA_EJECT);  //物理的拔出 SDCARD
        filter2.addAction(Intent.ACTION_MEDIA_REMOVED);  //完全拔出
        filter.addDataScheme("file"); // 必须要有此行，否则无法收到广播
        sdcardRecever=new SdcardRecever();
        registerReceiver(sdcardRecever, filter2);
    }

    private void unRegisterReceiver() {
        if (null != mNetWorkReceiver) {
            unregisterReceiver(mNetWorkReceiver);
            mNetWorkReceiver = null;
        }

        if (null !=sdcardRecever ) {
            unregisterReceiver(sdcardRecever);
            sdcardRecever = null;
        }
    }

    public abstract void handleNetWork(String netName, int netType, boolean isHasNetWork);

    @Override
    public void observeNetWork(String netName, int netType, boolean isHasNetWork) {
          LogUtil.e("wulianshu","observeNetWork 被调用");
         if(netType!=-1){
            UpgradeHelper.requestUpgradeData(new RequestUpgradeListener());
         }
          handleNetWork(netName,netType,isHasNetWork);

          //TODO 整理代碼
    }
    class RequestUpgradeListener implements RequestListener<UpgradeInfo> {
        @Override
        public void onResponse(UpgradeInfo result, boolean isCachedData) {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(ConstantUtils.SHARE_APP_TAG, Activity.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            if(result !=null) {
                if (ChaojishipinSplashActivity.isFrist) {
                    edit.putString("ischeck", result.getIscheck());
                    edit.commit();
                    ChaoJiShiPinMainActivity.isCheck = result.getIscheck();
                    ChaoJiShiPinMainActivity.lasttimeCheck = result.getIscheck();
                } else {
                    //上次审核状态
                    ChaoJiShiPinMainActivity.lasttimeCheck = sharedPreferences.getString("ischeck", "1");
                    //本次审核状态
                    ChaoJiShiPinMainActivity.isCheck = result.getIscheck();
                    //上次是非审核状态之后永远都是非审核状态  而且要保存侧滑菜单的状态
                    if("0".equals(ChaoJiShiPinMainActivity.lasttimeCheck)){
                        edit.putString("ischeck", "0");
                        edit.commit();
                        ChaoJiShiPinMainActivity.isCheck = result.getIscheck();;
                    }else{
                        edit.putString("ischeck",result.getIscheck());
                        ChaoJiShiPinMainActivity.isCheck = result.getIscheck();
                        edit.commit();
                    }
                }
            }else{
                //为空为审核状态
                edit.putString("ischeck", "1");
                edit.commit();
                ChaoJiShiPinMainActivity.isCheck = "1";
                ChaoJiShiPinMainActivity.lasttimeCheck = "1";
            }
            LogUtil.e(UpgradeHelper.TAG, "!!!!!!!!!!launch activity requestUpgradeData sucess!!!!!!!!!!");
        }

        @Override
        public void netErr(int errorCode) {
        }

        @Override
        public void dataErr(int errorCode) {
        }
    }


    public void onEventMainThread(Object obj) {

    }

    protected abstract View setContentView();

    protected abstract void handleInfo(Message msg);

    protected void onClickLeftButton() {
    }

    protected void onClickRightButton() {
    }
    //解决系统改变字体大小的时候导致的界面布局混乱的问题
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config,res.getDisplayMetrics() );
        return res;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        System.out.println("我靠我被调用了");
        super.onConfigurationChanged(newConfig);
    }

//    protected void changeFont(ViewGroup root) {
//        Typeface tf = Typeface.createFromAsset(this.getAssets(),
//                "fonts/huawenxinson.ttf");
//        for(int i = 0; i <root.getChildCount(); i++) {
//            View v = root.getChildAt(i);
//            if(v instanceof Button) {
//                ((Button)v).setTypeface(tf);
//            } else if(v instanceof TextView ) {
//                ((TextView)v).setTypeface(tf);
//            } else if(v instanceof EditText) {
//                ((EditText)v).setTypeface(tf);
//            }else if(v instanceof AutoCompleteTextView){
//                ((AutoCompleteTextView)v).setTypeface(tf);
//            }else if(v instanceof ViewGroup) {
//                changeFont((ViewGroup)v);
//            }
//        }
//    }

    public void replaceFragment(int id, Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(id, fragment);
        transaction.commitAllowingStateLoss();
    }

    public void addFragment(int id, Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(id, fragment);
        transaction.commitAllowingStateLoss();
    }

    protected static class UIHandler extends Handler {
        private final WeakReference<ChaoJiShiPinBaseActivity> mFragmentView;

        UIHandler(ChaoJiShiPinBaseActivity view) {
            this.mFragmentView = new WeakReference<ChaoJiShiPinBaseActivity>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            ChaoJiShiPinBaseActivity service = mFragmentView.get();
            if (service != null) {
                try {
                    super.handleMessage(msg);
                    service.handleInfo(msg);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
