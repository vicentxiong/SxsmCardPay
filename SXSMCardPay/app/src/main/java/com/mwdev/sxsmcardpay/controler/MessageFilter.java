package com.mwdev.sxsmcardpay.controler;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.basewin.services.PinpadBinder;
import com.basewin.services.ServiceManager;
import com.mwdev.sxsmcardpay.PosApplication;
import com.mwdev.sxsmcardpay.R;
import com.mwdev.sxsmcardpay.SxBaseActivity;
import com.mwdev.sxsmcardpay.database.Flushes;
import com.mwdev.sxsmcardpay.database.PosDataBaseFactory;
import com.mwdev.sxsmcardpay.database.TranslationRecord;
import com.mwdev.sxsmcardpay.iso8583.Iso8583Mgr;
import com.mwdev.sxsmcardpay.iso8583.util;
import com.mwdev.sxsmcardpay.util.PosLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xiongxin on 16-8-16.
 */
public class MessageFilter {
    public static final int IMPACT_FILTER = -3;
    public static final int POS_MAC_ERROR_FILTER = -2;     //pos对pos中心的报文进行mac值验证返回的错误值
    public static final int INTERCEPT_FILTER = -1;  //消息监听器的拦截值
    public static final int SUCCESSED_FILTER = 0;
    public final MessageType CHEKCIN_REQUEST_TYPE = new MessageType("0800","000000");
    public final MessageType CHEKCIN_RESPONES_TYPE = new MessageType("0810","000000");
    public final MessageType CHEKCOUT_REQUEST_TYPE = new MessageType("0820","000000");
    public final MessageType CHEKCOUT_RESPONES_TYPE = new MessageType("0830","000000");
    public final MessageType BALANCE_REQUEST_TYPE = new MessageType("0200","311000");
    public final MessageType BALANCE_RESPONES_TYPE = new MessageType("0210","311000");
    public final MessageType TRADE_REQUEST_TYPE = new MessageType("0200","001000");
    public final MessageType TRADE_RESPONES_TYPE = new MessageType("0210","001000");
    public final MessageType TRADE_IMPACT_REQUEST_TYPE = new MessageType("0400","001000");
    public final MessageType TRADE_IMPACT_RESPONES_TYPE = new MessageType("0410","001000");
    public final MessageType TRADE_CANCEL_REQUEST_TYPE = new MessageType("0200","201000");
    public final MessageType TRADE_CANCEL_RESPONES_TYPE = new MessageType("0210","201000");
    public final MessageType TRADE_CANCEL_IMPACT_REQUEST_TYPE = new MessageType("0400","201000");
    public final MessageType TRADE_CANCEL_IMPACT_RESPONES_TYPE = new MessageType("0410","201000");
    public final MessageType RETURN_GOODS_REQUEST_TYPE = new MessageType("0220","401000");
    public final MessageType RETURN_GOODS_RESPONES_TYPE = new MessageType("0230","401000");
    public final MessageType BATCH_SETTLEMENT_REQUEST_TYPE = new MessageType("0500","000000");
    public final MessageType BATCH_SETTLEMENT_RESPONES_TYPE = new MessageType("0510","000000");

    public static final String DEBIT_TYPE = "1";
    public static final String CREDIT_TYPE = "2";

    private HashMap<String,Flushes> allFlushes = new HashMap<String,Flushes>();
    private ArrayList<Flushes> tempTrade = new ArrayList<Flushes>();
    private PosApplication mPosApp;
    private SxBaseActivity mActivity;

    public MessageFilter(PosApplication application){
        mPosApp = application;
    }

