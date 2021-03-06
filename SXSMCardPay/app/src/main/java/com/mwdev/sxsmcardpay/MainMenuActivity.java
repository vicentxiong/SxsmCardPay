package com.mwdev.sxsmcardpay;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.basewin.services.PinpadBinder;
import com.basewin.services.ServiceManager;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.mwdev.sxsmcardpay.controler.FtpDataTranterCallBack;
import com.mwdev.sxsmcardpay.controler.MessageFilter;
import com.mwdev.sxsmcardpay.database.PosDataBaseFactory;
import com.mwdev.sxsmcardpay.database.TranslationRecord;
import com.mwdev.sxsmcardpay.iso8583.Iso8583Mgr;
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
    private AlertDialog.Builder ab;
    private View fileDialogroot;
    private NumberProgressBar mDataUploadBar;
    private File mRecordFile;

    public final static int BALANCE_QUERY=0;
    public final static int TRADE=1;
    public final static int TRADE_CANCEL=2;
    public final static int RETURN_GOODS=3;
    public final static String TYPE_KEY="type_key";

    private Handler mH = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SxBaseActivity.PROGRESSBAR_UPDATE:
                    mDataUploadBar.setProgress(msg.arg1);
                    break;
                case SxBaseActivity.OPENPROGRESSBAR_DIALOG:
                    showFileUploadProgressDialog();
                    break;

                case SxBaseActivity.SETPROGRESSBAR_SIZE:
                    PosLog.d("xx", "SETPROGRESSBAR_SIZE start");
                    mDataUploadBar.setMax(msg.arg1);
                    PosLog.d("xx", "SETPROGRESSBAR_SIZE stop");
                    break;
            }

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu_activity);
        myPosApplication = (PosApplication) getApplication();
        fileDialogroot = LayoutInflater.from(this).inflate(R.layout.fileupload_progressbar,null,false);
        mDataUploadBar = (NumberProgressBar) fileDialogroot.findViewById(R.id.progressBar);
        //
        getkey_local();
    }


    //
    public void getkey_local(){
        Intent intent=getIntent();
        boolean intent_tag=intent.getBooleanExtra(Iso8583Mgr.INTENT_TAG,false);
        if(intent_tag){
            String pik=mConfig.getString(Iso8583Mgr.PIK_LOCAL,null);
            String mak=mConfig.getString(Iso8583Mgr.MAK_LOCAL,null);
            Log.i("qiuqiu","pik==========>"+pik+"\nmak=========>"+mak);
            if(pik!=null&&mak!=null){
                PinpadBinder pinpad = null;
                try {
                    pinpad = ServiceManager.getInstence().getPinpad();
                    pinpad.loadPinKey(pik,null);
                    pinpad.loadMacKey(mak, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
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
        myPosApplication.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                myPosApplication.getSocketClient().getFilter().clearAllDBFlushes();
            }
        });
        dismissProgressDiglog();
       // myPosApplication.exitApplication();
        mConfig.setInt(Iso8583Mgr.IS_CHECKOUT,0);
        finish();
    }

    @Override
    protected void doonMessageSent(MessageFilter.MessageType type) {
        if(type.equals(myPosApplication.getSocketClient().getFilter().CHEKCOUT_REQUEST_TYPE)){
            onHandlerDialogText(R.string.check_out);
            showProgressDiglog();
        }
    }

    @Override
    protected void doMessageFilterResult(int result) {

        PosLog.e("xx","result == " + result);
        switch (result){
            case 0xF0:
                loadDBRecordFile();
                startFtpFileUpload();
                break;
            default:
                onHanderToast(myPosApplication.getStringIdByCode(result));
                dismissProgressDiglog();
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

    private void loadDBRecordFile(){
        mRecordFile = myPosApplication.restoreFtpFile();
    }

    private void startFtpFileUpload() {

        if(mRecordFile!=null){
            PosLog.d("xx", "file length == " + mRecordFile.length());
            Message msg = mH.obtainMessage(SxBaseActivity.SETPROGRESSBAR_SIZE);
            msg.arg1 = (int) mRecordFile.length();
            msg.sendToTarget();
            myPosApplication.startFtpConnectAndUpload(mRecordFile, MainMenuActivity.this);
        }else{
            sendRequest(myPosApplication.getmIso8583Mgr().checkOut(myPosApplication.getPsamID(),
                    myPosApplication.getCropNum()), "0820", "000000");
        }

//        for (int i = 1; i <= 100; i++) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            Message msg1 = mH.obtainMessage(SxBaseActivity.PROGRESSBAR_UPDATE);
//            msg1.arg1 = i;
//            msg1.sendToTarget();
//        }
    }


    private void showFileUploadProgressDialog() {
        if(ab==null){
            ab = new AlertDialog.Builder(this);
            ab.setView(fileDialogroot);
            mDialog = ab.create();
            mDialog.setCanceledOnTouchOutside(false);
        }

        mDialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        myPosApplication.resigterMessageCallBack(this);
        myPosApplication.addBatteryListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        PosLog.d("xx", "onNewIntent ");
        super.onNewIntent(intent);
        boolean batteryCheckout = intent.getIntExtra("battery",0) == 1;
        PosLog.d("xx", "batteryCheckout =  " + batteryCheckout);
        if(batteryCheckout){
            setDiglogText(getResources().getString(R.string.checkout_dialog_statement));
            showProgressDiglog();

            myPosApplication.getThreadPoolExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    loadDbDataAndSend();
                }
            });
            isfirstCheckout = false;
        }
    }

    @Override
    protected void doBatteryCheckout() {
        setDiglogText(getResources().getString(R.string.checkout_dialog_statement));
        showProgressDiglog();

        myPosApplication.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                loadDbDataAndSend();
            }
        });
        isfirstCheckout = false;
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

    }

    /**
     * actionbar right by onclick
     */
    @Override
    protected void doActionBarRightClick() {
        showCheckoutDialog();
    }

    private void loadDbDataAndSend() {
        PosLog.d("xx", "loadDbDataAndSend...");
        int debit_sumTrade =0,debit_sumAmount =0;
        int crebit_sumTrade = 0,crebit_sumAmount=0;
        PosDataBaseFactory.getIntance().openPosDatabase();
        List<TranslationRecord> list =
                PosDataBaseFactory.getIntance().query(TranslationRecord.class, null, null, null, null, null);
        PosDataBaseFactory.getIntance().closePosDatabase();
        int N = list.size();
        PosLog.d("xx","record size == "+ N);

        for (int i=0;i<N;i++){
            String type = list.get(i).getDTLTYPE();
            if(type.equals(MessageFilter.DEBIT_TYPE)){
                debit_sumTrade++;
                debit_sumAmount+=Integer.parseInt(list.get(i).getDTLAMT());
            }else if(type.equals(MessageFilter.CREDIT_TYPE)){
                crebit_sumTrade++;
                crebit_sumAmount+=Integer.parseInt(list.get(i).getDTLAMT());
            }

        }

        String req_debit_amount = String.format("%012d", debit_sumAmount);
        String req_debit_sum = String.format("%03d", debit_sumTrade);
        String req_crebit_amount = String.format("%012d", crebit_sumAmount);
        String req_crebit_sum = String.format("%03d", crebit_sumTrade);
        String req_tradeNo = myPosApplication.createTradeSerialNumber();
        String req_psamId = myPosApplication.getPsamID();
        String req_cropNo = myPosApplication.getCropNum();
//        req_debit_sum="010";  //测试对账不平
        PosLog.d("xx", "req_debit_sum : " + req_debit_sum + "  req_debit_amount : " + req_debit_amount);
        PosLog.d("xx", "req_crebit_sum : " + req_crebit_sum + "  req_crebit_amount : " + req_crebit_amount);

        sendRequest(myPosApplication.getmIso8583Mgr().batch_settlement(
                req_debit_amount,req_debit_sum,req_crebit_amount,req_crebit_sum,req_tradeNo,req_psamId,req_cropNo,"111111"), "0500", "000000");
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
                isfirstCheckout =false;
            }
        });

        ab.setNegativeButton(R.string.checkout_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendRequest(myPosApplication.getmIso8583Mgr().checkOut(myPosApplication.getPsamID(), myPosApplication.getCropNum()), "0820", "000000");
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
                Intent intent1=new Intent(MainMenuActivity.this,InputtradeNum.class);
                startActivity(intent1);
                break;
            case R.id.returngoods:
                mConfig.setInt(TYPE_KEY,RETURN_GOODS);
                Intent intent2=new Intent(MainMenuActivity.this,InputtradeNum.class);
                startActivity(intent2);
                break;
        }
    }


    @Override
    public void ftpStart() {
        PosLog.e("xx", "ftpStart");
        dismissProgressDiglog();
        Message msg = mH.obtainMessage(SxBaseActivity.OPENPROGRESSBAR_DIALOG);
        msg.sendToTarget();
    }

    @Override
    public void ftpTransferred(int length) {
        Message msg = mH.obtainMessage(SxBaseActivity.PROGRESSBAR_UPDATE);
        msg.arg1 = length;
        msg.sendToTarget();
    }

    @Override
    public void ftpCompleted() {
        disMissUploadDialog();
        myPosApplication.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                myPosApplication.getSocketClient().getFilter().clearTransctionRecords();
                if (mRecordFile != null) {
                    mRecordFile.delete();
                }
            }
        });
        sendRequest(myPosApplication.getmIso8583Mgr().checkOut(myPosApplication.getPsamID(),
                myPosApplication.getCropNum()), "0820", "000000");
    }

    @Override
    public void ftpAborted() {
        disMissUploadDialog();
        onHanderToast(R.string.ftp_upload_fail);
    }

    @Override
    public void ftpFailed() {
        disMissUploadDialog();
        onHanderToast(R.string.ftp_upload_fail);
    }

    @Override
    public void ftpExceptionCaught(Throwable throwable) {
        disMissUploadDialog();
        onHanderToast(R.string.ftp_upload_fail);
    }

    private void disMissUploadDialog(){
        if(mDialog!=null){
            mDialog.dismiss();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            disMissUploadDialog();
        }
       // super.onKeyDown(keyCode, event);
        return false;
    }
}
