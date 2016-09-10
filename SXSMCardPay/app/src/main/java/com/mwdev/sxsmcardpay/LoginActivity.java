package com.mwdev.sxsmcardpay;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mwdev.sxsmcardpay.controler.MessageFilter;
import com.mwdev.sxsmcardpay.iso8583.Iso8583Mgr;
import com.mwdev.sxsmcardpay.util.PosLog;
import com.ta.annotation.TAInject;
import com.ta.annotation.TAInjectResource;
import com.ta.annotation.TAInjectView;
import com.ta.util.config.TAIConfig;

/**
 * @created by xiongxin 20160820
 */
public class LoginActivity extends SxRequestActivity{
    @TAInjectView(id = R.id.cropname)
    private AutoCompleteTextView mCropName;
    @TAInjectView(id = R.id.password)
    private EditText mPasswd;
    @TAInjectView(id = R.id.pos_sign_in_button)
    private Button mLogin;
    @TAInjectView(id = R.id.repasswd)
    private CheckBox mRepasswd;
    @TAInjectResource(id = R.string.actionbar_login)
    private String title;
    @TAInjectResource(id = R.string.save_checkbox_status_key)
    private String checkbox_status_key;
    @TAInjectResource(id = R.string.save_cropname_key)
    private String cropname_key;
    @TAInjectResource(id = R.string.save_passwd_key)
    private String passwd_key;

    private TAIConfig mConfig;
    private PosApplication myPosApplication;
    private Iso8583Mgr myIso8583Mgr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loadConfig();
    }

    private void loadConfig(){
        myPosApplication=(PosApplication)getApplication();
        myIso8583Mgr=myPosApplication.getmIso8583Mgr();
        mConfig = myPosApplication.getConfig(PosApplication.PREFERENCECONFIG);
        if(mConfig.getInt(checkbox_status_key,0)==1){
            mCropName.setText(mConfig.getString(cropname_key, ""));
            mPasswd.setText(mConfig.getString(passwd_key,""));
            mRepasswd.setChecked(true);
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
        dismissProgressDiglog();
        saveConfig();
        if(myIso8583Mgr.getManager_unpackData().getBit("62")!=null){
            Intent i =new Intent(this,MainMenuActivity.class);
            startActivity(i);
            finish();
        }else{
            onHanderToast(R.string.checkin_fail);
        }

    }

    @Override
    protected void doonMessageSent() {

    }

    @Override
    protected void doMessageFilterResult(int result) {
        dismissProgressDiglog();
        onHanderToast(myPosApplication.getStringIdByCode(result));
    }

    @Override
    protected void doConnectFail() {

    }

    @Override
    protected void doResponeTimeOut(MessageFilter.MessageType type) {

    }

    /**
     * 函数的实现为控件事件的注册
     */
    @Override
    protected void onAfterOnCerate() {
        setAcitvityTitle(title);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDiglogText(getResources().getString(R.string.checkin_now));
                LoginActivity.this.showProgressDiglog();
                sendRequest(myIso8583Mgr.checkIn(myPosApplication.getPsamID(),mCropName.getText().toString().trim()),"0800","000000");
            }
        });

    }

    private void saveConfig(){
        String corpName = mCropName.getText().toString().trim();
        String pwd = mPasswd.getText().toString().trim();
        if(mRepasswd.isChecked()){
            mConfig.setInt(checkbox_status_key,1);
            mConfig.setString(cropname_key,corpName);
            mConfig.setString(passwd_key,pwd);
        }else{
            mConfig.clear();
        }
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
}