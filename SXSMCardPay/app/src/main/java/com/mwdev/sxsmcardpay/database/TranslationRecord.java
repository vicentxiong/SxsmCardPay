package com.mwdev.sxsmcardpay.database;

/**
 * Created by xiongxin on 16-8-16.
 */
public class TranslationRecord {
    private String DTLCARDNO;                       // 卡内号  （2）
    private String DTLCDCNT;                        // 卡计数器
    private String DTLPRCODE;                       // 交易处理码 （3）
    private String DTLTRANSTYPE;                    // 消息类型  （msgid）
    private String DTLPOSID;                       //清算设备编号  (41)
    private String DTLSAMID;                       //Sam卡号
    private String DTLPOSSEQ;                      //清算设备流水号 (11)
    private String DTLBATCHNO;                     //批次号 (60.2)
    private String DTLTERMID;                     //企业设备编号
    private String DTLTERMSEQ;                   //企业设备流水号
    private String DTLDATE;                        //交易日期  (13)
    private String DTLTIME;                        //交易时间  (12)
    private String DTLSETTDATE;                   //结算日期  (15)
    private String DTLCENSEQ;                    //中心流水号  (37)
    private String DTLSLAMT;                     //卡押金
    private String DTLBEFBAL;                    //交易前卡余额
    private String DTLAFTBAL;                     //交易后卡余额
    private String DTLAMT;                        //实际交易额度 (4)
    private String DTLUNITID;                     //营运公司代码  (42)
    private String DTLNETID;                      //网点代码
    private String DTLTAC;                        //交易验证码
    private String DTLTYPE;                       //明细类型

    public String getDTLCARDNO() {
        return DTLCARDNO;
    }

    public void setDTLCARDNO(String DTLCARDNO) {
        this.DTLCARDNO = DTLCARDNO;
    }

    public String getDTLCDCNT() {
        return DTLCDCNT;
    }

    public void setDTLCDCNT(String DTLCDCNT) {
        this.DTLCDCNT = DTLCDCNT;
    }

    public String getDTLPRCODE() {
        return DTLPRCODE;
    }

    public void setDTLPRCODE(String DTLPRCODE) {
        this.DTLPRCODE = DTLPRCODE;
    }

    public String getDTLTRANSTYPE() {
        return DTLTRANSTYPE;
    }

    public void setDTLTRANSTYPE(String DTLTRANSTYPE) {
        this.DTLTRANSTYPE = DTLTRANSTYPE;
    }

    public String getDTLPOSID() {
        return DTLPOSID;
    }

    public void setDTLPOSID(String DTLPOSID) {
        this.DTLPOSID = DTLPOSID;
    }

    public String getDTLSAMID() {
        return DTLSAMID;
    }

    public void setDTLSAMID(String DTLSAMID) {
        this.DTLSAMID = DTLSAMID;
    }

    public String getDTLPOSSEQ() {
        return DTLPOSSEQ;
    }

    public void setDTLPOSSEQ(String DTLPOSSEQ) {
        this.DTLPOSSEQ = DTLPOSSEQ;
    }

    public String getDTLBATCHNO() {
        return DTLBATCHNO;
    }

    public void setDTLBATCHNO(String DTLBATCHNO) {
        this.DTLBATCHNO = DTLBATCHNO;
    }

    public String getDTLTERMID() {
        return DTLTERMID;
    }

    public void setDTLTERMID(String DTLTERMID) {
        this.DTLTERMID = DTLTERMID;
    }

    public String getDTLTERMSEQ() {
        return DTLTERMSEQ;
    }

    public void setDTLTERMSEQ(String DTLTERMSEQ) {
        this.DTLTERMSEQ = DTLTERMSEQ;
    }

    public String getDTLDATE() {
        return DTLDATE;
    }

    public void setDTLDATE(String DTLDATE) {
        this.DTLDATE = DTLDATE;
    }

    public String getDTLTIME() {
        return DTLTIME;
    }

    public void setDTLTIME(String DTLTIME) {
        this.DTLTIME = DTLTIME;
    }

    public String getDTLSETTDATE() {
        return DTLSETTDATE;
    }

    public void setDTLSETTDATE(String DTLSETTDATE) {
        this.DTLSETTDATE = DTLSETTDATE;
    }

    public String getDTLCENSEQ() {
        return DTLCENSEQ;
    }

    public void setDTLCENSEQ(String DTLCENSEQ) {
        this.DTLCENSEQ = DTLCENSEQ;
    }

    public String getDTLSLAMT() {
        return DTLSLAMT;
    }

    public void setDTLSLAMT(String DTLSLAMT) {
        this.DTLSLAMT = DTLSLAMT;
    }

    public String getDTLBEFBAL() {
        return DTLBEFBAL;
    }

    public void setDTLBEFBAL(String DTLBEFBAL) {
        this.DTLBEFBAL = DTLBEFBAL;
    }

    public String getDTLAFTBAL() {
        return DTLAFTBAL;
    }

    public void setDTLAFTBAL(String DTLAFTBAL) {
        this.DTLAFTBAL = DTLAFTBAL;
    }

    public String getDTLAMT() {
        return DTLAMT;
    }

    public void setDTLAMT(String DTLAMT) {
        this.DTLAMT = DTLAMT;
    }

    public String getDTLUNITID() {
        return DTLUNITID;
    }

    public void setDTLUNITID(String DTLUNITID) {
        this.DTLUNITID = DTLUNITID;
    }

    public String getDTLNETID() {
        return DTLNETID;
    }

    public void setDTLNETID(String DTLNETID) {
        this.DTLNETID = DTLNETID;
    }

    public String getDTLTAC() {
        return DTLTAC;
    }

    public void setDTLTAC(String DTLTAC) {
        this.DTLTAC = DTLTAC;
    }

    public String getDTLTYPE() {
        return DTLTYPE;
    }

    public void setDTLTYPE(String DTLTYPE) {
        this.DTLTYPE = DTLTYPE;
    }
}
