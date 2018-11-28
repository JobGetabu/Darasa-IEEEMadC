package com.job.darasalecturer.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.job.darasalecturer.R;
import com.job.darasalecturer.model.LecUser;
import com.job.darasalecturer.util.AppStatus;
import com.job.darasalecturer.util.DoSnack;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.job.darasalecturer.util.Constants.LECUSERCOL;


public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.signup_toolbar)
    Toolbar signupToolbar;
    @BindView(R.id.signup_input_email)
    TextInputLayout signupInputEmail;
    @BindView(R.id.signup_input_password)
    TextInputLayout signupInputPassword;
    @BindView(R.id.signup_btn)
    TextView signupBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private DoSnack doSnack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        setSupportActionBar(signupToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(AppCompatResources.getDrawable(this,R.drawable.ic_back));

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        doSnack = new DoSnack(this, SignupActivity.this);
    }

    @OnClick(R.id.signup_btn)
    public void onViewClicked() {
        if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {

            doSnack.showSnackbar("You're offline", "Retry", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onViewClicked();
                }
            });

            return;
        }

        if (validate()){

            String email = signupInputEmail.getEditText().getText().toString();
            String password = signupInputPassword.getEditText().getText().toString();

            final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));
            pDialog.setTitleText("Signing up...");
            pDialog.setCancelable(false);
            pDialog.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> authtask) {

                            if (authtask.isSuccessful()) {

                                //registration successful

                                String devicetoken = FirebaseInstanceId.getInstance().getToken();
                                String mCurrentUserid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                LecUser lecUser = new LecUser();

                                lecUser.setDevicetoken(devicetoken);

                                // Set the value of 'Users'
                                DocumentReference usersRef = mFirestore.collection(LECUSERCOL).document(mCurrentUserid);

                                usersRef.set(lecUser)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pDialog.dismissWithAnimation();
                                                sendToAccountSetup();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pDialog.dismiss();
                                        doSnack.errorPrompt("Oops...", e.getMessage());
                                    }
                                });


                            } else {
                                pDialog.dismiss();
                                doSnack.UserAuthToastExceptions(SignupActivity.this,authtask);
                            }

                        }
                    });

        }

    }

    private boolean validate() {
        boolean valid = true;

        String email = signupInputEmail.getEditText().getText().toString();
        String password = signupInputPassword.getEditText().getText().toString();

        if (email.isEmpty() || !isEmailValid(email)) {
            signupInputEmail.setError("enter valid email");
            valid = false;
        } else {
            signupInputEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            signupInputPassword.setError("at least 6 characters");
            valid = false;
        } else {
            signupInputPassword.setError(null);
        }

        return valid;
    }

    private void sendToAccountSetup() {

        Intent aIntent = new Intent(this, AccountSetupActivity.class);
        aIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        aIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(aIntent);
        finish();
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
