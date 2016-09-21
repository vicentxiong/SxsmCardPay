package com.mwdev.sxsmcardpay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.karics.library.zxing.android.CaptureActivity;
import com.mwdev.sxsmcardpay.database.PosDataBaseFactory;
import com.mwdev.sxsmcardpay.database.TranslationRecord;
import com.mwdev.sxsmcardpay.iso8583.Iso8583Mgr;
import com.ta.annotation.TAInjectView;

import java.util.List;

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
    PosDataBaseFactory myPosDataBaseFactory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tradenum_query);
    }

    @Override
    protected void onAfterOnCerate() {
        myPosApplication=(PosApplication)getApplication();
        type=myPosApplication.getConfig(PosApplication.PREFERENCECONFIG).getInt(MainMenuActivity.TYPE_KEY,5);
        myPosDataBaseFactory=PosDataBaseFactory.getIntance();
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
//        if(trademum_ed.getText().toString().trim().length()==6){
//            myPosDataBaseFactory.openPosDatabase();
//                        List<TranslationRecord> list= PosDataBaseFactory.getIntance().query(TranslationRecord.class,"DTLPOSSEQ="+trademum_ed.getText().toString().trim(),null,null,null,null);
//                        myPosDataBaseFactory.closePosDatabase();
//            Log.i("qiuyi","list.size()=================="+list.size());
//                        TranslationRecord t=list.get(0);
//                            original_batchNum= t.getDTLBATCHNO();
//                            retrieve_referenceNum=t.getDTLCENSEQ();
//                            original_amount=t.getDTLAMT();
//            Log.i("qiuyi","original_batchNum=================="+original_batchNum);
//            Log.i("qiuyi","retrieve_referenceNum=================="+retrieve_referenceNum);
//            Log.i("qiuyi","original_amount=================="+original_amount);
//        }
        //tv_amount   referencenum_ed  batchmum_ed  trademum_ed
        String amount=tv_amount.getText().toString().trim();
        if(tv_amount.getText().toString().trim().length()>0) {
            if (myPosApplication.getmIso8583Mgr().isnumtrue(amount)) {
//            if(myPosApplication.getmIso8583Mgr().getnum(amount,".")==1){
                if (referencenum_ed.getText().toString().trim().length() == 12) {
                    if (batchmum_ed.getText().toString().trim().length() == 6) {
                        if (trademum_ed.getText().toString().trim().length() == 6) {

                            switch (type) {
                                case MainMenuActivity.TRADE_CANCEL:

                                    Intent intent = new Intent(InputtradeNum.this, ReadCardActivity.class);
                                    intent.putExtra(ReadCardActivity.OPERATORNUM_KEY, getIntent().getStringExtra(ReadCardActivity.OPERATORNUM_KEY));
                                    intent.putExtra(ReadCardActivity.OPERATORPW_KEY, getIntent().getStringExtra(ReadCardActivity.OPERATORPW_KEY));
                                    intent.putExtra(ReadCardActivity.TRADENUM_KEY, trademum_ed.getText().toString().trim());
                                    intent.putExtra(ReadCardActivity.AMOUNT_KEY, tv_amount.getText().toString().trim());
                                    intent.putExtra(ReadCardActivity.BATCH, batchmum_ed.getText().toString().trim());
                                    intent.putExtra(ReadCardActivity.REFERENCENUM, referencenum_ed.getText().toString().trim());
                                    startActivity(intent);
                                    finish();

                                    break;
                                case MainMenuActivity.RETURN_GOODS:
//                    if(trademum_ed.getText().toString().trim().length()==6){
                                    Intent intent1 = new Intent(InputtradeNum.this, InputAmountNumActivity.class);
                                    intent1.putExtra(ReadCardActivity.OPERATORNUM_KEY, getIntent().getStringExtra(ReadCardActivity.OPERATORNUM_KEY));
                                    intent1.putExtra(ReadCardActivity.OPERATORPW_KEY, getIntent().getStringExtra(ReadCardActivity.OPERATORPW_KEY));
                                    intent1.putExtra(ReadCardActivity.TRADENUM_KEY, trademum_ed.getText().toString().trim());
                                    intent1.putExtra(ReadCardActivity.AMOUNT_KEY, original_amount);
                                    intent1.putExtra(ReadCardActivity.BATCH, batchmum_ed.getText().toString().trim());
                                    intent1.putExtra(ReadCardActivity.REFERENCENUM, referencenum_ed.getText().toString().trim());
                                    startActivity(intent1);
                                    finish();
//                    }else Toast.makeText(InputtradeNum.this,getResources().getString(R.string.length_error_input_6bit_num),Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(InputtradeNum.this, getResources().getString(R.string.length_error_input_6bit_tradenum), Toast.LENGTH_SHORT).show();
//            Toast.makeText(InputtradeNum.this,getResources().getString(R.string.please_inputorscan),Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(InputtradeNum.this, getResources().getString(R.string.length_error_input_6bit_batchnum), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(InputtradeNum.this, getResources().getString(R.string.length_error_input_12bit_referencenum), Toast.LENGTH_SHORT).show();
                }
//            }
        }else {
                Toast.makeText(InputtradeNum.this, getResources().getString(R.string.type_error_input_amount), Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(InputtradeNum.this, getResources().getString(R.string.length_error_input_amount), Toast.LENGTH_SHORT).show();
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
