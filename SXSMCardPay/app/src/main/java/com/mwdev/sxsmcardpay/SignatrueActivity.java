package com.mwdev.sxsmcardpay;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaTimestamp;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.basewin.aidl.OnPrinterListener;
import com.basewin.services.ServiceManager;
import com.mwdev.sxsmcardpay.util.PosLog;
import com.ta.annotation.TAInjectResource;
import com.ta.annotation.TAInjectView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by eshion on 16-8-30.
 */
public class SignatrueActivity extends SxBaseActivity{

    @TAInjectResource(id = R.string.signatrue_title)
    private String title;
    @TAInjectView(id = R.id.gov)
    private GestureOverlayView mGov;
    @TAInjectView(id = R.id.sign_confirm_button)
    private Button mConfirm;
    @TAInjectView(id = R.id.sign_clean_button)
    private Button mCancel;

    private String mCardNo;
    private String mTradeNo;
    private String mAmt;
    private String mBattch;
    private String mReferenceNum;
    private TextView mTtimer;
    private PosApplication mPosApp;
    private boolean first = true;
    private boolean printerCancel = false;
    private static final int delay = 5;
    private Dialog mDialog;

    private OnPrinterListener mPrinterListener = new OnPrinterListener() {
        @Override
        public void onError(int i, String s) {
            dismissProgressDiglog();
            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFinish() {
            dismissProgressDiglog();
            if(first){
                h.sendEmptyMessage(SETPRINTERTIMERDIALOG);
                mPosApp.getThreadPoolExecutor().execute(new Timer());
                first = false;
            }
            else{
                finish();
            }
        }

        @Override
        public void onStart() {
            setDiglogText(getResources().getString(R.string.printing));
            showProgressDiglog();
        }
    };

    private class Timer implements Runnable{

        private int count = delay;
        /**
         * Starts executing the active part of the class' code. This method is
         * called when a thread is started that has been created with a class which
         * implements {@code Runnable}.
         */
        @Override
        public void run() {
            while (!printerCancel && count > 0){
                Message msg = h.obtainMessage(UPDATE_PRINTER_TIMER);
                msg.arg1 = count;
                msg.sendToTarget();
                count--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(count == 0){
                Message msg = h.obtainMessage(UPDATE_PRINTER_TIMER);
                msg.arg1 = count;
                msg.sendToTarget();
            }
            printerCancel = false;
        }
    }

    private Handler h = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SETPRINTERTIMERDIALOG:
                    showTimerDialog();
                    break;
                case UPDATE_PRINTER_TIMER:
                    if(msg.arg1>0){
                        mTtimer.setText(String.valueOf(msg.arg1));
                    }else{
                        mDialog.dismiss();
                        printer();
                    }
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        mPosApp = (PosApplication) getApplication();
    }

    /**
     * 函数的实现为控件事件的注册
     */
    @Override
    protected void onAfterOnCerate() {
        setAcitvityTitle(title);

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printer();

            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGov.clear(true);
            }
        });

        mCardNo = getIntent().getStringExtra(ReadCardActivity.CARDNUM_KEY);
        mTradeNo = getIntent().getStringExtra(ReadCardActivity.TRADENUM_KEY);
        mBattch = getIntent().getStringExtra(ReadCardActivity.BATCH);
        mReferenceNum = getIntent().getStringExtra(ReadCardActivity.REFERENCENUM);
        mAmt = getIntent().getStringExtra(ReadCardActivity.INPUT_AMOUNT);
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

    }

    private void showTimerDialog(){
        AlertDialog.Builder ab = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog);
        View v =  LayoutInflater.from(this).inflate(R.layout.timer_view,null,false);
        mTtimer = (TextView) v.findViewById(R.id.timerTV);
        ab.setView(v);
        ab.setMessage(R.string.printer_timer_title);

        ab.setNegativeButton(R.string.printer_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                printerCancel = true;
                finish();
            }
        });
        mDialog = ab.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    private Bitmap saveSignature() {
        mGov.setDrawingCacheEnabled(true);
        mGov.setDrawingCacheBackgroundColor(0xffffff);
        Bitmap b = Bitmap.createBitmap(mGov.getDrawingCache());
        mGov.destroyDrawingCache();
        return b;
    }

    private Bitmap compressBitmap(Bitmap Srcbitmap) {

        if (Srcbitmap != null) {
            ByteArrayOutputStream bous = new ByteArrayOutputStream();
            Srcbitmap.compress(Bitmap.CompressFormat.JPEG, 100, bous);
            byte[] data = bous.toByteArray();
            bous.reset();
            BitmapFactory.Options bop = new BitmapFactory.Options();
            bop.inSampleSize = 4;
            Bitmap b = BitmapFactory.decodeByteArray(data,0,data.length,bop);
            return b;
        }
        return null;
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
                ticket.put("content", "消费凭条");
                ticket.put("size", "3");
                ticket.put("position", "center");
                ticket.put("offset", "0");
                ticket.put("bold", "0");
                ticket.put("italic", "0");
                ticket.put("height", "-1");
                // 卡号
                JSONObject cardNo = new JSONObject();
                cardNo.put("content-type", "txt");
                cardNo.put("content", "卡号\t\t\t\t\t\t\t\t\t\t\t\t\t"+mCardNo);
                cardNo.put("size", "2");
                cardNo.put("position", "left");
                cardNo.put("offset", "0");
                cardNo.put("bold", "0");
                cardNo.put("italic", "0");
                cardNo.put("height", "-1");
                // 商户号
                JSONObject cropNo = new JSONObject();
                cropNo.put("content-type", "txt");
                cropNo.put("content", "商户号\t\t\t\t\t\t\t\t\t\t"+((PosApplication)getApplication()).getCropNum());
                cropNo.put("size", "2");
                cropNo.put("position", "left");
                cropNo.put("offset", "0");
                cropNo.put("bold", "0");
                cropNo.put("italic", "0");
                cropNo.put("height", "-1");
                // 终端编号
                JSONObject posNo = new JSONObject();
                posNo.put("content-type", "txt");
                posNo.put("content", "终端号\t\t\t\t\t\t\t\t\t\t"+((PosApplication)getApplication()).getPsamID());
                posNo.put("size", "2");
                posNo.put("position", "left");
                posNo.put("offset", "0");
                posNo.put("bold", "0");
                posNo.put("italic", "0");
                posNo.put("height", "-1");
                // 交易流水号
                JSONObject tradeNo = new JSONObject();
                tradeNo.put("content-type", "txt");
                tradeNo.put("content", "流水号\t\t\t\t\t\t\t\t\t\t"+mTradeNo);
                tradeNo.put("size", "2");
                tradeNo.put("position", "left");
                tradeNo.put("offset", "0");
                tradeNo.put("bold", "0");
                tradeNo.put("italic", "0");
                tradeNo.put("height", "-1");
                // 批次号
                JSONObject battchNo = new JSONObject();
                battchNo.put("content-type", "txt");
                battchNo.put("content", "批次号\t\t\t\t\t\t\t\t\t\t"+mBattch);
                battchNo.put("size", "2");
                battchNo.put("position", "left");
                battchNo.put("offset", "0");
                battchNo.put("bold", "0");
                battchNo.put("italic", "0");
                battchNo.put("height", "-1");
                // 检索号
                JSONObject referenceNo = new JSONObject();
                referenceNo.put("content-type", "txt");
                referenceNo.put("content", "检索号\t\t\t\t\t\t\t\t\t\t"+mReferenceNum);
                referenceNo.put("size", "2");
                referenceNo.put("position", "left");
                referenceNo.put("offset", "0");
                referenceNo.put("bold", "0");
                referenceNo.put("italic", "0");
                referenceNo.put("height", "-1");
                // 消费时间
                JSONObject time = new JSONObject();
                time.put("content-type", "txt");
                time.put("content", "交易时间\t\t\t\t\t\t\t"+formatDateAndTime());
                time.put("size", "2");
                time.put("position", "left");
                time.put("offset", "0");
                time.put("bold", "0");
                time.put("italic", "0");
                time.put("height", "-1");

                // 消费金额
                JSONObject atm = new JSONObject();
                atm.put("content-type", "txt");
                atm.put("content", "RMB\t"+mAmt);
                atm.put("size", "2");
                atm.put("position", "right");
                atm.put("offset", "0");
                atm.put("bold", "0");
                atm.put("italic", "0");
                atm.put("height", "-1");

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

                // 签名头
                JSONObject signatruetitle = new JSONObject();
                signatruetitle.put("content-type", "txt");
                signatruetitle.put("content", "签名  ");
                signatruetitle.put("size", "2");
                signatruetitle.put("position", "left");
                signatruetitle.put("offset", "0");
                signatruetitle.put("bold", "0");
                signatruetitle.put("italic", "0");
                signatruetitle.put("height", "-1");

                // 签名图片
                JSONObject signatrue = new JSONObject();
                signatrue.put("content-type", "jpg");
                signatrue.put("position", "center");

                // 添加二维码打印
                JSONObject td = new JSONObject();
                td.put("content-type", "two-dimension");
                td.put("content", mTradeNo+"-"+mBattch+"-"+mReferenceNum+"-"+mAmt);
                td.put("size", "3");
                td.put("position", "center");

                printContext.put(start);
                printContext.put(ticket);
                printContext.put(cardNo);
                printContext.put(cropNo);
                printContext.put(posNo);
                printContext.put(tradeNo);
                printContext.put(battchNo);
                printContext.put(referenceNo);
                printContext.put(time);
                printContext.put(atm);
                printContext.put(end);
                printContext.put(signatruetitle);
                printContext.put(signatrue);
                printContext.put(td);
                printJson.put("spos", printContext);
                // 设置底部空3行
                // Set at the bottom of the empty 3 rows
                //ServiceManager.getInstence().getPrinter().printBottomFeedLine(3);
                Bitmap[] bitmaps = new Bitmap[] { compressBitmap(saveSignature()) };
                ServiceManager.getInstence().getPrinter().print(printJson.toString(), bitmaps, mPrinterListener);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatDateAndTime(){
        SimpleDateFormat spm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return spm.format(new Date());
    }


}
