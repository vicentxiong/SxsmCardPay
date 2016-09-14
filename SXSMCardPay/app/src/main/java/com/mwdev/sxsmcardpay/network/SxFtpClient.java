package com.mwdev.sxsmcardpay.network;

import android.content.Context;
import android.content.res.Resources;

import com.mwdev.sxsmcardpay.controler.FtpDataTranterCallBack;
import com.mwdev.sxsmcardpay.PosApplication;
import com.mwdev.sxsmcardpay.R;
import com.mwdev.sxsmcardpay.database.PosDataBaseFactory;
import com.mwdev.sxsmcardpay.database.TranslationRecord;
import com.mwdev.sxsmcardpay.iso8583.util;
import com.mwdev.sxsmcardpay.util.PosLog;
import com.mwdev.sxsmcardpay.util.PosUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

/**
 * Created by xiongxin on 16-8-18.
 */
public class SxFtpClient {
    private static final String TAG = SxFtpClient.class.getName();
    private FTPClient mFtp;
    private PosApplication mContext;
    private Resources r;
    private String mCropID;
    private static final String REPART = "batch_POS_";
    private static final String ABS_PATH = "/upload/";
    private String userAndpasswd ;


    private FtpDataTranterCallBack mFtpTranterCallback;

    public SxFtpClient(Context cx){
        mContext = (PosApplication) cx;
        r = mContext.getResources();
        mFtp = new FTPClient();

    }

    private void addFtpTranterCallBack(FtpDataTranterCallBack cb){
        mFtpTranterCallback = cb;
    }

    /**
     *
     * @param uploadFile
     * @param callBack
     *
     * 异步连接并上传文件
     */
    public void startFtpTask(File uploadFile,FtpDataTranterCallBack callBack){
        PosApplication posApp = (PosApplication) mContext;
        posApp.getThreadPoolExecutor().execute(new FtpRequestTask(uploadFile,callBack));
    }

