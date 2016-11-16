package com.mwdev.sxsmcardpay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mwdev.sxsmcardpay.iso8583.Iso8583Mgr;
import com.ta.util.config.TAIConfig;

/**
 * Created by xiongxin on 16-8-22.
 */
public class WelcomeActivity extends Activity {
    private TAIConfig mConfig;
    private PosApplication myPosApplication;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Intent login;
            if(mConfig.getInt(Iso8583Mgr.IS_CHECKOUT,0)==1){
                Log.i("qiuqiu","mConfig.getInt(Iso8583Mgr.IS_CHECKOUT,0)==1");
                login = new Intent(WelcomeActivity.this,MainMenuActivity.class);
                login.putExtra(Iso8583Mgr.INTENT_TAG,true);
            }else {
                login = new Intent(WelcomeActivity.this,LoginActivity.class);
            }

            startActivity(login);
            finish();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        myPosApplication=(PosApplication)getApplication();
        mConfig = myPosApplication.getConfig(PosApplication.PREFERENCECONFIG);
        mHandler.sendEmptyMessageDelayed(0,getResources().getInteger(R.integer.welcome_timeout));
    }
}
