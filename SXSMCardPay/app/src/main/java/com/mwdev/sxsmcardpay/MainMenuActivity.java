package com.mwdev.sxsmcardpay;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mwdev.sxsmcardpay.controler.FtpDataTranterCallBack;
import com.mwdev.sxsmcardpay.controler.MessageFilter;
import com.mwdev.sxsmcardpay.database.PosDataBaseFactory;
import com.mwdev.sxsmcardpay.database.TranslationRecord;
import com.mwdev.sxsmcardpay.util.PosLog;
import com.ta.annotation.TAInjectResource;
import com.ta.annotation.TAInjectView;
import com.ta.util.config.TAIConfig;

import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;

/**
 * Created by xiongxin on 16-8-22.
 */
public class MainMenuActivity extends SxRequestActivity implements View.OnClickListener,FtpDataTranterCallBack{
    @TAInjectView(id = R.id.balance)
    private ImageButton mBalance;
    @TAInjectView(id = R.id.scale)
    private ImageButton mScale;
    @TAInjectView(id = R.id.scalerevoke)
    private ImageButton mScaleRevoke;
    @TAInjectView(id = R.id.returngoods)
    private ImageButton mReturnGoods;
    @TAInjectResource(id = R.string.actoinbar_mainmenu)
    private String title;
    //balance_query
    private TAIConfig mConfig;
    private PosApplication myPosApplication;
    private Dialog mDialog;
    private View fileDialogroot;
    private ProgressBar mDataUploadBar;

    public final static int BALANCE_QUERY=0;
    public final static int TRADE=1;
    public final static int TRADE_CANCEL=2;
    public final static int RETURN_GOODS=3;
    public final static String TYPE_KEY="type_key";

