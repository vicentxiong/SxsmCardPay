package com.mwdev.sxsmcardpay;

import android.os.Handler;
import android.widget.Toast;

import com.mwdev.sxsmcardpay.controler.FtpDataTranterCallBack;
import com.mwdev.sxsmcardpay.controler.MessageCallBack;
import com.mwdev.sxsmcardpay.database.Flushes;
import com.mwdev.sxsmcardpay.database.PosDataBaseFactory;
import com.mwdev.sxsmcardpay.iso8583.Iso8583Mgr;
import com.mwdev.sxsmcardpay.network.SocketClient;
import com.mwdev.sxsmcardpay.network.SxFtpClient;
import com.mwdev.sxsmcardpay.util.PosLog;
import com.ta.TAApplication;
import com.ta.util.netstate.TANetChangeObserver;
import com.ta.util.netstate.TANetWorkUtil;
import com.ta.util.netstate.TANetworkStateReceiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by xiongxin on 16-8-13.
 */
public class PosApplication extends TAApplication{
    private static final String TAG = PosApplication.class.getName();
    private SocketClient mSocketClient;
    private SxFtpClient mFtpClient;
    private WeakReference<PosNetworkObserver> mNetObserver = new WeakReference<PosNetworkObserver>(new PosNetworkObserver());
    private ThreadPoolExecutor mExecutor;
    private Iso8583Mgr mIso8583Mgr;
    private PosNetworkStatusListener l;

    public interface PosNetworkStatusListener{
        public void onNetworkStatus(boolean connect);
    }

    public void addNetworkStatusListener(PosNetworkStatusListener listener){
        l = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //log输出本地
        PosLog.startFileLoger();
        //初始化数据库工厂
        PosDataBaseFactory.getIntance().initiedFactory(this);
        //获取线程池
        mExecutor  = (ThreadPoolExecutor)
                Executors.newFixedThreadPool(getResources().getInteger(R.integer.threadpool_count));
        mSocketClient = new SocketClient(this);
        mFtpClient = new SxFtpClient(this);
        //注册pos设备的网络状态监听器
        TANetworkStateReceiver.registerObserver(mNetObserver.get());
        //初始化8583管理器
        mIso8583Mgr = new Iso8583Mgr(this);
        //加载psamid
        //getmIso8583Mgr().readPsamId();

    }

    public void exitApplication(){
        PosLog.stopFileLoger();
        mExecutor.shutdownNow();
        TANetworkStateReceiver.removeRegisterObserver(mNetObserver.get());
    }

    public Iso8583Mgr getmIso8583Mgr(){
        if(mIso8583Mgr==null)
            mIso8583Mgr = new Iso8583Mgr(this);
        return mIso8583Mgr;
    }

    public String getPsamID(){
        //return getmIso8583Mgr().getPsamId();
        return "312105012177"; //330600000000001
    }

    public ThreadPoolExecutor getThreadPoolExecutor(){
        return mExecutor;
    }

    public void startUpConnectAndSend(byte[] message,String msgid,String tradeDealcode){
        PosLog.d("xx","startUpConnectAndSend");
        if(mSocketClient!=null)
            mSocketClient.startConnectAndSend(message,msgid,tradeDealcode);
    }

    public void startFtpConnectAndUpload(File file,FtpDataTranterCallBack callBack){
        if(mFtpClient!=null)
            mFtpClient.startFtpTask(file, callBack);
    }

    public File restoreFtpFile(){
        return mFtpClient.restoreRecord();
    }

    public void resigterMessageCallBack(MessageCallBack callBack){
        if(mSocketClient != null)
            mSocketClient.addMessageCallBack(callBack);
    }

    public void addFlushes(Flushes f){
        mSocketClient.getFilter().addFlushesForTemp(f);
    }

