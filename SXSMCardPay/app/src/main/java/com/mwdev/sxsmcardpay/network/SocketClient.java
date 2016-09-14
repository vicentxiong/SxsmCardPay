package com.mwdev.sxsmcardpay.network;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;

import com.basewin.define.Msg;
import com.mwdev.sxsmcardpay.controler.MessageCallBack;
import com.mwdev.sxsmcardpay.PosApplication;
import com.mwdev.sxsmcardpay.R;
import com.mwdev.sxsmcardpay.controler.MessageFilter;
import com.mwdev.sxsmcardpay.util.PosLog;
import com.mwdev.sxsmcardpay.util.PosUtil;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;


/**
 * Created by xiongxin on 16-8-13.
 */
public class SocketClient{
    private static final String TAG = SocketClient.class.getName();
    private IoConnector mConnector;
    private ConnectFuture mConnFeture;
    private IoSession mIoSession;
    private MessageFilter mFilter;
    private MessageFilter.MessageType msgType;
    private PosApplication mContext;
    private Resources r;
    private int retyrCount ;
    private Object whenMessage;
    private RetryTask mRetryTask;


    private MessageCallBack messageCallback ;
    public SocketClient(Context cx){
        mContext = (PosApplication) cx;
        mFilter = new MessageFilter((PosApplication) mContext);
        init();
    }

