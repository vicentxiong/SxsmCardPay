package com.mwdev.sxsmcardpay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ta.annotation.TAInjectResource;
import com.ta.annotation.TAInjectView;
import com.ta.util.config.TAIConfig;

/**
 * Created by qiuyi on 16-8-30.
 */
public class Query_errorActivity extends SxBaseActivity implements View.OnClickListener{
    @TAInjectResource(id =R.string.query_balance)
    private String title;
    @TAInjectView(id=R.id.error_message_textview)
    private TextView error_tv;
    @TAInjectView(id=R.id.error_back_button)
    private Button back;
    int type;
    PosApplication myPosApplication;
    TAIConfig mConfig;
    String error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queryerror);
    }

    @Override
    protected void onAfterOnCerate() {

        myPosApplication=(PosApplication)getApplication();
        mConfig = myPosApplication.getConfig(PosApplication.PREFERENCECONFIG);
        type=mConfig.getInt(MainMenuActivity.TYPE_KEY,5);
        back.setOnClickListener(this);
        Intent intent=getIntent();
        int result=intent.getIntExtra(ReadCardActivity.ERROR_KEY,0);

        switch (type){
            case MainMenuActivity.BALANCE_QUERY:
                error=getResources().getString(myPosApplication.getStringIdByCode(result));
                break;

            case MainMenuActivity.RETURN_GOODS:
                title=getString(R.string.return_goods);
                error=getResources().getString(myPosApplication.getStringIdByCode(result));

                break;

            case MainMenuActivity.TRADE:
                title=getString(R.string.do_trade);
                switch (result){
                    case InputCardPW.IMPACT_MAC:
                        error=getResources().getString(R.string.macerror_reason);
                        break;
                    case InputCardPW.IMPACT_TIMEOUT:
                        error=getResources().getString(R.string.timeout_reason);
                        break;
                    default:
                        error=getResources().getString(myPosApplication.getStringIdByCode(result));
                        break;
                }
                break;

            case MainMenuActivity.TRADE_CANCEL:
                title=getString(R.string.trade_cancel);
                switch (result){
                    case InputCardPW.IMPACT_MAC:
                        error=getResources().getString(R.string.macerror_reason);
                        break;
                    case InputCardPW.IMPACT_TIMEOUT:
                        error=getResources().getString(R.string.timeout_reason);
                        break;
                    default:
                        error=getResources().getString(myPosApplication.getStringIdByCode(result));
                        break;
                }
                break;
        }
        setAcitvityTitle(title);
        error_tv.setText(error);
    }

    @Override
    protected void doActionBarLeftClick() {

    }

    @Override
    protected void doActionBarRightClick() {

    }

    @Override
    public void onClick(View v) {
        Intent intent =new Intent(Query_errorActivity.this,MainMenuActivity.class);
        startActivity(intent);
        finish();
    }
}