    public File restoreRecord(){
        PosLog.e("xiong","restoreRecord");
        PosDataBaseFactory.getIntance().openPosDatabase();
        List<TranslationRecord> list = PosDataBaseFactory.getIntance().
                query(TranslationRecord.class, null, null, null, null, null);
        PosDataBaseFactory.getIntance().closePosDatabase();
        //文件名
        StringBuffer fileName = new StringBuffer();
        fileName.append("PD").append(list.get(0).getDTLDATE().substring(2)).
                 append(list.get(0).getDTLUNITID()).append(list.get(0).getDTLPOSID()).
                 append(list.get(0).getDTLBATCHNO()).append("H");
        //文件描述域
        StringBuffer fileDescriptionArea = new StringBuffer();
        fileDescriptionArea.append("10").append("\r\n");
        //交易头
        StringBuffer transctionHeader = new StringBuffer();
        transctionHeader.append(list.get(0).getDTLUNITID()).append("000000").append("000000");
        transctionHeader.append(String.format("%05d", list.size())).append(String.format("%010d", list.size()));
        int sum =0;
        for (int i=0;i<list.size();i++){
            sum+=Integer.parseInt(list.get(i).getDTLAMT());
        }

        transctionHeader.append(String.format("%010d",sum));
        transctionHeader.append("0000000000");
        transctionHeader.append("0000000000");
        transctionHeader.append("            ");
        transctionHeader.append("\r\n");

        //交易明细
        StringBuffer fileRecord = new StringBuffer();
        for (int i=0;i<list.size();i++){
            fileRecord.append(list.get(i).getDTLCARDNO()).append(list.get(i).getDTLCDCNT()).
                       append(list.get(i).getDTLPRCODE()).append(list.get(i).getDTLTRANSTYPE()).
                       append(list.get(i).getDTLPOSID()).append(list.get(i).getDTLSAMID()).
                       append(list.get(i).getDTLPOSSEQ()).append(list.get(i).getDTLBATCHNO()).
                       append(list.get(i).getDTLTERMID()).append(list.get(i).getDTLTERMSEQ()).
                       append(list.get(i).getDTLDATE()).append(list.get(i).getDTLTIME()).
                       append(list.get(i).getDTLSETTDATE()).append(list.get(i).getDTLCENSEQ()).
                       append(list.get(i).getDTLSLAMT()).append(list.get(i).getDTLBEFBAL()).
                       append(list.get(i).getDTLAFTBAL()).append(list.get(i).getDTLAMT()).
                       append(list.get(i).getDTLUNITID()).append(list.get(i).getDTLNETID()).
                       append(list.get(i).getDTLTAC());
        }

        File file = new File(mContext.getExternalCacheDir(),fileName.toString());
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(fileDescriptionArea.toString());
            fw.write(transctionHeader.toString());
            fw.write(fileRecord.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PosLog.e("xiong","build file ok " + file.getAbsolutePath()+" file size = " + file.length());
        return file;

    }

    private void ftpConnectAndUpload(File uploadFile,FtpDataTranterCallBack callBack){
        try {
            PosLog.e("xiong","ftpConnectAndUpload start");
            mCropID = String.format("%015s",mContext.getCropNum());
            PosLog.e("xiong","ftpConnectAndUpload start222222");
            userAndpasswd=REPART+mCropID;

            PosLog.e("xiong","userAndpasswd ==" + "batch_POS_330600000000001");

            mFtp.connect(r.getString(R.string.ftp_addresss), r.getInteger(R.integer.ftp_port));
            mFtp.login("batch_POS_330600000000001", "batch_POS_330600000000001");
            mFtp.changeDirectory(ABS_PATH);
            PosLog.e("xiong", "changeDirectory" );
            addFtpTranterCallBack(callBack);
            mFtp.upload(uploadFile,new SxFtpTransterListener());
        } catch (IOException e) {
            PosLog.w(TAG,e.getMessage());
            if(mFtpTranterCallback!=null)
                mFtpTranterCallback.ftpExceptionCaught(e.getCause());
        } catch (FTPIllegalReplyException e) {
            PosLog.w(TAG, e.getMessage());
            if(mFtpTranterCallback!=null)
                mFtpTranterCallback.ftpExceptionCaught(e.getCause());
        } catch (FTPException e) {
            PosLog.w(TAG, e.getMessage());
            if(mFtpTranterCallback!=null)
                mFtpTranterCallback.ftpExceptionCaught(e.getCause());
        } catch (FTPAbortedException e) {
            PosLog.w(TAG, e.getMessage());
            if(mFtpTranterCallback!=null)
                mFtpTranterCallback.ftpExceptionCaught(e.getCause());
        } catch (FTPDataTransferException e) {
            PosLog.w(TAG, e.getMessage());
            if(mFtpTranterCallback!=null)
                mFtpTranterCallback.ftpExceptionCaught(e.getCause());
        }
    }

    class FtpRequestTask implements Runnable{
        private File upLoadFile;
        private FtpDataTranterCallBack cb;

        public FtpRequestTask(File upLoadFile, FtpDataTranterCallBack cb) {
            this.upLoadFile = upLoadFile;
            this.cb = cb;
        }

        @Override
        public void run() {
            if(upLoadFile!=null)
                ftpConnectAndUpload(upLoadFile,cb);
        }
    }


    public void exitFtp(){
        try {
            mFtp.disconnect(true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FTPIllegalReplyException e) {
            e.printStackTrace();
        } catch (FTPException e) {
            e.printStackTrace();
        }
    }
    /**
     * FTP数据传输监听器
     */
    private class SxFtpTransterListener implements FTPDataTransferListener{
        // 文件开始上传或下载时触发
        @Override
        public void started() {
            if(mFtpTranterCallback!=null)
                mFtpTranterCallback.ftpStart();
        }
        // 显示已经传输的字节数
        @Override
        public void transferred(int i) {
            if(mFtpTranterCallback!=null)
                mFtpTranterCallback.ftpTransferred(i);
        }
        // 文件传输完成时，触发
        @Override
        public void completed() {
            if(mFtpTranterCallback!=null)
                mFtpTranterCallback.ftpCompleted();
            exitFtp();
        }
        // 传输放弃时触发
        @Override
        public void aborted() {
            if(mFtpTranterCallback!=null)
                mFtpTranterCallback.ftpAborted();
            exitFtp();
        }
        // 传输失败时触发
        @Override
        public void failed() {
            if(mFtpTranterCallback!=null)
                mFtpTranterCallback.ftpFailed();
            exitFtp();
        }
    }
}