    private void init(){
        r = mContext.getResources();
        retyrCount = r.getInteger(R.integer.request_retry);
        //创建一个非阻塞的客户端程序
        mConnector = new NioSocketConnector();
        //设置链接超时时间
        mConnector.setConnectTimeoutMillis(r.getInteger(R.integer.socket_timeout));
        //添加过滤器
        mConnector.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new ByteCodecFactory()));
        //添加业务逻辑处理器类
        mConnector.setHandler(new ClentHandler());
    }

    /**
     *异步连接远程服务器
     *
     */
    public void startConnectAndSend(byte[] message,String msgid,String tradedealCode){
        whenMessage = message;
        msgType = new MessageFilter.MessageType(msgid,tradedealCode);
        PosApplication posApp = (PosApplication) mContext;
        posApp.getThreadPoolExecutor().execute(new Request(whenMessage));


    }

    /**
     * 响应超时判定线程 ，并根据设定重发次数进行重复请求
     */
    class RetryTask implements Runnable{
        private int count = 0;
        private long mTimeOut;
        private boolean mRequest = true;

        public void setTimeout(long time){
            this.mTimeOut = time;
        }

        public void setmRequest(boolean request){
            this.mRequest = request;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(mTimeOut);
                mIoSession.closeNow();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while(mRequest && count < retyrCount){
                if(whenMessage==null){
                    break;
                }
                connectAndSend((byte[]) whenMessage);
                count++;
                try {
                    Thread.sleep(mTimeOut);
                    mIoSession.closeNow();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(count == retyrCount){
                if(msgType.equals(mFilter.TRADE_IMPACT_REQUEST_TYPE) || msgType.equals(mFilter.TRADE_CANCEL_IMPACT_REQUEST_TYPE)){
                    messageCallback.onResponeTimeout(msgType);
                    mFilter.handlerByActivity(R.string.flushes_respone_timeout);
                }else if(msgType.equals(mFilter.TRADE_REQUEST_TYPE) || msgType.equals(mFilter.TRADE_CANCEL_REQUEST_TYPE)){
                    mFilter.postFlushesTask("98");
                    messageCallback.onResponeTimeout(msgType);
                }else{
                    messageCallback.onResponeTimeout(msgType);
                }
            }

        }
    }

    public MessageFilter getFilter(){
        return mFilter;
    }

    public void clearWhenMessage(){
        this.whenMessage = null;

    }


    public void addMessageCallBack(MessageCallBack cb){
        messageCallback = cb;
    }

    /**
     *向服务器发送报文
     *
     */
    private void connectAndSend(byte[] message){
        synchronized (mFilter){
            try{
                PosLog.d(TAG,"connectAndSend");
                //创建连接
                mConnFeture = mConnector.connect(new InetSocketAddress(r.getString(R.string.socket_address),
                        r.getInteger(R.integer.socket_port)));
                //等待连接创建完成
                mConnFeture.awaitUninterruptibly();
                //得到iosession 对象
                mIoSession = mConnFeture.getSession();

                if(mIoSession!=null){
                    mIoSession.write(IoBuffer.wrap(message));
                }
                //等待断开
                //mIoSession.getCloseFuture().awaitUninterruptibly();
                //mConnector.dispose();
            }catch (Exception ex){
                PosLog.w(TAG, "socket exception : " + ex.getMessage());
                if(messageCallback!=null)
                    messageCallback.onConnectFail();
                //mConnector.dispose();
            }
        }

    }
    /**
     *异步请求任务
     *
     */
    class Request implements Runnable{
        private Object message;

        public Request(Object msg){
            this.message = msg;
        }
        @Override
        public void run() {
            if(message!=null)
                connectAndSend((byte[]) message);
        }
    }

    /**
     *业务处理类
     *
     */
    class ClentHandler implements IoHandler {

        /**
         * Invoked from an I/O processor thread when a new connection has been created.
         * Because this method is supposed to be called from the same thread that
         * handles I/O of multiple sessions, please implement this method to perform
         * tasks that consumes minimal amount of time such as socket parameter
         * and user-defined session attribute initialization.
         *
         * @param session The session being created
         * @throws Exception If we get an exception while processing the create event
         */
        @Override
        public void sessionCreated(IoSession session) throws Exception {
            PosLog.d("xx","sessionCreated");
            if(messageCallback != null)
                messageCallback.onConnectCreated();
        }

        /**
         * Invoked when a connection has been opened.  This method is invoked after
         * {@link #sessionCreated(IoSession)}.  The biggest difference from
         * {@link #sessionCreated(IoSession)} is that it's invoked from other thread
         * than an I/O processor thread once thread model is configured properly.
         *
         * @param session The session being opened
         * @throws Exception If we get an exception while processing the open event
         */
        @Override
        public void sessionOpened(IoSession session) throws Exception {
            PosLog.d("xx","sessionOpened");
            if(messageCallback!=null)
                messageCallback.onConnected();
        }

        /**
         * Invoked when a connection is closed.
         *
         * @param session The session being closed
         * @throws Exception If we get an exception while processing the close event
         */
        @Override
        public void sessionClosed(IoSession session) throws Exception {
            PosLog.d("xx","sessionClosed");
            if(messageCallback != null)
                messageCallback.onDisconnected();

        }

        /**
         * Invoked with the related {@link IdleStatus} when a connection becomes idle.
         * This method is not invoked if the transport type is UDP; it's a known bug,
         * and will be fixed in 2.0.
         *
         * @param session The idling session
         * @param status  The session's status
         * @throws Exception If we get an exception while processing the idle event
         */
        @Override
        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
            PosLog.d("xx","sessionIdle");
            if(messageCallback != null)
                messageCallback.onCommunictionIdle(status.toString());
        }

        /**
         * Invoked when any exception is thrown by user {@link IoHandler}
         * implementation or by MINA.  If <code>cause</code> is an instance of
         *
         * @param session The session for which we have got an exception
         * @param cause   The exception that has been caught
         * @throws Exception If we get an exception while processing the caught exception
         */
        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            PosLog.d("xx","exceptionCaught  " + cause.getMessage());
            if(messageCallback != null)
                messageCallback.onExceptionCaught(cause);
        }

        /**
         * Invoked when a message is received.
         *
         * @param session The session that is receiving a message
         * @param message The received message
         * @throws Exception If we get an exception while processing the received message
         */
        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            PosLog.d("xx","messageReceived");
            if(mRetryTask!=null)
                mRetryTask.setmRequest(false);
            int result = 0;
            byte[] bytes = PosUtil.HexStringToByteArray((String) message);
            //单次请求响应后关闭连接
            mIoSession.closeNow();
            if(messageCallback != null){
                if((result=mFilter.onReceiveredFilter(bytes))==MessageFilter.SUCCESSED_FILTER){
                    messageCallback.onMessageReceivered(bytes);
                }else if(result==MessageFilter.INTERCEPT_FILTER){
                    PosLog.d("xx", "message receivered intercept");
                    startConnectAndSend(mContext.getmIso8583Mgr().checkOut(mContext.getPsamID(),
                                                            mContext.getCropNum()), "0820", "000000");
                    return;
                }else{
                    messageCallback.onMessageFilterResult(result);
                }
            }



        }

        /**
         * Invoked when a message written by {@link IoSession#write(Object)} is
         * sent out.
         *
         * @param session The session that has sent a full message
         * @param message The sent message
         * @throws Exception If we get an exception while processing the sent message
         */
        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            PosLog.d("xx","messageSent");
            mRetryTask = new RetryTask();
            mRetryTask.setTimeout(r.getInteger(R.integer.respone_timeout));
            PosApplication posApp = (PosApplication) mContext;
            posApp.getThreadPoolExecutor().execute(mRetryTask);

            if(messageCallback != null)
                messageCallback.onMessageSent();
        }

        /**
         * Handle the closure of an half-duplex TCP channel
         *
         * @param session The session which input is being closed
         * @throws Exception If we get an exception while closing the input
         */
        @Override
        public void inputClosed(IoSession session) throws Exception {
            PosLog.d("xx","inputClosed");

        }
    }
}
