package com.job.darasalecturer.ui.auth;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hanks.passcodeview.PasscodeView;
import com.job.darasalecturer.R;
import com.job.darasalecturer.model.LecAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.job.darasalecturer.util.Constants.LECAUTHCOL;

public class PasscodeActivity extends AppCompatActivity {

    @BindView(R.id.passcodeView)
    PasscodeView passcodeView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

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

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        passcodeView
                .setFirstInputTip(getResources().getString(R.string.enter_pin_4_digits))
                .setPasscodeLength(4)
                .setPasscodeType(PasscodeView.PasscodeViewType.TYPE_SET_PASSCODE)
                .setListener(new PasscodeView.PasscodeViewListener() {
                    @Override
                    public void onFail(String wrongNumber) {

                    }

                    @Override
                    public void onSuccess(String number) {

                        String s = passcodeView.getLocalPasscode();
                        LecAuth lecAuth = new LecAuth(s);

                        mFirestore.collection(LECAUTHCOL).document(mAuth.getUid()).set(lecAuth);
                        Toast.makeText(getApplication(), "Successful", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                });
    }
}
