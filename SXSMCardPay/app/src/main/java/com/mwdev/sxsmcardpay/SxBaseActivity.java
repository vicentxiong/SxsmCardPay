package com.mwdev.sxsmcardpay;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
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

import com.basewin.aidl.OnPrinterListener;
import com.basewin.services.ServiceManager;
import com.mwdev.sxsmcardpay.util.PosLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiongxin on 16-8-18.
 * 作为所有Activity的基类
 */
public abstract class SxBaseActivity extends Activity implements PosApplication.PosNetworkStatusListener{
    private static final int SHOWPROGRESSBAR = 0;
    private static final int DISMISSPROGRESSBAR = 1;
    private static final int TOAST_KEY = 2;
    public static final int PROGRESSBAR_UPDATE = 3;
    public static final int OPENPROGRESSBAR_DIALOG = 4;
    public static final int DIALOG_TEXT_UPDATE = 5;
    public static final int SETPROGRESSBAR_SIZE = 6;
    private ImageView mActionBarLeft;
    private ImageView mActionBarRight;
    private TextView mTitle;

    private WindowManager mWinMgr;
    private PosApplication mPosApp;
    WindowManager.LayoutParams mWindowLayoutParams;
    private View mProgress;
    private TextView mAttention;
    private boolean showProgress=false;
    private boolean isPrinterList;
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
                case SxBaseActivity.DIALOG_TEXT_UPDATE:
                    setDiglogText(getResources().getString(msg.arg1));
                    break;
            }
        }
    };

    private OnPrinterListener mPrinterListener = new OnPrinterListener() {
        @Override
        public void onError(int i, String s) {
            dismissProgressDiglog();
            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFinish() {
            dismissProgressDiglog();
        }

        @Override
        public void onStart() {
            setDiglogText(getResources().getString(R.string.printing));
            showProgressDiglog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWinMgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mProgress = LayoutInflater.from(getApplication()).inflate(R.layout.diglog_progressbar_view, null, false);
        mAttention = (TextView) mProgress.findViewById(R.id.attention);
        initProgress();
        mPosApp = (PosApplication) getApplication();
        mPosApp.addNetworkStatusListener(this);
        isPrinterList = getResources().getInteger(R.integer.pos_malfuntion_list)==1;
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
     * 在消息队列中处理dialog text
     * @param resid
     */
    public void onHandlerDialogText(int resid){
        Message msg = mhandler.obtainMessage(DIALOG_TEXT_UPDATE);
        msg.arg1=resid;
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

    @Override
    public void onNetworkStatus(boolean connect) {
        if(connect){
            mPosApp.getSocketClient().getFilter().postDBFlushesTask(this);
        }
    }

    public void DBFlushesStart(){
        setAcitvityTitle(getResources().getString(R.string.db_flushes));
        showProgressDiglog();
    }

    public void DBFlushesStop(int resId){
        dismissProgressDiglog();
        onHanderToast(resId);
        if(resId==R.string.flushes_respone_timeout){
            printerMalFuctoinList();
        }
    }

    protected void printerMalFuctoinList(){
        if(isPrinterList){
            printer();
        }
    }

    private void printer(){
        try {
            JSONObject printJson = new JSONObject();
            //ServiceManager.getInstence().getPrinter().setPrintGray(2000);
            ServiceManager.getInstence().getPrinter().setLineSpace(16);
            try {
                // 組打印json字符串
                JSONArray printContext = new JSONArray();
                //开头
                JSONObject start = new JSONObject();
                start.put("content-type", "txt");
                start.put("content", "*********************");
                start.put("size", "3");
                start.put("position", "left");
                start.put("offset", "0");
                start.put("bold", "0");
                start.put("italic", "0");
                start.put("height", "-1");

                // 小票
                JSONObject ticket = new JSONObject();
                ticket.put("content-type", "txt");
                ticket.put("content", "故障报告单");
                ticket.put("size", "3");
                ticket.put("position", "center");
                ticket.put("offset", "0");
                ticket.put("bold", "0");
                ticket.put("italic", "0");
                ticket.put("height", "-1");
                // 人工处理
                JSONObject actoin = new JSONObject();
                actoin.put("content-type", "txt");
                actoin.put("content", "冲正不成功，请人工处理");
                actoin.put("size", "2");
                actoin.put("position", "left");
                actoin.put("offset", "0");
                actoin.put("bold", "0");
                actoin.put("italic", "0");
                actoin.put("height", "-1");

                //结束
                JSONObject end = new JSONObject();
                end.put("content-type", "txt");
                end.put("content", "*********************");
                end.put("size", "3");
                end.put("position", "left");
                end.put("offset", "0");
                end.put("bold", "0");
                end.put("italic", "0");
                end.put("height", "-1");



                printContext.put(start);
                printContext.put(ticket);
                printContext.put(actoin);
                printContext.put(end);
                printJson.put("spos", printContext);
                // 设置底部空3行
                // Set at the bottom of the empty 3 rows
                //ServiceManager.getInstence().getPrinter().printBottomFeedLine(3);

                ServiceManager.getInstence().getPrinter().print(printJson.toString(), null, mPrinterListener);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
