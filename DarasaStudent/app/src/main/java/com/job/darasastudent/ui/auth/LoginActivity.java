package com.job.darasastudent.ui.auth;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.job.darasastudent.R;
import com.job.darasastudent.model.StudentDetails;
import com.job.darasastudent.ui.MainActivity;
import com.job.darasastudent.util.AppStatus;
import com.job.darasastudent.util.DoSnack;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.job.darasastudent.util.Constants.STUDENTDETAILSCOL;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.login_email)
    TextInputLayout loginEmail;
    @BindView(R.id.login_password)
    TextInputLayout loginPassword;
    @BindView(R.id.forgotpass)
    TextView forgotpass;
    @BindView(R.id.login_button)
    Button loginButton;
    @BindView(R.id.login_via_google)
    LinearLayout loginViaGoogle;

    private static final String TAG = "login";
    public static final int RC_SIGN_IN = 1001;


    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private DoSnack doSnack;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_xml);
        ButterKnife.bind(this);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        doSnack = new DoSnack(this, LoginActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            sendToMain();
        }
    }

    @OnClick(R.id.forgotpass)
    public void onForgotpassClicked() {

        final String email = loginEmail.getEditText().getText().toString();


        if (email.isEmpty() || !isEmailValid(email)) {
            loginEmail.setError("enter valid email to reset password");
            return;
        } else {
            loginEmail.setError(null);
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

    @OnClick(R.id.login_button)
    public void onLoginButtonClicked() {

        if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {

            doSnack.showSnackbar("You're offline", "Retry", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onLoginButtonClicked();
                }
            });

            return;
        }


        String email = loginEmail.getEditText().getText().toString();
        String password = loginPassword.getEditText().getText().toString();

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
                                DocumentReference usersRef = mFirestore.collection(STUDENTDETAILSCOL).document(mCurrentUserid);

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
                                DoSnack.UserAuthToastExceptions(LoginActivity.this,authtask);
                            }
                        }
                    });
        }

    }

    @OnClick({R.id.login_via_google, R.id.login_via_google_image})
    public void onLoginViaGoogleClicked() {

        if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {

            doSnack.showSnackbar("You're offline", "Retry", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onLoginViaGoogleClicked();
                }
            });

            return;
        }

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        loginViaGoogle.setEnabled(false);
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

        String email = loginEmail.getEditText().getText().toString();
        String password = loginPassword.getEditText().getText().toString();

        if (email.isEmpty() || !isEmailValid(email)) {
            loginEmail.setError("enter valid email");
            valid = false;
        } else {
            loginEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            loginPassword.setError("at least 6 characters");
            valid = false;
        } else {
            loginPassword.setError(null);
        }

        return valid;
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
                Snackbar.make(findViewById(android.R.id.content), "Google sign in failed", Snackbar.LENGTH_LONG).show();
                loginViaGoogle.setEnabled(true);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));
        pDialog.setTitleText("Logging in...");
        pDialog.setCancelable(false);
        pDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser user = mAuth.getCurrentUser();

                            final String device_token = FirebaseInstanceId.getInstance().getToken();
                            final String mCurrentUserid = mAuth.getCurrentUser().getUid();

                            // refactor this not to write to DB each time...check if account exists

                            DocumentReference docReference = mFirestore.collection(STUDENTDETAILSCOL).document(mCurrentUserid);
                            docReference.get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                                                    //update token only
                                                    updateTokenOnly(mCurrentUserid, device_token, pDialog);

                                                } else {
                                                    Log.d(TAG, "No such document");

                                                    //logging in with no pre account
                                                    //region create fresh account

                                                    pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                                    pDialog.setTitleText("Account doesn't exists! \n Creating one...");

                                                    //write to db
                                                    writingToStudUsers(pDialog, device_token, user, mCurrentUserid);

                                                    //endregion
                                                }
                                            } else {
                                                Log.d(TAG, "get failed with ", task.getException());
                                            }
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            pDialog.dismissWithAnimation();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            loginViaGoogle.setEnabled(true);
                        }
                    }
                });
    }

    private void writingToStudUsers(final SweetAlertDialog pDialog, String device_token, FirebaseUser user, String mCurrentUserid) {

        StudentDetails studentDetails = new StudentDetails();
        studentDetails.setDevicetoken(device_token);
        studentDetails.setPhotourl(user.getPhotoUrl().toString());
        studentDetails.setFirstname(user.getDisplayName());
        studentDetails.setStudentid(mCurrentUserid);
        studentDetails.setYearofstudy("1"); //DEF = 1

        // Set the value of 'Users'
        DocumentReference usersRef = mFirestore.collection(STUDENTDETAILSCOL).document(mCurrentUserid);

        usersRef.set(studentDetails)
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
    }

    private void updateTokenOnly(String mCurrentUserid, String device_token, final SweetAlertDialog pDialog) {
        //update device token

        // Set the value of 'Users'
        DocumentReference usersRef = mFirestore.collection(STUDENTDETAILSCOL).document(mCurrentUserid);

        usersRef.update("devicetoken", device_token)
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

    }

    private void sendToAccountSetup() {
        Intent aIntent = new Intent(this, AccountSetupActivity.class);
        aIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        aIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(aIntent);
        finish();
    }
}