    /**
     * 监听pos设备的网络状态
     */
    private class PosNetworkObserver extends TANetChangeObserver{
        @Override
        public void onConnect(TANetWorkUtil.netType type) {
            super.onConnect(type);
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.network_connected), Toast.LENGTH_LONG).show();
            mSocketClient.getFilter().postDBFlushesTask();
            if(l!=null)
                l.onNetworkStatus(true);
        }

        @Override
        public void onDisConnect() {
            super.onDisConnect();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.network_disconnected),Toast.LENGTH_LONG).show();
            mSocketClient.clearWhenMessage();
            mSocketClient.getFilter().clearFlushesMap();
            if(l!=null)
                l.onNetworkStatus(false);
        }
    }

    public String createTradeSerialNumber(){
        File file = new File(getFilesDir(),"sn");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long fileSize = file.length();
        if(fileSize>999999){
            file.delete();
            return String.format("%06d",0);
        }

        try {
            FileOutputStream fous = new FileOutputStream(file);
            fous.write(new byte[]{0x00});
            fous.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.format("%06d",fileSize);
    }

    /**
     * 获取商户号
     * @return
     */
    public String getCropNum(){
        return getConfig(PosApplication.PREFERENCECONFIG).getString("cropNameKey", "");
    }

    public int getStringIdByCode(int r){
        int res = 0;
        switch (r){
            case 0x01:res=R.string.code01;break;
            case 0x02:res=R.string.code02;break;
            case 0x03:res=R.string.code03;break;
            case 0x04:res=R.string.code04;break;
            case 0x05:res=R.string.code05;break;
            case 0x06:res=R.string.code06;break;
            case 0x07:res=R.string.code07;break;
            case 0x09:res=R.string.code09;break;
            case 0x12:res=R.string.code12;break;
            case 0x13:res=R.string.code13;break;

            case 0x14:res=R.string.code14;break;
            case 0x15:res=R.string.code15;break;
            case 0x19:res=R.string.code19;break;
            case 0x20:res=R.string.code20;break;
            case 0x21:res=R.string.code21;break;
            case 0x22:res=R.string.code22;break;
            case 0x23:res=R.string.code23;break;
            case 0x25:res=R.string.code25;break;
            case 0x30:res=R.string.code30;break;
            case 0x33:res=R.string.code33;break;

            case 0x34:res=R.string.code34;break;
            case 0x35:res=R.string.code35;break;
            case 0x36:res=R.string.code36;break;
            case 0x37:res=R.string.code37;break;
            case 0x38:res=R.string.code38;break;
            case 0x40:res=R.string.code40;break;
            case 0x41:res=R.string.code41;break;
            case 0x42:res=R.string.code42;break;
            case 0x43:res=R.string.code43;break;
            case 0x51:res=R.string.code51;break;

            case 0x52:res=R.string.code52;break;
            case 0x53:res=R.string.code53;break;
            case 0x54:res=R.string.code54;break;
            case 0x55:res=R.string.code55;break;
            case 0x56:res=R.string.code56;break;
            case 0x57:res=R.string.code57;break;
            case 0x58:res=R.string.code58;break;
            case 0x59:res=R.string.code59;break;
            case 0x60:res=R.string.code60;break;
            case 0x61:res=R.string.code61;break;

            case 0x62:res=R.string.code62;break;
            case 0x63:res=R.string.code63;break;
            case 0x64:res=R.string.code64;break;
            case 0x65:res=R.string.code65;break;
            case 0x66:res=R.string.code66;break;
            case 0x67:res=R.string.code67;break;
            case 0x68:res=R.string.code68;break;
            case 0x75:res=R.string.code75;break;
            case 0x77:res=R.string.code77;break;
            case 0x79:res=R.string.code79;break;

            case 0x81:res=R.string.code81;break;
            case 0x82:res=R.string.code82;break;
            case 0x83:res=R.string.code83;break;
            case 0x84:res=R.string.code84;break;
            case 0x85:res=R.string.code85;break;
            case 0x86:res=R.string.code86;break;
            case 0x87:res=R.string.code87;break;
            case 0x88:res=R.string.code88;break;

            case 0x90:res=R.string.code90;break;
            case 0x91:res=R.string.code91;break;
            case 0x92:res=R.string.code92;break;
            case 0x93:res=R.string.code93;break;
            case 0x94:res=R.string.code94;break;
            case 0x95:res=R.string.code95;break;
            case 0x96:res=R.string.code96;break;
            case 0x97:res=R.string.code97;break;
            case 0x98:res=R.string.code98;break;
            case 0x99:res=R.string.code99;break;
            case 0xA0:res=R.string.codeA0;break;
        }
        return res;
    }

}