    private Handler mH = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mDataUploadBar.setProgress(msg.arg1);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu_activity);
        myPosApplication = (PosApplication) getApplication();
        fileDialogroot = LayoutInflater.from(this).inflate(R.layout.fileupload_progressbar,null,false);
        mDataUploadBar = (ProgressBar) fileDialogroot.findViewById(R.id.progressBar);
    }

    @Override
    protected void doConnectCreated() {

    }

    @Override
    protected void doConnected() {

    }

    @Override
    protected void doDisconnected() {

    }

    @Override
    protected void doCommunictionIdle(String idle) {

    }

    @Override
    protected void doExceptionCaught(Throwable throwable) {

    }

    @Override
    protected void doMessageReceivered(Object message) {
        dismissProgressDiglog();
        finish();
    }

    @Override
    protected void doonMessageSent() {

    }

    @Override
    protected void doMessageFilterResult(int result) {
        dismissProgressDiglog();
        switch (result){
            case -2:
                onHanderToast(R.string.mac_auth_fail);
                break;
            case 0x79:
                startFtpFileUpload();
                break;
        }
    }

    @Override
    protected void doConnectFail() {
        dismissProgressDiglog();
        onHanderToast(R.string.pos_center_connect_fail);
    }

    @Override
    protected void doResponeTimeOut(MessageFilter.MessageType type) {
        dismissProgressDiglog();
        onHanderToast(R.string.pos_center_respone_timeout);
    }

    private void startFtpFileUpload(){
        File file = myPosApplication.restoreFtpFile();
        mDataUploadBar.setMax((int)file.length());
        myPosApplication.startFtpConnectAndUpload(file, this);
    }

    private void showFileUploadProgressDialog(){
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setView(fileDialogroot);
        mDialog = ab.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    /**
     * 函数的实现为控件事件的注册
     */
    @Override
    protected void onAfterOnCerate() {
        setAcitvityTitle(title);
        myPosApplication=(PosApplication) getApplication();
        mConfig=myPosApplication.getConfig(PosApplication.PREFERENCECONFIG);
        mBalance.setOnClickListener(this);
        mScale.setOnClickListener(this);
        mScaleRevoke.setOnClickListener(this);
        mReturnGoods.setOnClickListener(this);
    }

    /**
     * actionbar left by onclick
     */
    @Override
    protected void doActionBarLeftClick() {
        showFileUploadProgressDialog();
    }

    /**
     * actionbar right by onclick
     */
    @Override
    protected void doActionBarRightClick() {
        showCheckoutDialog();
    }

    private void loadDbDataAndSend(){
        PosLog.d("xx","loadDbDataAndSend...");
        int sumTrade =0,sumAmount =0;
        PosDataBaseFactory.getIntance().openPosDatabase();
        List<TranslationRecord> list =
                PosDataBaseFactory.getIntance().query(TranslationRecord.class, null, null, null, null, null);
        PosDataBaseFactory.getIntance().closePosDatabase();
        int N = list.size();
        PosLog.d("xx","record size == "+ N);
        sumTrade = N;
        for (int i=0;i<N;i++){
            sumAmount+=Integer.parseInt(list.get(i).getDTLAMT());
        }
        String req_amount = String.format("%012d", sumAmount);
        String req_sum = String.format("%06d", sumTrade);
        String req_tradeNo = myPosApplication.createTradeSerialNumber();
        String req_psamId = myPosApplication.getPsamID();
        String req_cropNo = myPosApplication.getCropNum();
        PosLog.d("xx","3333333333333");
        sendRequest(myPosApplication.getmIso8583Mgr().batch_settlement(
                req_amount,req_sum,req_tradeNo,req_psamId,req_cropNo,"111111"), "0500", "000000");
    }


    private void showCheckoutDialog(){
        AlertDialog.Builder ab = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog);
        ab.setMessage(R.string.checkout_dialog_message);
        ab.setPositiveButton(R.string.checkout_dialog_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                setDiglogText(getResources().getString(R.string.checkout_dialog_statement));
                showProgressDiglog();

                myPosApplication.getThreadPoolExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        loadDbDataAndSend();
                    }
                });
            }
        });

        ab.setNegativeButton(R.string.checkout_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        Dialog dialog = ab.create();
        dialog.show();
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.balance:
                mConfig.setInt(TYPE_KEY,BALANCE_QUERY);
                Intent i=new Intent(MainMenuActivity.this,ReadCardActivity.class);
                startActivity(i);
                break;
            case R.id.scale:
                mConfig.setInt(TYPE_KEY,TRADE);
                Intent intent=new Intent(MainMenuActivity.this,InputAmountNumActivity.class);
                startActivity(intent);
                break;
            case R.id.scalerevoke:
                mConfig.setInt(TYPE_KEY,TRADE_CANCEL);
                Intent intent1=new Intent(MainMenuActivity.this,InputOperatorNum.class);
                startActivity(intent1);
                break;
            case R.id.returngoods:
                mConfig.setInt(TYPE_KEY,RETURN_GOODS);
                Intent intent2=new Intent(MainMenuActivity.this,InputOperatorNum.class);
                startActivity(intent2);
                break;
        }
    }


    @Override
    public void ftpStart() {
        showFileUploadProgressDialog();
    }

    @Override
    public void ftpTransferred(int length) {
        Message msg = mH.obtainMessage();
        msg.arg1 = length;
        mH.sendMessage(msg);
    }

    @Override
    public void ftpCompleted() {
        if(mDialog!=null)
            mDialog.dismiss();
        mDialog=null;
    }

    @Override
    public void ftpAborted() {
        if(mDialog!=null)
            mDialog.dismiss();
        mDialog=null;
    }

    @Override
    public void ftpFailed() {
        if(mDialog!=null)
            mDialog.dismiss();
        mDialog=null;
    }

    @Override
    public void ftpExceptionCaught(Throwable throwable) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(mDialog!=null)
                mDialog.dismiss();
        }
        return super.onKeyDown(keyCode, event);
    }
}
