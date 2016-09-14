package com.mwdev.sxsmcardpay.controler;

/**
 * Created by xiongxin on 16-8-15.
 */
public interface MessageCallBack {

    /**
     *开始创建socket
     *
     */
    public void onConnectCreated();

    /**
     *socket创建完成
     *
     */
    public void onConnected();

    /**
     *连接断开
     *
     */
    public void onDisconnected();

    /**
     *读写 or 读 or 写 通道处于空闲状态
     *
     */
    public void onCommunictionIdle(String idle);

    /**
     * 通讯异常回调处理
     *
     */
    public void onExceptionCaught(Throwable throwable);

    /**
     *接收远程消息
     *
     */
    public void onMessageReceivered(Object message);

    /**
     *发送远程消息成功回调
     *
     */
    public void onMessageSent(MessageFilter.MessageType type);

    /**
     * response消息过滤结果
     * @param result
     */
    public void onMessageFilterResult(int result);

    /**
     * 连接失败
     */
    public void onConnectFail();

    /**
     * 响应超时
     */
    public void onResponeTimeout(MessageFilter.MessageType type);
}
