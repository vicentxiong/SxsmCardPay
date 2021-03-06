package com.mwdev.sxsmcardpay.network;

import android.content.Context;
import android.content.res.Resources;

import com.mwdev.sxsmcardpay.controler.FtpDataTranterCallBack;
import com.mwdev.sxsmcardpay.PosApplication;
import com.mwdev.sxsmcardpay.R;
import com.mwdev.sxsmcardpay.controler.MessageFilter;
import com.mwdev.sxsmcardpay.database.PosDataBaseFactory;
import com.mwdev.sxsmcardpay.database.TranslationRecord;
import com.mwdev.sxsmcardpay.util.PosLog;
import com.mwdev.sxsmcardpay.util.PosUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        PosLog.e("xx","restoreRecord");
        PosDataBaseFactory.getIntance().openPosDatabase();
        List<TranslationRecord> list = PosDataBaseFactory.getIntance().
                query(TranslationRecord.class, null, null, null, null, null);
        PosDataBaseFactory.getIntance().closePosDatabase();
        if(list==null || list.size()==0){
            PosLog.d("xx","no transctoin record ");
            return null;
        }
        //文件名
        StringBuffer fileName = new StringBuffer();
        fileName.append("PD").append(list.get(0).getDTLDATE().substring(2)).
                 append(list.get(0).getDTLUNITID()).append(list.get(0).getDTLPOSID()).
                 append(list.get(0).getDTLBATCHNO()).append("H").append("00").append(".txt");
        //文件描述域
        StringBuffer fileDescriptionArea = new StringBuffer();
        fileDescriptionArea.append("10").append("\r\n");
        //交易头
        StringBuffer transctionHeader = new StringBuffer();
        transctionHeader.append(list.get(0).getDTLUNITID()).append(",").append(getMinDate(list)).append(",").append(getMaxDate(list)).append(",");
        transctionHeader.append(String.format("%05d", list.size())).append(",");
        int debit_sum =0,debit_amount=0;
        int crebit_sum=0,crebit_amount=0;
        for (int i=0;i<list.size();i++){
            String type = list.get(i).getDTLTYPE();
            if(type.equals(MessageFilter.DEBIT_TYPE)){
                debit_sum++;
                debit_amount+=Integer.parseInt(list.get(i).getDTLAMT());
            }else if(type.equals(MessageFilter.CREDIT_TYPE)){
                crebit_sum++;
                crebit_amount+=Integer.parseInt(list.get(i).getDTLAMT());
            }
        }
        transctionHeader.append(String.format("%010d",debit_sum)).append(",");
        transctionHeader.append(String.format("%010d",debit_amount)).append(",");
        transctionHeader.append(String.format("%010d",crebit_sum)).append(",");
        transctionHeader.append(String.format("%010d",crebit_amount)).append(",");
        transctionHeader.append("            ");
        transctionHeader.append("\r\n");

        //交易明细
        StringBuffer fileRecord = new StringBuffer();
        for (int i=0;i<list.size();i++){
            fileRecord.append(PosUtil.formatStringReZoro(16,list.get(i).getDTLCARDNO())).append(",").append("000000").append(",").
                       append(PosUtil.formatStringReZoro(6,list.get(i).getDTLPRCODE())).append(",").append(PosUtil.formatStringReZoro(4,list.get(i).getDTLTRANSTYPE())).append(",").
                       append(list.get(i).getDTLPOSID()).append(",").append("                ").append(",").
                       append(PosUtil.formatStringReZoro(6,list.get(i).getDTLPOSSEQ())).append(",").append(list.get(i).getDTLBATCHNO()).append(",").
                       append("            ").append(",").append("0000000000").append(",").
                       append(list.get(i).getDTLDATE()).append(",").append(list.get(i).getDTLTIME()).append(",").
                       append(list.get(i).getDTLSETTDATE()).append(",").append(PosUtil.formatStringReZoro(12,list.get(i).getDTLCENSEQ())).append(",").
                       append("000000000").append(",").append("000000000").append(",").
                       append("000000000").append(",").append(PosUtil.formatStringReZoro(9,list.get(i).getDTLAMT())).append(",").
                       append(list.get(i).getDTLUNITID()).append(",").append("            ").append(",").
                       append("        ");
            fileRecord.append("\r\n");
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
        PosLog.e("xx","build file ok " + file.getAbsolutePath()+" file size = " + file.length());
        return file;

    }

    private String getMaxDate(List<TranslationRecord> list)  {
        String maxDate = "";
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        try {

            if (list != null && list.size() > 0) {
                maxDate = list.get(0).getDTLSETTDATE();
                for (int i = 1; i < list.size(); i++) {
                    Date max = df.parse(maxDate);
                    Date temp = df.parse(list.get(i).getDTLSETTDATE());
                    if (temp.getTime() > max.getTime()) {
                        maxDate = list.get(i).getDTLSETTDATE();
                    }
                }
            }

        } catch (ParseException e) {

        }
        return maxDate;
    }

    private String getMinDate(List<TranslationRecord> list)  {
        String minDate = "";
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        try {

            if (list != null && list.size() > 0) {
                minDate = list.get(0).getDTLSETTDATE();
                for (int i = 1; i < list.size(); i++) {
                    Date max = df.parse(minDate);
                    Date temp = df.parse(list.get(i).getDTLSETTDATE());
                    if (temp.getTime() < max.getTime()) {
                        minDate = list.get(i).getDTLSETTDATE();
                    }
                }
            }

        } catch (ParseException e) {

        }
        return minDate;
    }

    private void ftpConnectAndUpload(File uploadFile,FtpDataTranterCallBack callBack){
        try {
            PosLog.e("xx","ftpConnectAndUpload start");
            mCropID = mContext.getCropNum();
            PosLog.e("xx","ftpConnectAndUpload start222222");
            userAndpasswd=REPART+mCropID;

            PosLog.e("xx","userAndpasswd ==" + userAndpasswd);

            mFtp.connect(r.getString(R.string.ftp_addresss), r.getInteger(R.integer.ftp_port));
            mFtp.login(userAndpasswd, userAndpasswd);
            mFtp.changeDirectory(ABS_PATH);
            PosLog.e("xx", "changeDirectory" );
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
