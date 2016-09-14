package com.mwdev.sxsmcardpay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.karics.library.zxing.android.CaptureActivity;
import com.ta.annotation.TAInjectView;

/**
 * Created by qiuyi on 16-8-31.
 */
public class InputtradeNum extends SxBaseActivity{
    private String title;
    private PosApplication myPosApplication;
    @TAInjectView(id=R.id.input_tradenum)
    EditText trademum_ed;
    @TAInjectView(id=R.id.input_batchnum)
    EditText batchmum_ed;
    @TAInjectView(id=R.id.input_referencenum)
    EditText referencenum_ed;
    @TAInjectView(id=R.id.tradenum_next)
    Button next_bt;
    @TAInjectView(id=R.id.scan)
    Button scan_bt;
    @TAInjectView(id=R.id.input_amount_ed)
    EditText tv_amount;
    int type;
    String original_amount;
    String original_batchNum;
    String retrieve_referenceNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tradenum_query);
    }

    @Override
    protected void onAfterOnCerate() {
        myPosApplication=(PosApplication)getApplication();
        type=myPosApplication.getConfig(PosApplication.PREFERENCECONFIG).getInt(MainMenuActivity.TYPE_KEY,5);
        if(type==MainMenuActivity.TRADE_CANCEL){
            title=getResources().getString(R.string.trade_cancel);
            setAcitvityTitle(title);
        }else if(type==MainMenuActivity.RETURN_GOODS){
            title=getResources().getString(R.string.return_goods);
            setAcitvityTitle(title);
        }
        next_bt.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if(!tv_amount.getText().toString().trim().equalsIgnoreCase("")&&!referencenum_ed.getText().toString().trim().equalsIgnoreCase("")&&!batchmum_ed.getText().toString().trim().equalsIgnoreCase("")&&!trademum_ed.getText().toString().trim().equalsIgnoreCase("")){
            switch (type){
                case MainMenuActivity.TRADE_CANCEL:
                    if(trademum_ed.getText().toString().trim().length()==6){
                        Intent intent=new Intent(InputtradeNum.this,ReadCardActivity.class);
                        intent.putExtra(ReadCardActivity.OPERATORNUM_KEY,getIntent().getStringExtra(ReadCardActivity.OPERATORNUM_KEY));
                        intent.putExtra(ReadCardActivity.OPERATORPW_KEY,getIntent().getStringExtra(ReadCardActivity.OPERATORPW_KEY));
                        intent.putExtra(ReadCardActivity.TRADENUM_KEY,trademum_ed.getText().toString().trim());
                        intent.putExtra(ReadCardActivity.AMOUNT_KEY,tv_amount.getText().toString().trim());
                        intent.putExtra(ReadCardActivity.BATCH,batchmum_ed.getText().toString().trim());
                        intent.putExtra(ReadCardActivity.REFERENCENUM,referencenum_ed.getText().toString().trim());
                        startActivity(intent);
                        finish();
                    }else Toast.makeText(InputtradeNum.this,getResources().getString(R.string.length_error_input_6bit_num),Toast.LENGTH_SHORT).show();
                    break;
                case MainMenuActivity.RETURN_GOODS:
                    if(trademum_ed.getText().toString().trim().length()==6){
                        Intent intent=new Intent(InputtradeNum.this,InputAmountNumActivity.class);
                        intent.putExtra(ReadCardActivity.OPERATORNUM_KEY,getIntent().getStringExtra(ReadCardActivity.OPERATORNUM_KEY));
                        intent.putExtra(ReadCardActivity.OPERATORPW_KEY,getIntent().getStringExtra(ReadCardActivity.OPERATORPW_KEY));
                        intent.putExtra(ReadCardActivity.TRADENUM_KEY,trademum_ed.getText().toString().trim());
                        intent.putExtra(ReadCardActivity.AMOUNT_KEY,original_amount);
                        intent.putExtra(ReadCardActivity.BATCH,batchmum_ed.getText().toString().trim());
                        intent.putExtra(ReadCardActivity.REFERENCENUM,referencenum_ed.getText().toString().trim());
                        startActivity(intent);
                        finish();
                    }else Toast.makeText(InputtradeNum.this,getResources().getString(R.string.length_error_input_6bit_num),Toast.LENGTH_SHORT).show();
                    break;
            }
        }else {
            Toast.makeText(InputtradeNum.this,getResources().getString(R.string.please_inputorscan),Toast.LENGTH_SHORT).show();
        }


    }
});
        scan_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(InputtradeNum.this, CaptureActivity.class);
                startActivityForResult(intent,1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==-1){
            if(requestCode==1){
               String result= data.getStringExtra("codedContent");
                String[] resultArray=result.split("-");
                trademum_ed.setText(resultArray[0]);
                original_amount=resultArray[3];
                tv_amount.setText(original_amount);
                batchmum_ed.setText(resultArray[1]);
                referencenum_ed.setText(resultArray[2]);
                original_batchNum=resultArray[1];
                retrieve_referenceNum=resultArray[2];
            }
        }else Toast.makeText(InputtradeNum.this,getResources().getString(R.string.scan_error),Toast.LENGTH_LONG).show();
    }

    @Override
    protected void doActionBarLeftClick() {

    }

    @Override
    protected void doActionBarRightClick() {

    }
}