    /**
     * 过滤接收到的报文消息
     * @param message
     * @return
     */
    public int onReceiveredFilter(byte[] message){
        int resultCode=-1;

        mPosApp.getmIso8583Mgr().unpackData(message);
        PosLog.d("xx", "start onReceiveredFilter");
        String _msgid = util.Delete0(mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("msgid"));
        String _area3 = util.Delete0(mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("3"));
        String _area39 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("39");
        MessageType target = new MessageType(_msgid,_area3);
        PosLog.d("xx", "result code : " + _area39);
        do {
            if(target.equals(CHEKCIN_RESPONES_TYPE)){
                PosLog.d("xx","CHEKCIN_RESPONES_TYPE");
                resultCode = onHandleCheckInMessage(_area39);
                break;
            }
            if(target.equals(CHEKCOUT_RESPONES_TYPE)){
                PosLog.d("xx","CHEKCOUT_RESPONES_TYPE");
                resultCode = onHandleCheckOutMessage(_area39);
                break;
            }
            if(target.equals(BALANCE_RESPONES_TYPE)){
                PosLog.d("xx","BALANCE_RESPONES_TYPE");
                resultCode = onHandleBalanceQueryMessage(message,_area39);
                break;
            }
            if(target.equals(TRADE_RESPONES_TYPE)){
                PosLog.d("xx","TRADE_RESPONES_TYPE");
                resultCode = onHandlerTradeMessage(message,_area39);
                break;
            }
            if(target.equals(TRADE_IMPACT_RESPONES_TYPE)){
                break;
            }
            if(target.equals(TRADE_CANCEL_RESPONES_TYPE)){
                PosLog.d("xx","TRADE_CANCEL_RESPONES_TYPE");
                resultCode = onHandlerTradeCalcelMessage(message,_area39);
                break;
            }
            if(target.equals(TRADE_CANCEL_IMPACT_RESPONES_TYPE)){
                resultCode = onHandlerTradeCancelImpactMessage(message,_area39);
                break;
            }
            if(target.equals(RETURN_GOODS_RESPONES_TYPE)){
                PosLog.d("xx","RETURN_GOODS_RESPONES_TYPE");
                resultCode = onHandlerReturnGoodsMessage(message,_area39);
                break;
            }
            if(target.equals(BATCH_SETTLEMENT_RESPONES_TYPE)){
                PosLog.d("xx","BATCH_SETTLEMENT_RESPONES_TYPE");
                resultCode = onHandlerBatchSettlementMessage(message,_area39);
                break;
            }

        }while(false);

        return resultCode;
    }

