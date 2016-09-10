package com.mwdev.sxsmcardpay;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mwdev.sxsmcardpay.util.PosLog;

/**
 * Created by xiongxin on 16-8-18.
 * 作为所有Activity的基类
 */
public abstract class SxBaseActivity extends Activity {
    private static final int SHOWPROGRESSBAR = 0;
    private static final int DISMISSPROGRESSBAR = 1;
    private static final int TOAST_KEY = 2;
    private ImageView mActionBarLeft;
    private ImageView mActionBarRight;
    private TextView mTitle;

    private WindowManager mWinMgr;
    WindowManager.LayoutParams mWindowLayoutParams;
    private View mProgress;
    private TextView mAttention;
    private boolean showProgress=false;
    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SHOWPROGRESSBAR:
                    onHandlerProgressbarShow();
                    break;
                case DISMISSPROGRESSBAR:
                    onHandlerProgressbarDismiss();
                    break;
                case TOAST_KEY:
                    Toast.makeText(getApplicationContext(),getResources().getString(msg.arg1),Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWinMgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mProgress = LayoutInflater.from(getApplication()).inflate(R.layout.diglog_progressbar_view, null, false);
        mAttention = (TextView) mProgress.findViewById(R.id.attention);
        initProgress();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initView();
        initActionBar();
        onAfterOnCerate();
    }

    public void initActionBar(){
        ActionBar bar = getActionBar();
        bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setDisplayShowCustomEnabled(true);
        bar.setCustomView(R.layout.actionbar_custom_view);
        mActionBarLeft = (ImageView) bar.getCustomView().findViewById(R.id.actionbar_left);
        mActionBarRight = (ImageView) bar.getCustomView().findViewById(R.id.actionbar_right);
        mTitle = (TextView) bar.getCustomView().findViewById(R.id.title);
        ActionBarClickListener l = new ActionBarClickListener();
        mActionBarLeft.setOnClickListener(l);
        mActionBarRight.setOnClickListener(l);
    }

    /**
     * 设置对话框文本
     */
    public void setDiglogText(String text){
        mAttention.setText(text);
    }

    /**
     *
     * 在消息队列中处理 toast 提示
     * @param resid
     */
    public void onHanderToast(int resid){
        Message msg = mhandler.obtainMessage();
        msg.what = TOAST_KEY;
        msg.arg1 = resid;
        msg.sendToTarget();
    }

    /**
     * 设置界面title
     * @param title
     */
    public void setAcitvityTitle(String title){
        mTitle.setText(title);
    }

    /**
     * 设置actionbar left button 是否可见
     * @param visitity
     */
    public void setmActionBarLeftVisitity(int visitity){
        mActionBarLeft.setVisibility(visitity);
    }

    /**
     *
     * 设置actionbar right button 是否可见
     * @param visitity
     */
    public void setmActionBarRightVisitity(int visitity){
        mActionBarRight.setVisibility(visitity);
    }


    /**
     *初始化所有控件
     * 子类中所有加有 @TAInjectView 注解的
     *
     */
    public void initView(){
        PosApplication posApp = (PosApplication) getApplication();
        posApp.getInjector().injectView(this);
        posApp.getInjector().injectResource(this);
    }

    /**
     * 函数的实现为控件事件的注册
     *
     */
    protected abstract void onAfterOnCerate();

    /**
     * actionbar left by onclick
     */
    protected abstract void doActionBarLeftClick();

    /**
     * actionbar right by onclick
     */
    protected abstract void doActionBarRightClick();

    private class ActionBarClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.actionbar_left:
                    doActionBarLeftClick();
                    break;
                case R.id.actionbar_right:
                    doActionBarRightClick();
                    break;

            }

        }
    }

    private void initProgress(){
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; //图片之外的其他地方透明
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowLayoutParams.x = 0;
        mWindowLayoutParams.y = 0;
        mWindowLayoutParams.alpha = 1f; //透明度
        mWindowLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mWindowLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS ;
        mWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;

    }

    private void onHandlerProgressbarShow(){
        if(!showProgress){
            mWinMgr.addView(mProgress,mWindowLayoutParams);
            showProgress=true;
        }
    }

    private void onHandlerProgressbarDismiss(){
        PosLog.d("xx", "dismissProgressDiglog start = " + showProgress);
        if(showProgress){
            mWinMgr.removeView(mProgress);
            showProgress=false;
        }
        PosLog.d("xx", "dismissProgressDiglog stop");
    }


    /**
     * 打开进度条对话框，在请求服务器时
     */
    public void showProgressDiglog(){
        mhandler.sendEmptyMessage(SHOWPROGRESSBAR);
    }

    /**
     * 关闭进度条对话框，在请求服务器时
     */
    public void dismissProgressDiglog(){
        mhandler.sendEmptyMessage(DISMISSPROGRESSBAR);
    }

    /**
     * back key 需要先关progressbar
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(showProgress){
                mWinMgr.removeView(mProgress);
                showProgress=false;
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


}
