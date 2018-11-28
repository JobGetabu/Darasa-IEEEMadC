package com.job.darasalecturer.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hanks.passcodeview.PasscodeView;
import com.job.darasalecturer.R;
import com.job.darasalecturer.ui.MainActivity;
import com.job.darasalecturer.ui.ScannerActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowPasscodeActivity extends AppCompatActivity {

    public static final String SHOWPASSCODEACTIVITYEXTRA = "SHOWPASSCODEACTIVITYEXTRA";
    public static final String SHOWPASSCODEACTIVITYEXTRA2 = "SHOWPASSCODEACTIVITYEXTRA2";
    public static final String SHOWACTIONEXTRA = "SHOWACTIONEXTRA";

    public static final String OnBackPressAction = "OnBackPressAction";

    @BindView(R.id.passcodeView)
    PasscodeView passcodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_passcode);
        ButterKnife.bind(this);

    }

    private void showpassword(String vs){
       switch (vs){
           case OnBackPressAction:
               passcodeView
                       .setPasscodeLength(4)
                       .setLocalPasscode(getIntent().getStringExtra(SHOWPASSCODEACTIVITYEXTRA) )
                       .setFirstInputTip(getResources().getString(R.string.enter_your_pin))
                       .setPasscodeType(PasscodeView.PasscodeViewType.TYPE_CHECK_PASSCODE)
                       .setListener(new PasscodeView.PasscodeViewListener() {
                           @Override
                           public void onFail(String wrongNumber) {

                               onBackPressed();
                               ScannerActivity.userpasscode = null;
                           }

                           @Override
                           public void onSuccess(String number) {

                               startActivity(new Intent(ShowPasscodeActivity.this,MainActivity.class));
                               finish();

                           }
                       });
               break;
       }

    }

    private void sendToMain(String pin) {

        Intent resultIntent = new Intent();
        resultIntent.putExtra(SHOWPASSCODEACTIVITYEXTRA2, pin);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
