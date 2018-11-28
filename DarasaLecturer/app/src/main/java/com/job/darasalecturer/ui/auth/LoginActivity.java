package com.job.darasalecturer.ui.auth;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
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
import com.job.darasalecturer.ui.MainActivity;
import com.job.darasalecturer.util.AppStatus;
import com.job.darasalecturer.util.DoSnack;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.job.darasalecturer.util.Constants.LECUSERCOL;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "login";

    @BindView(R.id.login_toolbar)
    Toolbar loginToolbar;
    @BindView(R.id.login_input_email)
    TextInputLayout loginInputEmail;
    @BindView(R.id.login_input_password)
    TextInputLayout loginInputPassword;
    @BindView(R.id.login_btn)
    TextView loginBtn;
    @BindView(R.id.login_forgotpass)
    TextView loginForgotpass;


    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private DoSnack doSnack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setSupportActionBar(loginToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(AppCompatResources.getDrawable(this,R.drawable.ic_back));

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        doSnack = new DoSnack(this, LoginActivity.this);

        loginForgotpass.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            sendToMain();
        }
    }

    @OnClick(R.id.login_btn)
    public void onLoginBtnViewClicked() {

        if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {

            doSnack.showSnackbar("You're offline", "Retry", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onLoginBtnViewClicked();
                }
            });

            return;
        }


        String email = loginInputEmail.getEditText().getText().toString();
        String password = loginInputPassword.getEditText().getText().toString();

        if (validate()) {

            final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));
            pDialog.setTitleText("Logging in...");
            pDialog.setCancelable(false);
            pDialog.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> authtask) {
                            if (authtask.isSuccessful()) {

                                //login successful

                                //update device token

                                String devicetoken = FirebaseInstanceId.getInstance().getToken();
                                String mCurrentUserid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                // Set the value of 'Users'
                                DocumentReference usersRef = mFirestore.collection(LECUSERCOL).document(mCurrentUserid);

                                usersRef.update("devicetoken", devicetoken)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pDialog.dismissWithAnimation();
                                                sendToMain();
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
                                doSnack.UserAuthToastExceptions(LoginActivity.this,authtask);
                                loginForgotpass.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }

    private void sendToMain() {

        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    private boolean validate() {
        boolean valid = true;

        String email = loginInputEmail.getEditText().getText().toString();
        String password = loginInputPassword.getEditText().getText().toString();

        if (email.isEmpty() || !isEmailValid(email)) {
            loginInputEmail.setError("enter valid email");
            valid = false;
        } else {
            loginInputEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            loginInputPassword.setError("at least 6 characters");
            valid = false;
        } else {
            loginInputPassword.setError(null);
        }

        return valid;
    }

    private boolean isEmailValid(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @OnClick(R.id.login_forgotpass)
    public void onForgotPassViewClicked() {
        final String email = loginInputEmail.getEditText().getText().toString();


        if (email.isEmpty() || !isEmailValid(email)) {
            loginInputEmail.setError("enter valid email to reset password");
            return;
        } else {
            loginInputEmail.setError(null);
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            doSnack.showSnackbar("Email sent to " + email, "Check", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    Intent intent = new Intent(Intent.ACTION_MAIN);
                                    intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                    try {
                                        //startActivity(intent);
                                        startActivity(Intent.createChooser(intent, getString(R.string.chooseEmailClient)));
                                    } catch (ActivityNotFoundException e) { }
                                }
                            });
                        }else {
                            doSnack.showShortSnackbar("You're not registered :(");
                        }
                    }
                });
    }
}
