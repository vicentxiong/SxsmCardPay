package com.mwdev.sxsmcardpay;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ta.annotation.TAInjectResource;
import com.ta.annotation.TAInjectView;
import com.ta.util.config.TAIConfig;

/**
 * Created by qiuyi on 16-8-30.
 */
public class TradeSuccessActivity extends SxBaseActivity{
    String title;
    PosApplication myPosApplication;
    private TAIConfig mConfig;
    String tradeNum;
    String merchantNum;
    String input_amount;
    String CardNum;
    int type;
    @TAInjectResource(id = R.string.save_cropname_key)
    private String cropname_key;
    @TAInjectView(id=R.id.cardnum_tradesuccess)
    TextView tv_cardnum;
    @TAInjectView(id=R.id.merchantsnum_tradesuccess)
    TextView tv_merchantnum;
    @TAInjectView(id=R.id.tradenum_tradesuccess)
    TextView tv_tradeNum;
    @TAInjectView(id=R.id.amount_tradesuccess)
    TextView tv_amount;
    @TAInjectView(id=R.id.sign_print_button)
    Button sign_bt;
    @TAInjectView(id=R.id.success_title)
    TextView tv_title;
    @TAInjectView(id=R.id.amount_textview)
    TextView tv_amount_left;
    String tv_titleString;
    String bt_String;
    String referenceNum;
    String batchnum;
    String amount_left;
    String bt_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumption_success);
    }

    @Override
    protected void onAfterOnCerate() {
        setAcitvityTitle(title);
        myPosApplication=(PosApplication) getApplication();
        mConfig = myPosApplication.getConfig(PosApplication.PREFERENCECONFIG);
        type=mConfig.getInt(MainMenuActivity.TYPE_KEY,5);
        //        merchantNum=mConfig.getString(cropname_key,"");
        merchantNum=myPosApplication.getCropNum();
        Intent intent=getIntent();
        tradeNum=intent.getStringExtra(ReadCardActivity.TRADENUM_KEY);
        input_amount=intent.getStringExtra(ReadCardActivity.INPUT_AMOUNT);
        CardNum=intent.getStringExtra(ReadCardActivity.CARDNUM_KEY);
        batchnum=intent.getStringExtra(ReadCardActivity.BATCH);

        switch (type){
            case MainMenuActivity.TRADE:
                title=getResources().getString(R.string.do_trade);
                tv_titleString=getResources().getString(R.string.trade_success);
                bt_String=getResources().getString(R.string.sign_print);
                amount_left=getResources().getString(R.string.consumption_amount);
                bt_text=getResources().getString(R.string.sign_print);
                break;
            case MainMenuActivity.TRADE_CANCEL:
                input_amount=intent.getStringExtra(ReadCardActivity.AMOUNT_KEY);
                title=getResources().getString(R.string.trade_cancel);
                tv_titleString=getResources().getString(R.string.trade_cancel_success);
                bt_String=getResources().getString(R.string.confirm);
                amount_left=getResources().getString(R.string.cancel_amount);
                bt_text=getResources().getString(R.string.confirm);
                break;
            case MainMenuActivity.RETURN_GOODS:
                title=getResources().getString(R.string.return_goods);
                tv_titleString=getResources().getString(R.string.return_goods_success);
                bt_String=getResources().getString(R.string.confirm);
                amount_left=getResources().getString(R.string.retrurn_goods_amount);
                bt_text=getResources().getString(R.string.confirm);
                break;
        }
        tv_cardnum.setText(getResources().getString(R.string.cardnum_text)+CardNum);
        tv_merchantnum.setText(getResources().getString(R.string.merchants_num)+merchantNum);
        tv_tradeNum.setText(getResources().getString(R.string.trade_num)+tradeNum);
        tv_amount.setText(input_amount);
        tv_amount_left.setText(amount_left);
        tv_title.setText(tv_titleString);
        sign_bt.setText(bt_text);
        sign_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt_press();
            }
        });

    }

    @Override
    protected void doActionBarLeftClick() {

    }

    @Override
    protected void doActionBarRightClick() {

    }

    public void bt_press(){
        switch (type){
            case MainMenuActivity.TRADE:
                Intent intent_trade=new Intent(TradeSuccessActivity.this,SignatrueActivity.class);
                intent_trade.putExtra(ReadCardActivity.TRADENUM_KEY,tradeNum);
                intent_trade.putExtra(ReadCardActivity.INPUT_AMOUNT,input_amount);
                intent_trade.putExtra(ReadCardActivity.CARDNUM_KEY,CardNum);
                intent_trade.putExtra(ReadCardActivity.REFERENCENUM,referenceNum);
                intent_trade.putExtra(ReadCardActivity.BATCH,batchnum);
                intent_trade.putExtra(ReadCardActivity.REFERENCENUM,getIntent().getStringExtra(ReadCardActivity.REFERENCENUM));
                startActivity(intent_trade);
                finish();
                break;
            case MainMenuActivity.TRADE_CANCEL:
                 Intent intent_tradecancel=new Intent(TradeSuccessActivity.this,MainMenuActivity.class);
                startActivity(intent_tradecancel);
                finish();
                break;
            case MainMenuActivity.RETURN_GOODS:
                Intent intent_returngoods=new Intent(TradeSuccessActivity.this,MainMenuActivity.class);
                startActivity(intent_returngoods);
                finish();
                break;
        }
    }

    @Override
    protected void doBatteryCheckout() {
        isfirstCheckout =false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        return false;
    }
}