    /**
     * 处理签到响应报文
     * @param responeCode
     * @return
     */
    public int onHandleCheckInMessage(String responeCode){
        if("00".equals(responeCode)){
            String _area62 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("62");
            String pik = _area62.substring(0, 32);
            String mak = _area62.substring(40,72);
            String _area60 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("60");
            String battch = util.Delete0(_area60.substring(4,16)).trim();

            try {
                PinpadBinder pinpad = ServiceManager.getInstence().getPinpad();
                pinpad.loadPinKey(pik,null);
                pinpad.loadMacKey(mak, null);
                mPosApp.setBattchNum(battch);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return SUCCESSED_FILTER;
        }else{
            return Integer.parseInt(responeCode,16);
        }
    }

    /**
     * 处理签退响应报文
     * @param responeCode
     * @return
     */
    public int onHandleCheckOutMessage(String responeCode){
        if("00".equals(responeCode)){
            return SUCCESSED_FILTER;
        }else{
            return Integer.parseInt(responeCode,16);
        }
    }

    /**
     * 处理余额查询响应报文
     * @param message
     * @param responeCode
     * @return
     */
    public int onHandleBalanceQueryMessage(byte[] message,String responeCode){
        if("00".equals(responeCode)){
            /*
            if(mPosApp.getmIso8583Mgr().makeMac(message)== Iso8583Mgr.MAC_ERROR){
                return POS_MAC_ERROR_FILTER;
            }
            */
            return SUCCESSED_FILTER;
        }else{
            return Integer.parseInt(responeCode,16);
        }
    }

    /**
     * 处理消费响应报文
     * @param message
     * @param responeCode
     * @return
     */
    public int onHandlerTradeMessage(byte[] message,String responeCode){
        if("00".equals(responeCode)){
            /*
            if(mPosApp.getmIso8583Mgr().makeMac(message)== Iso8583Mgr.MAC_ERROR){
                //MAC校验错误 发送冲正
                postFlushesTask("A0");
                return POS_MAC_ERROR_FILTER;
            }*/
            persistToDatabase(DEBIT_TYPE);
            return SUCCESSED_FILTER;
        }else{
            return  Integer.parseInt(responeCode,16);
        }
    }

    /**
     * 处理消费冲正响应报文
     * @param message
     * @param responeCode
     * @return
     */
    public int onHandlerTradeImpactMessage(byte[] message,String responeCode){
        if("00".equals(responeCode)){
            mPosApp.getThreadPoolExecutor().execute(
                    new FlushesResponeTsk(util.Delete0(mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("11")))
            );
            handlerByActivity(R.string.impact_success);
            return IMPACT_FILTER;
        }else{
            /**
             * TODO
             */
            int r = Integer.parseInt(responeCode,16);
            handlerByActivity(mPosApp.getStringIdByCode(r));
            return r;
        }
    }

    /**
     * 处理消费撤销响应报文
     * @param message
     * @param responeCode
     * @return
     */
    public int onHandlerTradeCalcelMessage(byte[] message,String responeCode){
        if("00".equals(responeCode)){
            if(mPosApp.getmIso8583Mgr().makeMac(message)== Iso8583Mgr.MAC_ERROR){
                //MAC校验错误 发送冲正
                postFlushesTask("A0");
                return POS_MAC_ERROR_FILTER;
            }
            persistToDatabase(CREDIT_TYPE);
            return SUCCESSED_FILTER;
        }else{
            return  Integer.parseInt(responeCode,16);
        }
    }

    /**
     * 处理消费撤销冲正响应报文
     * @param message
     * @param responeCode
     * @return
     */
    public int onHandlerTradeCancelImpactMessage(byte[] message,String responeCode){
        if("00".equals(responeCode)){
            mPosApp.getThreadPoolExecutor().execute(
                    new FlushesResponeTsk(util.Delete0(mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("11")))
            );
            handlerByActivity(R.string.impact_success);
            return IMPACT_FILTER;
        }else{
            /**
             * TODO
             */
            int r = Integer.parseInt(responeCode,16);
            handlerByActivity(mPosApp.getStringIdByCode(r));
            return r;
        }
    }

    /**
     * 处理退货响应报文
     * @param message
     * @param responeCode
     * @return
     */
    public int onHandlerReturnGoodsMessage(byte[] message,String responeCode){
        if("00".equals(responeCode)){
            /*
            if(mPosApp.getmIso8583Mgr().makeMac(message)== Iso8583Mgr.MAC_ERROR){
                return POS_MAC_ERROR_FILTER;
            }
            */
            persistToDatabase(CREDIT_TYPE);
            return SUCCESSED_FILTER;
        }else{
            return  Integer.parseInt(responeCode,16);
        }
    }

    /**
     * 处理批结算响应报文
     * @param message
     * @param responeCode
     * @return
     */
    public int onHandlerBatchSettlementMessage(byte[] message,String responeCode){
        int res = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("48").charAt(30);
        PosLog.e("xin", "res ==> " + res + "  responeCode ==>" + responeCode);
        if("00".equals(responeCode)&&res=='1'){  //返回 1 对账平
            /*
            if(mPosApp.getmIso8583Mgr().makeMac(message)== Iso8583Mgr.MAC_ERROR){
                return POS_MAC_ERROR_FILTER;
            }
            */
            clearTransctionRecords();

            return INTERCEPT_FILTER;
        }else if("00".equals(responeCode)&&res=='2'){ //返回 2 对账不平
            return 0xF0;
        }else{
            return Integer.parseInt(responeCode,16);
        }

    }

    /**
     *
     * 冲正响应结果输出到终端
     * @param resource
     */
    public void handlerByActivity(int resource){
        if(mActivity!=null){
            mActivity.DBFlushesStop(resource);
            mActivity = null;
        }
    }

    /**
     * 响应超时，获取相应的冲正信息
     * @param f
     */
    public void addFlushesForTemp(Flushes f){
        tempTrade.add(0,f);
    }


    /**
     * 消费 退货的成功交易保存到数据库
     *
     */
    private void persistToDatabase(String type){
        String _area5515 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("55").substring(64,80);
        String _area3 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("3");
        String _msgid = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("msgid");
        String _area41 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("41");
        String _area11 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("11");
        String _area602 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("60").substring(4,16);
        String _area13 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("13");
        String _area12 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("12");
        String _area15 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("15");
        String _area37 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("37");
        String _area4 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("4");
        String _area42 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("42");

        TranslationRecord record = new TranslationRecord();
        record.setDTLCARDNO(_area5515);
        record.setDTLCDCNT("000000");
        record.setDTLPRCODE(util.Delete0(_area3));
        record.setDTLTRANSTYPE(util.Delete0(_msgid));
        record.setDTLPOSID(_area41);
        record.setDTLSAMID("                ");
        record.setDTLPOSSEQ(util.Delete0(_area11));
        record.setDTLBATCHNO(util.Delete0(_area602));
        record.setDTLTERMID("            ");
        record.setDTLTERMSEQ("0000000000");
        record.setDTLDATE(util.Delete0(_area13));
        record.setDTLTIME(util.Delete0(_area12));
        record.setDTLSETTDATE(util.Delete0(_area15));
        record.setDTLCENSEQ(_area37);
        record.setDTLSLAMT("000000000");
        record.setDTLBEFBAL("000000000");
        record.setDTLAFTBAL("000000000");
        record.setDTLAMT(util.Delete0(_area4));
        record.setDTLUNITID(_area42);
        record.setDTLNETID("            ");
        record.setDTLTAC("        ");
        record.setDTLTYPE(type);


        PosDataBaseFactory.getIntance().openPosDatabase();
        PosDataBaseFactory.getIntance().insert(record);
        PosDataBaseFactory.getIntance().closePosDatabase();
    }

    public void postFlushesMessage(byte[] message){}

    /**
     * mac校验失败 响应超时 产生冲正 并保存数据库
     * @param impactReason
     */
    public void createFlushedMessage(String impactReason){
        Flushes flushes = null;
        if("A0".equals(impactReason)){
            String _area2 = mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("2");
            String _area4 = util.Delete0(mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("3"));
            String _area3 = util.Delete0(mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("4"));
            String _area11 = util.Delete0(mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("11"));
            String _area61 = util.Delete0(mPosApp.getmIso8583Mgr().getManager_unpackData().getBit("61"));
            String _area611 = _area61.substring(0, 6);
            String _area612 = _area61.substring(6,12);
            flushes = new Flushes();
            flushes.setMsgID("0400");
            flushes.setTradeDealCode(_area3);
            flushes.setCardNum(_area2);
            flushes.setAmount(_area4);
            flushes.setTradeNUm(_area11);
            flushes.setOriginal_batchNum(_area611);
            flushes.setOriginal_tradeNum(_area612);
            flushes.setImpact_reason(impactReason);
        }else if("98".equals(impactReason)){
            PosLog.d("xx","98  flushes");
            flushes = tempTrade.get(0);
            tempTrade.remove(flushes);
        }


        allFlushes.put(flushes.getTradeNUm(),flushes);
        PosDataBaseFactory.getIntance().openPosDatabase();
        PosDataBaseFactory.getIntance().insert(flushes);
        PosDataBaseFactory.getIntance().closePosDatabase();
        PosLog.d("xx", "create flushes finsh ");
    }

    /**
     * 发送已保存的冲正
     */
    public void sendFlushesRequestByArray(){
        byte[] message = null;
        Flushes f = null;
        Iterator it = allFlushes.values().iterator();
        if(it.hasNext()){
            f = (Flushes) it.next();
            MessageType type = new MessageType(f.getMsgID(),f.getTradeDealCode());
            if(type.equals(TRADE_IMPACT_REQUEST_TYPE)){
                message = mPosApp.getmIso8583Mgr().trade_impact
                        (f.getImpact_reason(),
                                mPosApp.getPsamID(),
                                mPosApp.getCropNum(),
                                f.getCardNum(),
                                null,
                                f.getTradeNUm(),
                                f.getAmount(),
                                f.getOriginal_batchNum(),
                                f.getOriginal_batchNum());

            }else if(type.equals(TRADE_CANCEL_IMPACT_REQUEST_TYPE)){
                message = mPosApp.getmIso8583Mgr().trade_cancel__impact
                        (mPosApp.getPsamID(),
                                mPosApp.getCropNum(),
                                f.getCardNum(),
                                null,
                                f.getTradeNUm(),
                                f.getAmount(),
                                null,
                                f.getOriginal_tradeNum(),
                                f.getOriginal_batchNum(),
                                f.getImpact_reason());
            }

            mPosApp.startUpConnectAndSend(message,f.getMsgID(),f.getTradeDealCode());
        }
    }



    public void postFlushesTask(String reason){
        mPosApp.getThreadPoolExecutor().execute(new FlushesRequestTask(reason));
    }

    public void postDBFlushesTask(SxBaseActivity at){
        mActivity = at;
        mPosApp.getThreadPoolExecutor().execute(new DBFlushesRequestTask());
    }

    public void clearFlushesMap(){
        allFlushes.clear();
    }

    /**
     * 冲正线程
     */
    class FlushesRequestTask implements Runnable{
        private String mImpactReason;

        public FlushesRequestTask(String reason) {
            this.mImpactReason = reason;
        }

        @Override
        public void run() {
            createFlushedMessage(mImpactReason);
            sendFlushesRequestByArray();
        }
    }

    /**
     * 加载数据库冲正线程
     */
    class DBFlushesRequestTask implements Runnable{

        /**
         * Starts executing the active part of the class' code. This method is
         * called when a thread is started that has been created with a class which
         * implements {@code Runnable}.
         */
        @Override
        public void run() {
            loadDbFlushesMessage();
            sendFlushesRequestByArray();
        }
    }

    /**
     * 冲正响应处理线程
     */
    class FlushesResponeTsk implements Runnable{
        private String tradeNum;

        public FlushesResponeTsk(String tradeNum) {
            this.tradeNum = tradeNum;
        }

        @Override
        public void run() {
            clearFlushesMessage(tradeNum);
        }
    }

    public void loadDbFlushesMessage(){
        PosDataBaseFactory.getIntance().openPosDatabase();
        List<Flushes> list = PosDataBaseFactory.getIntance().query(Flushes.class, null, null, null, null, null);
        PosDataBaseFactory.getIntance().closePosDatabase();

        int n = list.size();
        for (int i=0;i<n;i++){
            allFlushes.put(list.get(i).getTradeNUm(),list.get(i));
        }

        if(n>0 && mActivity!=null){
            mActivity.DBFlushesStart();
        }
    }

    public void clearFlushesMessage(String tradenumber){
        allFlushes.remove(tradenumber);

        PosDataBaseFactory.getIntance().openPosDatabase();
        PosDataBaseFactory.getIntance().delete(Flushes.class, "tradeNUm = " + tradenumber);
        PosDataBaseFactory.getIntance().closePosDatabase();
    }

    /**
     *
     * 结算对账成功后，清除数据库中当前批次的所有交易明细
     */
    public void clearTransctionRecords(){
        PosDataBaseFactory.getIntance().openPosDatabase();
        PosDataBaseFactory.getIntance().delete(TranslationRecord.class,null);
        PosDataBaseFactory.getIntance().closePosDatabase();
    }

    /**
     * 比较消息类型 和 交易处理马
     */
    public static class MessageType{
        private String mMsdId;
        private String mTradeCode;

        public MessageType(String mMsdId, String mTradeCode) {
            this.mMsdId = mMsdId;
            this.mTradeCode = mTradeCode;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof MessageType){
                MessageType mt = (MessageType) o;
                if(mMsdId==null || mTradeCode==null)
                    return false;
                return mMsdId.equals(mt.mMsdId) && mTradeCode.equals(mt.mTradeCode);
            }else{
                return false;
            }
        }
    }
}
