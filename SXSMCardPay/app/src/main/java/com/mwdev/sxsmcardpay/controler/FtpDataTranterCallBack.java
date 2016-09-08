package com.mwdev.sxsmcardpay.controler;

/**
 * Created by xiongxin on 16-8-18.
 */
public interface FtpDataTranterCallBack {

    public void ftpStart();

    public void ftpTransferred(int length);

    public void ftpCompleted();

    public void ftpAborted();

    public void ftpFailed();

    public void ftpExceptionCaught(Throwable throwable);
}
