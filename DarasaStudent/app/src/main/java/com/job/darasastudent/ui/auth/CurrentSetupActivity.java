package com.job.darasastudent.ui.auth;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.job.darasastudent.R;
import com.job.darasastudent.model.StudentDetails;
import com.job.darasastudent.ui.MainActivity;
import com.job.darasastudent.util.AppStatus;
import com.job.darasastudent.util.DoSnack;
import com.job.darasastudent.viewmodel.AccountSetupViewModel;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.job.darasastudent.util.Constants.CURRENT_SEM_PREF_NAME;
import static com.job.darasastudent.util.Constants.CURRENT_YEAROFSTUDY_PREF_NAME;
import static com.job.darasastudent.util.Constants.CURRENT_YEAR_PREF_NAME;
import static com.job.darasastudent.util.Constants.STUDENTDETAILSCOL;


public class CurrentSetupActivity extends AppCompatActivity {

    @BindView(R.id.current_toolbar)
    Toolbar currentToolbar;
    @BindView(R.id.current_semester)
    TextInputLayout currentSemester;
    @BindView(R.id.current_year)
    TextInputLayout currentYear;
    @BindView(R.id.current_acadyear)
    TextInputLayout currentAcadyear;
    @BindView(R.id.current_btn)
    TextView currentBtn;

    private DoSnack doSnack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    SharedPreferences sharedPreferences;
    private AccountSetupViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_setup);
        ButterKnife.bind(this);

        setSupportActionBar(currentToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_back));

        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        doSnack = new DoSnack(this, CurrentSetupActivity.this);

        // View model
        AccountSetupViewModel.Factory factory = new AccountSetupViewModel.Factory(
                CurrentSetupActivity.this.getApplication(), mAuth, mFirestore);

        model = ViewModelProviders.of(CurrentSetupActivity.this, factory)
                .get(AccountSetupViewModel.class);

        //ui observer
        uiObserver();
    }

    @OnClick(R.id.current_btn)
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

        if (validate()) {

            final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));
            pDialog.setTitleText("Just a moment...");
            pDialog.setCancelable(true);
            pDialog.show();

            final String sem = currentSemester.getEditText().getText().toString();
            final String syr = currentYear.getEditText().getText().toString();
            final String ayr = currentAcadyear.getEditText().getText().toString();

            Map<String, Object> studMap = new HashMap<>();
            studMap.put("currentsemester", sem);
            studMap.put("currentyear", syr);
            studMap.put("yearofstudy", ayr);

            // Set the value of 'Users'
            DocumentReference usersRef = mFirestore.collection(STUDENTDETAILSCOL).document(mAuth.getCurrentUser().getUid());

            usersRef.update(studMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            pDialog.setCancelable(true);
                            pDialog.setTitleText("Saved Successfully");
                            pDialog.setContentText("You're now set");
                            pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();

                                    saveToSharedPrefs(sem,syr,ayr);

                                    sendToMain();

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pDialog.dismiss();
                    doSnack.errorPrompt("Oops...", e.getMessage());
                }
            });
        }
    }

    private void saveToSharedPrefs(String sem,String syr,String ayr ){
        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences(getApplicationContext().getPackageName(),MODE_PRIVATE).edit();

        sharedPreferencesEditor.putString( CURRENT_SEM_PREF_NAME, sem);
        sharedPreferencesEditor.putString( CURRENT_YEAR_PREF_NAME, syr);
        sharedPreferencesEditor.putString( CURRENT_YEAROFSTUDY_PREF_NAME, ayr);

        sharedPreferencesEditor.apply();
    }

    private boolean validate() {
        boolean valid = true;

        String sem = currentSemester.getEditText().getText().toString();
        String syr = currentYear.getEditText().getText().toString();
        String ayr = currentAcadyear.getEditText().getText().toString();


        if (sem.isEmpty()) {
            currentSemester.setError("enter semester");
            valid = false;
        } else {
            currentSemester.setError(null);
        }

        if (syr.isEmpty()) {
            currentYear.setError("enter study year");
            valid = false;
        } else {
            currentYear.setError(null);
        }

        if (ayr.isEmpty()) {
            currentAcadyear.setError("enter academic year");
            valid = false;
        } else {
            currentAcadyear.setError(null);
        }

        return valid;
    }

    private void sendToMain() {
        Intent mIntent = new Intent(this, MainActivity.class);
        startActivity(mIntent);
        finish();
    }

    private void uiObserver() {
        model.getLecUserMediatorLiveData().observe(this, new Observer<StudentDetails>() {
            @Override
            public void onChanged(@Nullable StudentDetails studUser) {
                if (studUser != null) {
                    currentSemester.getEditText().setText(studUser.getCurrentsemester());
                    currentYear.getEditText().setText(studUser.getCurrentyear());
                    currentAcadyear.getEditText().setText(String.valueOf(studUser.getYearofstudy()));
                }
            }
        });
    }
}
