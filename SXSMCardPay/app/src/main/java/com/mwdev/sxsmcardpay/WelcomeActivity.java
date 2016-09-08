package com.mwdev.sxsmcardpay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by xiongxin on 16-8-22.
 */
public class WelcomeActivity extends Activity {

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Intent login = new Intent(WelcomeActivity.this,LoginActivity.class);
            startActivity(login);
            finish();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mHandler.sendEmptyMessageDelayed(0,getResources().getInteger(R.integer.welcome_timeout));
    }
}
