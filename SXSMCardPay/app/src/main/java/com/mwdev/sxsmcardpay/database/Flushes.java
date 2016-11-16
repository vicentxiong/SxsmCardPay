package com.mwdev.sxsmcardpay.database;

/**
 * Created by xiongxin on 16-8-19.
 */
public class Flushes {
    private String msgID;
    private String tradeDealCode;
    private String cardNum;
    private String tradeNUm;
    private String amount;
    private String original_batchNum;
    private String original_tradeNum;
    private String impact_reason;


    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public String getTradeDealCode() {
        return tradeDealCode;
    }

    public void setTradeDealCode(String tradeDealCode) {
        this.tradeDealCode = tradeDealCode;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getTradeNUm() {
        return tradeNUm;
    }

    public void setTradeNUm(String tradeNUm) {
        this.tradeNUm = tradeNUm;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOriginal_batchNum() {
        return original_batchNum;
    }

    public void setOriginal_batchNum(String original_batchNum) {
        this.original_batchNum = original_batchNum;
    }

    public String getOriginal_tradeNum() {
        return original_tradeNum;
    }

    public void setOriginal_tradeNum(String original_tradeNum) {
        this.original_tradeNum = original_tradeNum;
    }

    public String getImpact_reason() {
        return impact_reason;
    }

    public void setImpact_reason(String impact_reason) {
        this.impact_reason = impact_reason;
    }
}
