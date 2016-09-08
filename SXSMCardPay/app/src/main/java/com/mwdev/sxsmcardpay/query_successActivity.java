package com.mwdev.sxsmcardpay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mwdev.sxsmcardpay.iso8583.util;
import com.ta.annotation.TAInjectResource;
import com.ta.annotation.TAInjectView;

/**
 * Created by qiuyi on 16-8-30.
 */
public class query_successActivity extends SxBaseActivity implements View.OnClickListener{
   @TAInjectResource(id =R.string.query_balance)
   private String title;
    @TAInjectView(id=R.id.card_num_textview)
    private TextView cardnum_tv;
    @TAInjectView(id=R.id.banlance_textview)
    private TextView amount_tv;
    @TAInjectView(id=R.id.back_button)
    private Button back;
    PosApplication myPosApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_querysuccess);
    }

    @Override
    protected void onAfterOnCerate() {
        setAcitvityTitle(title);
        myPosApplication=(PosApplication)getApplication();
        Intent intent=getIntent();
        String cardnum=intent.getStringExtra(ReadCardActivity.CARDNUM_KEY);
        String amount=intent.getStringExtra(ReadCardActivity.AMOUNT_KEY);
        String realamount=real_amount(amount);
        Log.i("qiuyi","query_successActivity   cardnum=====>"+cardnum+"\n amount=======>"+amount);
        cardnum_tv.setText(getResources().getString(R.string.cardnum_text)+cardnum);
        amount_tv.setText(realamount);
        back.setOnClickListener(this);

    }

    public String real_amount(String amount){
        String s=util.Delete0(amount);
        //000000000000
        String zheng=s.substring(0,10);
        String xiaoshu=s.substring(10,s.length());
        String result="";
        for(int i=0;i<zheng.length();i++){
             if(!(zheng.charAt(i)+"").equalsIgnoreCase("0")){
                 if(i<9){
                     result=zheng.substring(i,zheng.length());
                     break;
                 }
             }
        }
        if(result.equalsIgnoreCase("")){
            result="0";
        }
            return result+"."+xiaoshu;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_button:
                Intent intent=new Intent(query_successActivity.this,MainMenuActivity.class);
                startActivity(intent);
                finish();
        }
    }
    @Override
    protected void doActionBarLeftClick() {

    }

    @Override
    protected void doActionBarRightClick() {

    }


}
