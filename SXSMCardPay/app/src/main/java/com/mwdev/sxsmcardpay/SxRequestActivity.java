package com.mwdev.sxsmcardpay;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.mwdev.sxsmcardpay.controler.MessageCallBack;
import com.mwdev.sxsmcardpay.controler.MessageFilter;
import com.mwdev.sxsmcardpay.util.PosLog;

/**
 * 联网需要继承该类
 * Created by xiongxin on 16-8-19.
 */
public abstract class SxRequestActivity extends SxBaseActivity implements MessageCallBack {
    private PosApplication mPosApp;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosApp = (PosApplication) getApplication();
        mPosApp.resigterMessageCallBack(this);


    }

    public void sendRequest(byte[] message,String msgid,String tradeDealCode){
        mPosApp.startUpConnectAndSend(message, msgid, tradeDealCode);
    }

    /**
     * 开始创建socket
     */
    @Override
    public void onConnectCreated() {
        doConnectCreated();
    }

    /**
     * socket创建完成
     */
    @Override
    public void onConnected() {
        doConnected();
    }

    /**
     * 连接断开
     */
    @Override
    public void onDisconnected() {
        onDisconnected();
    }

    /**
     * 读写 or 读 or 写 通道处于空闲状态
     *
     * @param idle
     */
    @Override
    public void onCommunictionIdle(String idle) {
        doCommunictionIdle(idle);
    }

    /**
     * 通讯异常回调处理
     *
     * @param throwable
     */
    @Override
    public void onExceptionCaught(Throwable throwable) {
        doExceptionCaught(throwable);
    }

    /**
     * 接收远程消息
     *
     * @param message
     */
    @Override
    public void onMessageReceivered(Object message) {
        doMessageReceivered(message);
    }

    /**
     * 发送远程消息成功回调
     */
    @Override
    public void onMessageSent(MessageFilter.MessageType type) {
        doonMessageSent(type);
    }

    /**
     * response消息过滤结果
     *
     * @param result
     */
    @Override
    public void onMessageFilterResult(int result) {
        doMessageFilterResult(result);
    }

    /**
     * 连接失败
     */
    @Override
    public void onConnectFail() {
        doConnectFail();
    }

    /**
     * 响应超时
     */
    @Override
    public void onResponeTimeout(MessageFilter.MessageType type) {
        doResponeTimeOut(type);
    }

    protected abstract void doConnectCreated();
    protected abstract void doConnected();
    protected abstract void doDisconnected();
    protected abstract void doCommunictionIdle(String idle);
    protected abstract void doExceptionCaught(Throwable throwable);
    protected abstract void doMessageReceivered(Object message);
    protected abstract void doonMessageSent(MessageFilter.MessageType type);
    protected abstract void doMessageFilterResult(int result);
    protected abstract void doConnectFail();
    protected abstract void doResponeTimeOut(MessageFilter.MessageType type);


}
