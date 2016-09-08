package com.mwdev.sxsmcardpay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ta.annotation.TAInjectResource;
import com.ta.annotation.TAInjectView;
import com.ta.util.config.TAIConfig;

/**
 * Created by qiuyi on 16-8-28.
 */
public class InputAmountNumActivity extends SxBaseActivity implements View.OnClickListener{
    @TAInjectResource(id = R.string.please_input_amount)
    String title;
    String merchantsnum;
    @TAInjectView(id = R.id.merchantsnum_consumption_textview)
    TextView merchantsnum_tv;
    @TAInjectView(id =R.id.input_amount_edittext)
    EditText amount_edit;
    @TAInjectView(id =R.id.input_amount_confirm)
    Button confirm_amount;
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
    @TAInjectResource(id = R.string.save_cropname_key)
    private String cropname_key;
    PosApplication myPosApplication;
    boolean point=false;
    int tep=0;
    int tep0=0;
    String trade_amount;
    private TAIConfig mConfig;
    int type;
//    public static String confirm_amount
public static String CONFIRM_AMOUNT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intput_amount);
    }

    @Override
    protected void onAfterOnCerate() {

        myPosApplication=(PosApplication)getApplication();
        setAcitvityTitle(title);
        type=myPosApplication.getConfig(PosApplication.PREFERENCECONFIG).getInt(MainMenuActivity.TYPE_KEY,5);
        if(type==MainMenuActivity.RETURN_GOODS){
            title=getResources().getString(R.string.please_input_return_amount);
            setAcitvityTitle(title);

        }
        mConfig = myPosApplication.getConfig(PosApplication.PREFERENCECONFIG);
        merchantsnum_tv.setText(getResources().getString(R.string.merchants_num)+mConfig.getString(cropname_key, ""));
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
        key_point.setOnClickListener(this);
        confirm_amount.setOnClickListener(this);
    }

    @Override
    protected void doActionBarLeftClick() {

    }

    @Override
    protected void doActionBarRightClick() {

    }

    @Override
    public void onClick(View v) {
String orign=amount_edit.getText().toString().trim();
        int length=orign.length();
        switch (v.getId()) {
            case R.id.key_one_button:

                key_press(1,length,orign);
                break;
            case R.id.key_two_button:
                key_press(2,length,orign);
                break;
            case R.id.key_three_button:
                key_press(3,length,orign);
                break;
            case R.id.key_four_button:
                key_press(4,length,orign);
                break;
            case R.id.key_five_button:
                key_press(5,length,orign);
                break;
            case R.id.key_six_button:
                key_press(6,length,orign);
                break;

            case R.id.key_seven_button:
                key_press(7,length,orign);
                break;
            case R.id.key_eight_button:
                key_press(8,length,orign);
                break;
            case R.id.key_nine_button:

                key_press(9,length,orign);
                break;
            case R.id.key_zero_button:
                press_zero(length,orign);
                break;
            case R.id.key_fork_button:

                press_fork(orign);
                break;
            case R.id.key_point_button:
                press_point();
                break;
            case R.id.input_amount_confirm:
                pressButton();
                break;

        }
    }

    public void key_press(int keynum,int length,String orign){
        if(!point){
          if(orign.equalsIgnoreCase("0.00")){
            orign=keynum+".00";
        }else{

            if(length>8){
                Toast.makeText(InputAmountNumActivity.this,"长度超标",Toast.LENGTH_SHORT).show();
            }else {
                orign=orign.substring(0,length-3)+keynum+".00";
            }

        }
        }else {
            if(tep==0){
                int i=orign.indexOf(".");
                orign=orign.substring(0,i+1)+keynum+"0";
                tep++;
           }else if(tep==1){
                orign=orign.substring(0,length-1)+keynum;
                tep++;
            }else Toast.makeText(InputAmountNumActivity.this,"精度超标",Toast.LENGTH_SHORT).show();
        }
        amount_edit.setText(orign);

    }
    public void press_point(){

        if(!point){
            point=true;
        }
    }

    public void press_zero(int length,String orign){
        if(!point){
            if(orign.equalsIgnoreCase("0.00")){

            }else{
                if(length>8){
                    Toast.makeText(InputAmountNumActivity.this,"长度超标",Toast.LENGTH_SHORT).show();
                }else {
                    int i=orign.indexOf(".");
                    orign=orign.substring(0,i)+"0.00";
                }

            }
        }else{
            if(tep<2){
                tep++;
            }
        }
        amount_edit.setText(orign);
    }

    public void press_fork(String orign){
        Log.i("qiuyi","xxxxxxxxxxx");
        if((orign.substring(orign.length()-2,orign.length()).equalsIgnoreCase("00"))){
            tep=0;
            point=false;
            int i=orign.indexOf(".");
            if(i==1){
                orign="0.00";
            }else {
                orign=orign.substring(0,i-1)+".00";
            }
        }else {
            if(!point){

                    int i=orign.indexOf(".");
                    if(i==1){
                        orign="0.00";
                    }else {
                        orign=orign.substring(0,i-1)+".00";
                    }

            }else{
                if(tep==2){
                    orign=orign.substring(0,orign.length()-1)+"0";
                    tep--;
                }else if(tep==1){
                    orign=orign.substring(0,orign.length()-2)+"00";
                    tep--;
                }else if(tep==0){

                    point=false;

                }
            }
        }

        amount_edit.setText(orign);
    }

    public void pressButton(){
        switch (type){

            case MainMenuActivity.RETURN_GOODS:
                Intent intent1=new Intent(InputAmountNumActivity.this,ReadCardActivity.class);
                trade_amount=amount_edit.getText().toString().trim();
//                if(trade_amount>getIntent().getStringExtra(ReadCardActivity.AMOUNT_KEY)){
//                }
                intent1.putExtra(ReadCardActivity.INPUT_AMOUNT,trade_amount);
                intent1.putExtra(ReadCardActivity.TRADENUM_KEY,getIntent().getStringExtra(ReadCardActivity.TRADENUM_KEY));
                intent1.putExtra(ReadCardActivity.OPERATORNUM_KEY,getIntent().getStringExtra(ReadCardActivity.OPERATORNUM_KEY));
                intent1.putExtra(ReadCardActivity.OPERATORPW_KEY,getIntent().getStringExtra(ReadCardActivity.OPERATORPW_KEY));
                intent1.putExtra(ReadCardActivity.BATCH,getIntent().getStringExtra(ReadCardActivity.BATCH));
                intent1.putExtra(ReadCardActivity.REFERENCENUM,getIntent().getStringExtra(ReadCardActivity.REFERENCENUM));
                intent1.putExtra(ReadCardActivity.AMOUNT_KEY,getIntent().getStringExtra(ReadCardActivity.AMOUNT_KEY));
                startActivity(intent1);
                finish();


                break;
            case MainMenuActivity.TRADE:
                Intent intent=new Intent(InputAmountNumActivity.this,ReadCardActivity.class);
                trade_amount=amount_edit.getText().toString().trim();
                intent.putExtra(ReadCardActivity.INPUT_AMOUNT,trade_amount);
                startActivity(intent);
                finish();
        }

    }
}
