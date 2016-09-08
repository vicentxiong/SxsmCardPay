package com.mwdev.sxsmcardpay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mwdev.sxsmcardpay.iso8583.CardNumInterface;
import com.mwdev.sxsmcardpay.iso8583.Iso8583Mgr;
import com.ta.annotation.TAInjectResource;

/**
 * Created by qiuyi on 16-8-25.
 */
public class ReadCardActivity extends SxBaseActivity implements CardNumInterface{
    @TAInjectResource(id=R.string.read_card)
    private String title;
    private PosApplication myPosApplication;
    Iso8583Mgr myIso8583Mgr;
    public final static String CARDNUM_KEY="cardnum";
    public final static String TYPE_KEY="type";
    public final static String AMOUNT_KEY="amount";
    public final static String ERROR_KEY="error";
    public final static String INPUT_AMOUNT="input_amount";
    public final static String TRADENUM_KEY="tradenum";
    public final static String OPERATORNUM_KEY="operatonum";
    public final static String OPERATORPW_KEY="operatopw";
    //retrieve_referenceNum  检索参考号
    public final static String REFERENCENUM="retrieve_referenceNum";
    public final static String BATCH="batch";


//    String input_amount;
    int type;
    @Override
    protected void onAfterOnCerate() {
        setAcitvityTitle(title);
        Intent intent=getIntent();
//       input_amount=intent.getStringExtra(INPUT_AMOUNT);
//        Log.i("qiuyi","input_amount=======>"+input_amount);
        myPosApplication=(PosApplication)getApplication();
        myIso8583Mgr=myPosApplication.getmIso8583Mgr();
        myIso8583Mgr.addResigter(this);
        myIso8583Mgr.readCardNum();
        type=myPosApplication.getConfig(PosApplication.PREFERENCECONFIG).getInt(MainMenuActivity.TYPE_KEY,5);


    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            dismissProgressDiglog();
            String cardnum= (String) msg.obj;
            Log.i("qiuyi","type=======>"+type);
            switch (type){
                case MainMenuActivity.BALANCE_QUERY:
//                    if(cardnum!=null&&!cardnum.equalsIgnoreCase("")){
                        Intent i =new Intent(ReadCardActivity.this,InputCardPW.class);
                        i.putExtra(CARDNUM_KEY,cardnum);
                        startActivity(i);
                        finish();
//                    }

                    break;
                case MainMenuActivity.TRADE:

                    Intent i1 =new Intent(ReadCardActivity.this,InputCardPW.class);
                    i1.putExtra(CARDNUM_KEY,cardnum);
                    i1.putExtra(ReadCardActivity.INPUT_AMOUNT,getIntent().getStringExtra(ReadCardActivity.INPUT_AMOUNT));
                    startActivity(i1);
                    finish();


                    break;
                case MainMenuActivity.TRADE_CANCEL:

                    Intent i2 =new Intent(ReadCardActivity.this,InputCardPW.class);
                    i2.putExtra(CARDNUM_KEY,cardnum);
                    i2.putExtra(ReadCardActivity.TRADENUM_KEY,getIntent().getStringExtra(ReadCardActivity.TRADENUM_KEY));
                    i2.putExtra(ReadCardActivity.OPERATORNUM_KEY,getIntent().getStringExtra(ReadCardActivity.OPERATORNUM_KEY));
                    i2.putExtra(ReadCardActivity.OPERATORPW_KEY,getIntent().getStringExtra(ReadCardActivity.OPERATORPW_KEY));
                    i2.putExtra(ReadCardActivity.AMOUNT_KEY,getIntent().getStringExtra(ReadCardActivity.AMOUNT_KEY));
                    i2.putExtra(ReadCardActivity.BATCH,getIntent().getStringExtra(ReadCardActivity.BATCH));
                    i2.putExtra(ReadCardActivity.REFERENCENUM,getIntent().getStringExtra(ReadCardActivity.REFERENCENUM));
                    startActivity(i2);
                    finish();

                    break;
                case MainMenuActivity.RETURN_GOODS:
                    Intent i3 =new Intent(ReadCardActivity.this,InputCardPW.class);
                    i3.putExtra(CARDNUM_KEY,cardnum);
                    i3.putExtra(ReadCardActivity.TRADENUM_KEY,getIntent().getStringExtra(ReadCardActivity.TRADENUM_KEY));
                    i3.putExtra(ReadCardActivity.OPERATORNUM_KEY,getIntent().getStringExtra(ReadCardActivity.OPERATORNUM_KEY));
                    i3.putExtra(ReadCardActivity.OPERATORPW_KEY,getIntent().getStringExtra(ReadCardActivity.OPERATORPW_KEY));
                    i3.putExtra(ReadCardActivity.INPUT_AMOUNT,getIntent().getStringExtra(ReadCardActivity.INPUT_AMOUNT));
                    i3.putExtra(ReadCardActivity.BATCH,getIntent().getStringExtra(ReadCardActivity.BATCH));
                    i3.putExtra(ReadCardActivity.REFERENCENUM,getIntent().getStringExtra(ReadCardActivity.REFERENCENUM));
                    startActivity(i3);
                    finish();
                    break;
            }

        }
    };

    @Override
    protected void doActionBarLeftClick() {

    }

    @Override
    protected void doActionBarRightClick() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readcard);

    }




    @Override
    public void onScuess(String cardnum) {
        setDiglogText(getResources().getString(R.string.read_card_now));
        showProgressDiglog();
        Log.i("qiuyi","cardnum=======>"+cardnum);
        Message m=new Message();
        m.obj=cardnum;
        handler.sendMessageDelayed(m,400);


    }

}
