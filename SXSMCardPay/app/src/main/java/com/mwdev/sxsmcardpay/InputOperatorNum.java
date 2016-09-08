package com.mwdev.sxsmcardpay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ta.annotation.TAInjectView;

/**
 * Created by qiuyi on 16-8-31.
 */
public class InputOperatorNum extends SxBaseActivity{
    private String title;
    private PosApplication myPosApplication;
    @TAInjectView(id=R.id.operatornum)
    EditText operatornum_ed;
    @TAInjectView(id=R.id.operatorpw)
    EditText operatorpw_ed;
    @TAInjectView(id=R.id.operatornext)
    Button   next_bt;
    int type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_operatornum);

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
                        if(!operatornum_ed.getText().toString().trim().equalsIgnoreCase("")&&!operatorpw_ed.getText().toString().trim().equalsIgnoreCase("")){
                            Intent intent=new Intent(InputOperatorNum.this,InputtradeNum.class);
                            intent.putExtra(ReadCardActivity.OPERATORNUM_KEY,operatornum_ed.getText().toString().trim());
                            intent.putExtra(ReadCardActivity.OPERATORPW_KEY,operatorpw_ed.getText().toString().trim());
                            startActivity(intent);
                            finish();
                        }else Toast.makeText(InputOperatorNum.this,"请输入",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void doActionBarLeftClick() {

    }

    @Override
    protected void doActionBarRightClick() {

    }
}
