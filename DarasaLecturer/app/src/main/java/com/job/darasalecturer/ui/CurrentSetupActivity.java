package com.job.darasalecturer.ui;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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
import com.job.darasalecturer.R;
import com.job.darasalecturer.model.LecUser;
import com.job.darasalecturer.ui.newlesson.AddClassActivity;
import com.job.darasalecturer.util.AppStatus;
import com.job.darasalecturer.util.DoSnack;
import com.job.darasalecturer.viewmodel.AccountSetupViewModel;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.job.darasalecturer.ui.AddAttendanceActivity.CURRENT_SEM_EXTRA;
import static com.job.darasalecturer.ui.AddAttendanceActivity.CURRENT_YR_EXTRA;
import static com.job.darasalecturer.util.Constants.CURRENT_SEM_PREF_NAME;
import static com.job.darasalecturer.util.Constants.CURRENT_YEAR_PREF_NAME;
import static com.job.darasalecturer.util.Constants.LECUSERCOL;


//TODO: Is this required for lec app ? ? ??
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
            String ayr = currentAcadyear.getEditText().getText().toString();

            Map<String, Object> lecMap = new HashMap<>();
            lecMap.put("currentsemester", sem);
            lecMap.put("currentyear", syr);
            lecMap.put("currentacademicyear", ayr);

            setSemYearPref(sem, syr);

            // Set the value of 'Users'
            DocumentReference usersRef = mFirestore.collection(LECUSERCOL).document(mAuth.getCurrentUser().getUid());

            usersRef.update(lecMap)
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

                                    sendToSetClass();

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

    private void setSemYearPref(String sem, String syr) {
        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences(getApplicationContext().getPackageName(),MODE_PRIVATE).edit();

        sharedPreferencesEditor.putString(CURRENT_SEM_PREF_NAME, sem);
        sharedPreferencesEditor.putString(CURRENT_YEAR_PREF_NAME, syr);

        sharedPreferencesEditor.apply();

        //if started activity was {AddAttendance}
        Intent resultIntent = new Intent();
        resultIntent.putExtra(CURRENT_SEM_EXTRA, sem);
        resultIntent.putExtra(CURRENT_YR_EXTRA, syr);
        setResult(Activity.RESULT_OK, resultIntent);
    }

    private void sendToSetClass() {

        Intent aIntent = new Intent(this, AddClassActivity.class);
        startActivity(aIntent);
        finish();
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

    private void uiObserver() {
        model.getLecUserMediatorLiveData().observe(this, new Observer<LecUser>() {
            @Override
            public void onChanged(@Nullable LecUser lecUser) {
                if (lecUser != null) {
                    currentSemester.getEditText().setText(lecUser.getCurrentsemester());
                    currentYear.getEditText().setText(lecUser.getCurrentyear());
                    currentAcadyear.getEditText().setText(lecUser.getCurrentacademicyear());
                }
            }
        });
    }
}
