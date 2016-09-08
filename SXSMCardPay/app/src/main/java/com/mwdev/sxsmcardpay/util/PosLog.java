package com.mwdev.sxsmcardpay.util;

import android.util.Log;

import com.ta.util.TALogger;
import com.ta.util.log.TAPrintToFileLogger;

/**
 * Created by xiongxin on 16-8-16.
 */
public class PosLog {
    //输出到sd卡中的打印器
    private static TAPrintToFileLogger fileLogger = new TAPrintToFileLogger();

    /*
     *输出开始 - 与 topFileLoger()配合使用
     * 有开始必须要停止
     *
     */
    public static void startFileLoger(){
        TALogger.addLogger(fileLogger);
    }

    /**
     *停止输出
     *
     */
    public static void stopFileLoger(){
        TALogger.removeLogger(fileLogger);
    }

    public static void v(String tag,String message){
        TALogger.v(tag, message);
    }

    public static void d(String tag,String message){
        TALogger.d(tag, message);
    }

    public static void i(String tag,String message){
        TALogger.i(tag, message);
    }

    public static void w(String tag,String message){
        TALogger.w(tag, message);
    }

    public static void e(String tag,String message){
        TALogger.e(tag,message);
    }
}
