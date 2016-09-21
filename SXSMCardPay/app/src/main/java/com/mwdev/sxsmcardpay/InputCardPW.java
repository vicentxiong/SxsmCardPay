package com.mwdev.sxsmcardpay;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.basewin.define.KeyType;
import com.basewin.services.ServiceManager;
import com.mwdev.sxsmcardpay.controler.MessageFilter;
import com.mwdev.sxsmcardpay.database.Flushes;
import com.mwdev.sxsmcardpay.database.PosDataBaseFactory;
import com.mwdev.sxsmcardpay.iso8583.Iso8583Mgr;
import com.mwdev.sxsmcardpay.iso8583.util;
import com.mwdev.sxsmcardpay.util.PosLog;
import com.mwdev.sxsmcardpay.util.PosUtil;
import com.ta.annotation.TAInjectResource;
import com.ta.annotation.TAInjectView;
import com.ta.util.config.TAIConfig;

/**
 * Created by qiuyi on 16-8-27.
 */
public class InputCardPW extends SxRequestActivity implements View.OnClickListener{
    @TAInjectResource(id = R.string.please_input_card_pw)
    String title;
    String CardNum;
    @TAInjectView(id = R.id.cardnum)
    TextView cardnum_tv;
    @TAInjectView(id = R.id.consumption_amount_textView)
    TextView amount_tv;
    @TAInjectView(id =R.id.cardpassword)
    EditText cardpw;
    @TAInjectView(id=R.id.mernum)
    TextView mernum_tv;
    @TAInjectView(id =R.id.confirm_cardpwd)
    Button confirm_pw;
    @TAInjectView(id =R.id.key_one_button)
    Button key_one;
    @TAInjectView(id =R.id.key_two_button)
    Button key_two;
    @TAInjectView(id =R.id.key_three_button)
    Button key_three;
    @TAInjectView(id =R.id.key_four_button)
    Button key_four;
    @TAInjectView(id =R.id.key_five_button)
    Button key_five;
    @TAInjectView(id =R.id.key_six_button)
    Button key_six;
    @TAInjectView(id =R.id.key_seven_button)
    Button key_seven;
    @TAInjectView(id =R.id.key_eight_button)
    Button key_eight;
    @TAInjectView(id =R.id.key_nine_button)
    Button key_nine;
    @TAInjectView(id =R.id.key_zero_button)
    Button key_zero;
    @TAInjectView(id =R.id.key_point_button)
    ImageButton key_point;
    @TAInjectView(id =R.id.key_fork_button)
    ImageButton key_fork;
    @TAInjectView(id=R.id.password_key)
    View passwordkey;
    @TAInjectView(id=R.id.fill1)
    TextView fill1;
    @TAInjectView(id=R.id.fill2)
    TextView fill2;
    String pw="";
    private Iso8583Mgr myIso8583Mgr;
    PosApplication myPosApplication;
    private TAIConfig mConfig;
    String tradeNum;
    int type;
    String psamid;
    String original_tradeNum;
    String original_batchNum;
    String retrieve_referenceNum;
    String batchnum;
    String original_amount;
    String operatorNum;
    public  final static int PRINT=11;
    private ToneGenerator mToneGenerator;  // 声音产生器
    private static final int DTMF_DURATION_MS = 120;
    String operatorPassWord;
    @TAInjectResource(id = R.string.save_cropname_key)
    private String cropname_key;
    String merchantNum;
    String input_amount;
    String binaryString_pin;
    String cardinfo;
    boolean isResponeTimeOut=false;
    public final static int IMPACT_MAC=0X1111;
    public final static int IMPACT_TIMEOUT=0X2222;
    public final static int IMPACT_ERROR=0X3333;
    PosDataBaseFactory myPosDataBaseFactory;
    boolean isback;
    private Handler myHandler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            if(msg.what==PRINT){

                printerMalFuctoinList();
                isback=true;
                Intent intent_3=new Intent(InputCardPW.this,Query_errorActivity.class);
                intent_3.putExtra(ReadCardActivity.ERROR_KEY,IMPACT_ERROR);
                startActivity(intent_3);
            }else {

                int j = cardpw.getText().toString().trim().length();
                String text = "";
                for (int i = 1; i <= j; i++) {
//                Log.i("qiuyi","text1=======>"+text);
                    text += "*";
//                Log.i("qiuyi","text2=======>"+text);
                }
                cardpw.setText(text);


            }

        }

    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_cardpwd);
        isback=true;
        init();
    }
    public void init(){
        mToneGenerator = new ToneGenerator(
                AudioManager.STREAM_DTMF, 80); // 设置声音的大小
        setVolumeControlStream(AudioManager.STREAM_DTMF);

        myPosApplication=(PosApplication)getApplication();
        myIso8583Mgr=myPosApplication.getmIso8583Mgr();
        mConfig = myPosApplication.getConfig(PosApplication.PREFERENCECONFIG);
        merchantNum = myPosApplication.getCropNum();
        type=mConfig.getInt(MainMenuActivity.TYPE_KEY,5);
        Intent intent=getIntent();
        CardNum=intent.getStringExtra(ReadCardActivity.CARDNUM_KEY);
        input_amount=intent.getStringExtra(ReadCardActivity.INPUT_AMOUNT);
        original_amount=intent.getStringExtra(ReadCardActivity.AMOUNT_KEY);
        cardinfo=intent.getStringExtra(ReadCardActivity.CARDINFO_KEY);
        Log.i("qiuyi","input_amount========>"+input_amount);
        if(type==MainMenuActivity.TRADE){
            Log.i("qiuyi","input_amount========>"+input_amount);

            amount_tv.setVisibility(View.VISIBLE);
            passwordkey.setVisibility(View.VISIBLE);
            cardpw.setVisibility(View.VISIBLE);
            amount_tv.setText(getResources().getString(R.string.consumption_amount)+input_amount+getResources().getString(R.string.yuan));
        }else if(type==MainMenuActivity.RETURN_GOODS){
            Log.i("qiuyi","input_amount========>"+input_amount);
            title=getResources().getString(R.string.return_goods);
            mernum_tv.setVisibility(View.VISIBLE);
            amount_tv.setVisibility(View.VISIBLE);
            fill1.setVisibility(View.VISIBLE);
            fill2.setVisibility(View.VISIBLE);
            mernum_tv.setText(getResources().getString(R.string.merchants_num)+merchantNum);
            amount_tv.setText(getResources().getString(R.string.retrurn_goods_amount)+input_amount+getResources().getString(R.string.yuan));
            confirm_pw.setText(getResources().getString(R.string.confirm_return_goods));
        }else if(type==MainMenuActivity.TRADE_CANCEL){
            Log.i("qiuyi","input_amount========>"+input_amount);
            title=getResources().getString(R.string.trade_cancel);
            mernum_tv.setVisibility(View.VISIBLE);
            amount_tv.setVisibility(View.VISIBLE);
            fill1.setVisibility(View.VISIBLE);
            fill2.setVisibility(View.VISIBLE);
            mernum_tv.setText(getResources().getString(R.string.merchants_num)+merchantNum);
            amount_tv.setText(getResources().getString(R.string.cancel_amount)+original_amount+getResources().getString(R.string.yuan));
            confirm_pw.setText(getResources().getString(R.string.confirm_cancel_trade));
        }else if(type==MainMenuActivity.BALANCE_QUERY){
            title=getResources().getString(R.string.query_balance);
            fill1.setVisibility(View.VISIBLE);
            fill2.setVisibility(View.VISIBLE);
            confirm_pw.setText(getResources().getString(R.string.confirm_query_amount));
        }
        Log.i("qiuyi","CardNum========>"+CardNum);
        setAcitvityTitle(title);
        cardnum_tv.setText(getResources().getString(R.string.cardnum_text)+CardNum);

    }

    @Override
    public void onNetworkStatus(boolean connect) {
        super.onNetworkStatus(connect);
        Log.i("qiuyi11","connect=======>"+connect);
        if(!connect){
            finish();
        }

    }

    @Override
    protected void onAfterOnCerate() {
//        setAcitvityTitle(title);
        key_one.setOnClickListener(this);
        key_two.setOnClickListener(this);
        key_three.setOnClickListener(this);
        key_four.setOnClickListener(this);
        key_five.setOnClickListener(this);
        key_six.setOnClickListener(this);
        key_seven.setOnClickListener(this);
        key_eight.setOnClickListener(this);
        key_nine.setOnClickListener(this);
        key_zero.setOnClickListener(this);
        key_fork.setOnClickListener(this);
        confirm_pw.setOnClickListener(this);
        myPosDataBaseFactory=PosDataBaseFactory.getIntance();
    }
    @Override
    public void onClick(View v) {
        Message m=new Message();
        int length=0;
        length=cardpw.getText().toString().trim().length();

        switch (v.getId()) {
            case R.id.key_one_button:

                keypress(1, length, m);
                break;
            case R.id.key_two_button:
                keypress(2, length, m);
                break;
            case R.id.key_three_button:
                keypress(3, length, m);
                break;
            case R.id.key_four_button:
                keypress(4, length, m);
                break;
            case R.id.key_five_button:
                keypress(5, length, m);
                break;
            case R.id.key_six_button:
                keypress(6, length, m);
                break;

            case R.id.key_seven_button:
                keypress(7, length, m);
                break;
            case R.id.key_eight_button:
                keypress(8, length, m);
                break;
            case R.id.key_nine_button:
                keypress(9, length, m);

                break;
            case R.id.key_zero_button:
                keypress(0, length, m);
                break;
            case R.id.key_fork_button:

                deletekeynum(length);
                break;
            case R.id.confirm_cardpwd:
                press_confimButton();
                break;

        }

    }
    public void press_confimButton(){

        switch (type) {
            case MainMenuActivity.BALANCE_QUERY:
                Log.i("qiuyi", "type=======>" + type);

                Log.i("qiuyi", "pw========>" + pw);
                if (CardNum!=null&&CardNum.length() > 12) {
                    tradeNum = myPosApplication.createTradeSerialNumber();
                    //binaryString_pin =getpinbinaryString(CardNum,pw);
                    psamid = myPosApplication.getPsamID();


                    Log.i("qiuyi","psamid====>"+psamid+
                            "\nmerchantNum=====>"+ merchantNum+
                            "\ncardinfo=======>" +cardinfo+
//                                        "\nbinaryString_pin====>" +binaryString_pin +
                            "\ntradeNum===>" +tradeNum);
                    if(psamid!=null&&!"".equalsIgnoreCase(psamid)){
                        if(merchantNum!=null&&!"".equalsIgnoreCase(merchantNum)){
//                                        if(binaryString_pin!=null&&!"".equalsIgnoreCase(binaryString_pin)){
                            if(tradeNum!=null&&!"".equalsIgnoreCase(tradeNum)){
                                if(cardinfo!=null&&!"".equalsIgnoreCase(cardinfo)){
                                    setDiglogText(getResources().getString(R.string.doqueryamount_now));
                                    isback=false;
                                    showProgressDiglog();
                                    sendRequest(myIso8583Mgr.balance_query(psamid, merchantNum
                                            , cardinfo, binaryString_pin, tradeNum), "0200", "311000");
                                }else{
                                    Toast.makeText(this, getResources().getString(R.string.cardinfo_error), Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                            }
//                                        }else{
//                                            Toast.makeText(this, getResources().getString(R.string.password_error), Toast.LENGTH_SHORT).show();
//                                            pw="";
//                                            cardpw.setText("");
//
//                                        }
                        }else{
                            Toast.makeText(this, getResources().getString(R.string.merchantNum_error), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }else {
                        Toast.makeText(this, getResources().getString(R.string.psam_error), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }else {
                    Toast.makeText(this, getResources().getString(R.string.cardnum_error), Toast.LENGTH_SHORT).show();
                    finish();
                }


                break;

            case MainMenuActivity.TRADE:
                if(pw.length()==6) {
                    Log.i("qiuyi", "type=======>" + MainMenuActivity.TRADE);
                    if (CardNum.length() > 12) {
                        tradeNum = myPosApplication.createTradeSerialNumber();
                        binaryString_pin = getpinbinaryString(CardNum, pw);
                        psamid = myPosApplication.getPsamID();
//                        merchantNum = myPosApplication.getCropNum();
                        batchnum = myPosApplication.getBattchNum();
//                            String password=Iso8583Mgr.hexString2binaryString("90D892CF608EA288");
                        Log.i("xiongxin", "batchnum===>" + batchnum);
                        Log.i("qiuyi", "psamid====>" + psamid +
                                "\nmerchantNum=====>" + merchantNum +
                                "\ncardinfo=======>" + cardinfo +
                                "\nbinaryString_pin====>" + binaryString_pin +
                                "\ntradeNum====>" + tradeNum +
                                "\nbatchnum====>" + batchnum +
                                "\ninput_amount=====>" + input_amount);
                        String amount_need8583 = Iso8583Mgr.inputamount2amount(input_amount);
                        Log.i("qiuyi", "amount_need8583====>" + amount_need8583);
                        if (psamid != null && !"".equalsIgnoreCase(psamid)) {
                            if (merchantNum != null && !"".equalsIgnoreCase(merchantNum)) {
                                if (binaryString_pin != null && !"".equalsIgnoreCase(binaryString_pin)) {
                                    if (amount_need8583 != null && !"".equalsIgnoreCase(amount_need8583)) {
                                        if (cardinfo != null && !"".equalsIgnoreCase(cardinfo)) {
                                            setDiglogText(getResources().getString(R.string.dotrade_now));
                                            isback=false;
                                            showProgressDiglog();
                                            sendRequest(myIso8583Mgr.trade(psamid, merchantNum, cardinfo, binaryString_pin, tradeNum, amount_need8583), "0200", "001000");

                                            Flushes flushes1 = new Flushes();
                                            flushes1.setMsgID("0400");
                                            flushes1.setTradeDealCode("001000");
                                            flushes1.setCardNum(cardinfo);
                                            flushes1.setAmount(amount_need8583);
                                            flushes1.setTradeNUm(tradeNum);
                                            flushes1.setOriginal_batchNum(batchnum);
                                            flushes1.setOriginal_tradeNum(tradeNum);
                                            flushes1.setImpact_reason("98");
                                            myPosApplication.addFlushes(flushes1);
                                        } else {
                                            Toast.makeText(this, getResources().getString(R.string.cardinfo_error), Toast.LENGTH_SHORT).show();
                                            finish();
                                        }

                                    } else {
                                        Toast.makeText(this, getResources().getString(R.string.input_error), Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(this, getResources().getString(R.string.password_error), Toast.LENGTH_SHORT).show();
                                    pw = "";
                                    cardpw.setText("");
                                }
                            } else {
                                Toast.makeText(this, getResources().getString(R.string.merchantNum_error), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.psam_error), Toast.LENGTH_LONG).show();
                            finish();
                        }

                    } else {
                        Toast.makeText(this, getResources().getString(R.string.cardnum_error), Toast.LENGTH_SHORT).show();
                    }
                } else Toast.makeText(this, getResources().getString(R.string.passwd_length_error), Toast.LENGTH_SHORT).show();
                break;
            case MainMenuActivity.TRADE_CANCEL:
                Log.i("qiuyi", "type=======>" + MainMenuActivity.TRADE_CANCEL);
                if(CardNum.length()>12) {
//                    merchantNum = myPosApplication.getCropNum();
                    psamid = myPosApplication.getPsamID();
                    tradeNum = myPosApplication.createTradeSerialNumber();
                    original_tradeNum = getIntent().getStringExtra(ReadCardActivity.TRADENUM_KEY);
                    original_batchNum = getIntent().getStringExtra(ReadCardActivity.BATCH);
                    retrieve_referenceNum = getIntent().getStringExtra(ReadCardActivity.REFERENCENUM);
//                            binaryString_pin = getpinbinaryString(CardNum, pw);
                    batchnum=myPosApplication.getBattchNum();
                    Log.i("qiuyi", "type=======>" + MainMenuActivity.TRADE_CANCEL);
//                        myPosDataBaseFactory.openPosDatabase();
//                        List<TranslationRecord> list= PosDataBaseFactory.getIntance().query(TranslationRecord.class,"DTLPOSSEQ="+original_tradeNum,null,null,null,null);
//                        myPosDataBaseFactory.closePosDatabase();
//                        TranslationRecord t=list.get(0);
//                            original_batchNum= t.getDTLBATCHNO();
//                            retrieve_referenceNum=t.getDTLCENSEQ();
                    String amount_need8583 = Iso8583Mgr.inputamount2amount(original_amount);
                    Log.i("qiuyi", "psamid====>" + psamid +
                            "\nmerchantNum=====>" + merchantNum +
                            "\ncardinfo=======>" + cardinfo +
//                                    "\nbinaryString_pin====>" + binaryString_pin +
                            "\ntradeNum====>" + tradeNum +
                            "\nretrieve_referenceNum====>" + retrieve_referenceNum +
                            "\noriginal_tradeNum====>" + original_tradeNum +
                            "\noriginal_batchNum====>" + original_batchNum +
                            "\namount_need8583=====>" + amount_need8583);
                    if (psamid != null && !"".equalsIgnoreCase(psamid)) {
                        if (merchantNum != null && !"".equalsIgnoreCase(merchantNum)) {
//                                    if (binaryString_pin != null && !"".equalsIgnoreCase(binaryString_pin)) {

                            if (cardinfo != null && !"".equalsIgnoreCase(cardinfo)) {
                                if (retrieve_referenceNum != null && !"".equalsIgnoreCase(retrieve_referenceNum)&&original_tradeNum != null && !"".equalsIgnoreCase(original_tradeNum) && original_batchNum != null && !"".equalsIgnoreCase(original_batchNum) && amount_need8583 != null && !"".equalsIgnoreCase(amount_need8583)) {
                                    setDiglogText(getResources().getString(R.string.dotrade_cancel_now));
                                    isback=false;
                                    showProgressDiglog();
                                    sendRequest(myIso8583Mgr.trade_cancel(psamid, merchantNum, cardinfo, binaryString_pin, tradeNum, retrieve_referenceNum, original_tradeNum, original_batchNum, amount_need8583), "0200", "201000");
                                    Flushes flushes2 = new Flushes();
                                    flushes2.setMsgID("0400");
                                    flushes2.setTradeDealCode("201000");
                                    flushes2.setCardNum(cardinfo);
                                    flushes2.setAmount(amount_need8583);
                                    flushes2.setTradeNUm(tradeNum);
                                    flushes2.setOriginal_batchNum(batchnum);
                                    flushes2.setOriginal_tradeNum(tradeNum);
                                    flushes2.setImpact_reason("98");
                                    myPosApplication.addFlushes(flushes2);

                                }else{
                                    Toast.makeText(this, getResources().getString(R.string.original_data_error), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }else{
                                Toast.makeText(this, getResources().getString(R.string.cardinfo_error), Toast.LENGTH_SHORT).show();
                                finish();
                            }

//                                    }else{
//                                        Toast.makeText(this, getResources().getString(R.string.password_error), Toast.LENGTH_SHORT).show();
//                                        pw="";
//                                        cardpw.setText("");
//                                    }
                        }else{
                            Toast.makeText(this, getResources().getString(R.string.merchantNum_error), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }else{
                        Toast.makeText(this, getResources().getString(R.string.psam_error), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }else {
                    Toast.makeText(this, getResources().getString(R.string.cardnum_error), Toast.LENGTH_SHORT).show();
                }

                break;
            case MainMenuActivity.RETURN_GOODS:
                Log.i("qiuyi", "type=======>" + MainMenuActivity.RETURN_GOODS);
                if(CardNum.length()>12) {
//                    merchantNum = myPosApplication.getCropNum();
                    psamid = myPosApplication.getPsamID();
                    tradeNum = myPosApplication.createTradeSerialNumber();
                    original_tradeNum = getIntent().getStringExtra(ReadCardActivity.TRADENUM_KEY);
                    original_batchNum = getIntent().getStringExtra(ReadCardActivity.BATCH);
                    retrieve_referenceNum = getIntent().getStringExtra(ReadCardActivity.REFERENCENUM);
//                            binaryString_pin = getpinbinaryString(CardNum, pw);
                    operatorNum = getIntent().getStringExtra(ReadCardActivity.OPERATORNUM_KEY);
                    operatorPassWord = getIntent().getStringExtra(ReadCardActivity.OPERATORPW_KEY);
                    String input_amount_need8583 = Iso8583Mgr.inputamount2amount(input_amount);
                    Log.i("qiuyi", "psamid====>" + psamid +
                            "\nmerchantNum=====>" + merchantNum +
                            "\ncardinfo=======>" + cardinfo +
//                                    "\nbinaryString_pin====>" + binaryString_pin +
                            "\ntradeNum====>" + tradeNum +
                            /**"\noperatorNum====>" + operatorNum +
                             "\noperatorPassWord====>" + operatorPassWord +*/
                            "\nretrieve_referenceNum====>" + retrieve_referenceNum +
                            "\noriginal_tradeNum====>" + original_tradeNum +
                            "\noriginal_batchNum====>" + original_batchNum +
                            "\ninput_amount_need8583=====>" + input_amount_need8583);
                    if(psamid != null && !"".equalsIgnoreCase(psamid)){
                        if(merchantNum != null && !"".equalsIgnoreCase(merchantNum)){
//                                    if(binaryString_pin != null && !"".equalsIgnoreCase(binaryString_pin)) {
                            if (cardinfo != null && !"".equalsIgnoreCase(cardinfo)){
                                if (input_amount_need8583 != null && !"".equalsIgnoreCase(input_amount_need8583)) {
                                    if (retrieve_referenceNum != null && !"".equalsIgnoreCase(retrieve_referenceNum) && original_tradeNum != null && !"".equalsIgnoreCase(original_tradeNum) && original_batchNum != null && !"".equalsIgnoreCase(original_batchNum)) {
                                        setDiglogText(getResources().getString(R.string.doreturngood_now));
                                        isback=false;
                                        showProgressDiglog();
                                        sendRequest(myIso8583Mgr.Return_goods(psamid, merchantNum, cardinfo, binaryString_pin, tradeNum, input_amount_need8583, retrieve_referenceNum, original_tradeNum, original_batchNum), "0220", "401000");
                                    } else {
                                        Toast.makeText(this, getResources().getString(R.string.original_data_error), Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(this, getResources().getString(R.string.returngoods_amount_error), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }else {
                                Toast.makeText(this, getResources().getString(R.string.cardinfo_error), Toast.LENGTH_SHORT).show();
                                finish();
                            }
//                                    }else{
//                                        Toast.makeText(this, getResources().getString(R.string.password_error), Toast.LENGTH_SHORT).show();
//                                        pw="";
//                                        cardpw.setText("");
//                                    }
                        }else {
                            Toast.makeText(this, getResources().getString(R.string.merchantNum_error), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }else {
                        Toast.makeText(this, getResources().getString(R.string.psam_error), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }else{
                    Toast.makeText(this, getResources().getString(R.string.cardnum_error), Toast.LENGTH_SHORT).show();
                }
                break;

            default:

                break;

        }



    }


    public String getpinbinaryString(String cardnum,String password){
        String binaryString_pin;
//        String CardNum_12 = cardnum.substring(4, cardnum.length());
        byte[] pin = PosUtil.createPinEncryption(password, cardnum);
        Log.i("xxx","password================>"+password);
        Log.i("xxx","cardnum================>"+cardnum);
        Log.i("xxx","password================>"+password);
        String hexStringpin = null;
        try {
            hexStringpin = ServiceManager.getInstence().getPinpad().encryptData(KeyType.PIN_KEY, util.byteArray2Hex(pin));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("xxx","hexStringpin================>"+hexStringpin);
        binaryString_pin = Iso8583Mgr.hexString2binaryString(hexStringpin);

        return binaryString_pin;
    }

    @Override
    protected void doConnectCreated() {

    }

    @Override
    protected void doConnected() {

    }

    @Override
    protected void doDisconnected() {

    }

    @Override
    protected void doCommunictionIdle(String idle) {

    }

    @Override
    protected void doExceptionCaught(Throwable throwable) {

    }

    @Override
    protected void doMessageReceivered(Object message) {
        dismissProgressDiglog();
        isback=true;
        switch (type){
            case MainMenuActivity.BALANCE_QUERY:

                String amount_4=myIso8583Mgr.getManager_unpackData().getBit("4");
                if(amount_4!=null&&amount_4.length()>0){
//                    String amount=amount_4.substring(16,amount_4.length());
                    Intent intent=new Intent(InputCardPW.this,query_successActivity.class);
                    intent.putExtra(ReadCardActivity.CARDNUM_KEY,CardNum);
                    intent.putExtra(ReadCardActivity.AMOUNT_KEY,amount_4);
                    Log.i("qiuyi","InputCardPW   CardNum=====>"+CardNum+"\n amount=======>"+amount_4);
                    startActivity(intent);
                    finish();
                }
                break;
            case MainMenuActivity.TRADE:

                String amount_37=myIso8583Mgr.getManager_unpackData().getBit("37");
                Intent intent=new Intent(InputCardPW.this,TradeSuccessActivity.class);
                intent.putExtra(ReadCardActivity.CARDNUM_KEY,CardNum);
                intent.putExtra(ReadCardActivity.TRADENUM_KEY,tradeNum);
                intent.putExtra(ReadCardActivity.INPUT_AMOUNT,input_amount);
                intent.putExtra(ReadCardActivity.BATCH,batchnum);
                intent.putExtra(ReadCardActivity.REFERENCENUM,amount_37);
                startActivity(intent);
                finish();
                break;
            case MainMenuActivity.TRADE_CANCEL:

                Intent intent_tradecancel=new Intent(InputCardPW.this,TradeSuccessActivity.class);
                intent_tradecancel.putExtra(ReadCardActivity.CARDNUM_KEY,CardNum);
                intent_tradecancel.putExtra(ReadCardActivity.TRADENUM_KEY,tradeNum);
                intent_tradecancel.putExtra(ReadCardActivity.AMOUNT_KEY,original_amount);
                startActivity(intent_tradecancel);
                finish();
                break;
            case MainMenuActivity.RETURN_GOODS:

                Intent intent_returngoods=new Intent(InputCardPW.this,TradeSuccessActivity.class);
                intent_returngoods.putExtra(ReadCardActivity.CARDNUM_KEY,CardNum);
                intent_returngoods.putExtra(ReadCardActivity.TRADENUM_KEY,tradeNum);
                intent_returngoods.putExtra(ReadCardActivity.INPUT_AMOUNT,input_amount);
                startActivity(intent_returngoods);
                finish();

                break;
            default:

                break;

        }


    }

    @Override
    protected void doonMessageSent(MessageFilter.MessageType type) {

    }


    @Override
    protected void doMessageFilterResult(int result) {
        Log.i("qiuyi", "doMessageFilterResult     result=====>" + result);
        dismissProgressDiglog();
        isback=true;
        switch (type){
            case MainMenuActivity.BALANCE_QUERY:

                switch (result){

                    case 0xA0:
                        Log.i("qiuyi", "doMessageFilterResult     result=====>" + result);
                        onHanderToast(R.string.checkIn_again);
                        Intent intent1=new Intent(InputCardPW.this,LoginActivity.class);
                        startActivity(intent1);
                        finish();
                        break;

                    default:
                        Log.i("qiuyi","doMessageFilterResult     result=====>"+result);
                        Intent intent=new Intent(InputCardPW.this,Query_errorActivity.class);
                        intent.putExtra(ReadCardActivity.ERROR_KEY,result);
                        startActivity(intent);
                        finish();
                        break;

                }

                break;
            case MainMenuActivity.TRADE:

                switch (result){
//                    case -2:
//                        Flushes flushes = new Flushes();
//                        flushes.setMsgID("0400");
//                        flushes.setTradeDealCode("001000");
//                        flushes.setCardNum(cardinfo);
//                        flushes.setAmount(input_amount);
//                        flushes.setTradeNUm(tradeNum);
//                        flushes.setOriginal_batchNum(batchnum);
//                        flushes.setOriginal_tradeNum(tradeNum);
//                        flushes.setImpact_reason("A0");
//                        myPosApplication.addFlushes(flushes);
//                        setDiglogText(getResources().getString(R.string.impact_now));
//                        showProgressDiglog();
//                        break;

                    case -3:
                        dismissProgressDiglog();
                        isback=true;
                        onHanderToast(R.string.impact_success);
                            Intent intent_3=new Intent(InputCardPW.this,Query_errorActivity.class);
//                            String reson=getResources().getString(R.string.ResponeTimeOut_impact);
                            intent_3.putExtra(ReadCardActivity.ERROR_KEY,IMPACT_TIMEOUT);
                            startActivity(intent_3);
                            finish();


                        break;

                    case 0xA0:
                        Log.i("qiuyi", "doMessageFilterResult     result=====>" + result);
                        onHanderToast(R.string.checkIn_again);
                        Intent intent1=new Intent(InputCardPW.this,LoginActivity.class);
                        startActivity(intent1);
                        finish();
                        break;

                    default:

                        Log.i("qiuyi","doMessageFilterResult     result=====>"+result);
                        Intent intent_trade=new Intent(InputCardPW.this,Query_errorActivity.class);
                        intent_trade.putExtra(ReadCardActivity.ERROR_KEY,result);
                        startActivity(intent_trade);
                        finish();
                        break;

                }
                break;
            case MainMenuActivity.TRADE_CANCEL:
                switch (result){
//                    case -2:
//                        Flushes flushes = new Flushes();
//                        flushes.setMsgID("0400");
//                        flushes.setTradeDealCode("201000");
//                        flushes.setCardNum(cardinfo);
//                        flushes.setAmount(original_amount);
//                        flushes.setTradeNUm(tradeNum);
//                        flushes.setOriginal_batchNum(batchnum);
//                        flushes.setOriginal_tradeNum(tradeNum);
//                        flushes.setImpact_reason("A0");
//                        myPosApplication.addFlushes(flushes);
//                        setDiglogText(getResources().getString(R.string.impact_now));
//                        showProgressDiglog();
//                        break;
                    case -3:
                        dismissProgressDiglog();
                        isback=true;
                        onHanderToast(R.string.impact_success);
                        Intent intent_3=new Intent(InputCardPW.this,Query_errorActivity.class);
//                            String reson=getResources().getString(R.string.ResponeTimeOut_impact);
                        intent_3.putExtra(ReadCardActivity.ERROR_KEY,IMPACT_TIMEOUT);
                        startActivity(intent_3);
                        finish();
                        break;
                    case 0xA0:
                        Log.i("qiuyi", "doMessageFilterResult     result=====>" + result);
                        onHanderToast(R.string.checkIn_again);
                        Intent intent1=new Intent(InputCardPW.this,LoginActivity.class);
                        startActivity(intent1);
                        finish();
                        break;
                    default:
                        Log.i("qiuyi","doMessageFilterResult     result=====>"+result);
                        Intent intent_trade=new Intent(InputCardPW.this,Query_errorActivity.class);
                        intent_trade.putExtra(ReadCardActivity.ERROR_KEY,result);
                        startActivity(intent_trade);
                        break;
                }
                break;
            case MainMenuActivity.RETURN_GOODS:
                switch (result){
                    case 0xA0:
                        Log.i("qiuyi","doMessageFilterResult     result=====>"+result);
                        onHanderToast(R.string.checkIn_again);
                        Intent intent1=new Intent(InputCardPW.this,LoginActivity.class);
                        startActivity(intent1);
                        finish();
                        break;
                    default:
                        Log.i("qiuyi","doMessageFilterResult     result=====>"+result);
                        Intent intent_trade=new Intent(InputCardPW.this,Query_errorActivity.class);
                        intent_trade.putExtra(ReadCardActivity.ERROR_KEY,result);
                        startActivity(intent_trade);
                        break;

                }
                break;

        }



    }

    @Override
    protected void doConnectFail() {
//        dismissProgressDiglog();
        Log.i("qiuyi11","doConnectFail()");
        onHanderToast(R.string.network_error);
        finish();


    }

    @Override
    protected void doResponeTimeOut(MessageFilter.MessageType messagetype) {
//        isResponeTimeOut=true;
        PosLog.i("qiuyi11","===doResponeTimeOut===");
        dismissProgressDiglog();
        switch (type){
            case MainMenuActivity.BALANCE_QUERY:
                Intent intent=new Intent(InputCardPW.this,Query_errorActivity.class);
                intent.putExtra(ReadCardActivity.ERROR_KEY,0x98);
                startActivity(intent);
                finish();
                break;
            case MainMenuActivity.RETURN_GOODS:
                Intent intent_trade=new Intent(InputCardPW.this,Query_errorActivity.class);
                intent_trade.putExtra(ReadCardActivity.ERROR_KEY,0x98);
                startActivity(intent_trade);
                break;
            case MainMenuActivity.TRADE:
                if(messagetype.equals(myPosApplication.getSocketClient().getFilter().TRADE_IMPACT_REQUEST_TYPE)){
//                    printerMalFuctoinList();
                    Message m=new Message();
                    m.what=PRINT;
                    myHandler.sendMessage(m);
                }else {
                    onHandlerDialogText(R.string.ResponeTimeOut_impact);
                    isback=false;
                    showProgressDiglog();
                }
                break;
            case MainMenuActivity.TRADE_CANCEL:
                if(messagetype.equals(myPosApplication.getSocketClient().getFilter().TRADE_CANCEL_IMPACT_REQUEST_TYPE)){
//                    printerMalFuctoinList();
                    Message m=new Message();
                    m.what=PRINT;
                    myHandler.sendMessage(m);

                }else {
                    onHandlerDialogText(R.string.ResponeTimeOut_impact);
                    isback=false;
                    showProgressDiglog();
                }
                break;
        }
    }


    @Override
    protected void doActionBarLeftClick() {

    }

    @Override
    protected void doActionBarRightClick() {

    }
    public void keypress(int keynum,int length,Message m){
        if(length<6){
            mToneGenerator.startTone(3, DTMF_DURATION_MS);
            if(length>0){
                String text="";
                for(int i=1;i<=length;i++){
//                    Log.i("qiuyi","text1=======>"+text);
                    text+="*";
//                    Log.i("qiuyi","text2=======>"+text);
                }
                cardpw.setText(text);
            }

            m.what=keynum;
            myHandler.sendMessageDelayed(m,500);
            pw=pw+keynum;
            Log.i("qiuyi1","pw=======>"+pw);
            cardpw.setText(cardpw.getText().toString().trim()+keynum);
        }else{
//            Toast.makeText(this, getResources().getString(R.string.password_length_overstep), Toast.LENGTH_SHORT).show();
        }

    }

    public void deletekeynum(int length){
        if (length > 0) {
            String text = "";
            for (int i = 1; i <= length - 1; i++) {
//                Log.i("qiuyi", "text1=======>" + text);
                text += "*";
//                Log.i("qiuyi", "text2=======>" + text);
            }
            cardpw.setText(text);
            pw=pw.substring(0,pw.length()-1);
            Log.i("qiuyi1","pw=======>"+pw);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(isback){
                finish();
            }

        }
        return false;
    }

    @Override
    protected void printerFinish() {
//        super.printerFinish();
        finish();
    }
}
