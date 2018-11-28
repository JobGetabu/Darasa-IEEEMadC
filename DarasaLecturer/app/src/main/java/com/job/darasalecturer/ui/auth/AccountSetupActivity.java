package com.job.darasalecturer.ui.auth;

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

import static com.job.darasalecturer.util.Constants.FIRST_NAME_PREF_NAME;
import static com.job.darasalecturer.util.Constants.LAST_NAME_PREF_NAME;
import static com.job.darasalecturer.util.Constants.LECUSERCOL;

public class AccountSetupActivity extends AppCompatActivity {

    @BindView(R.id.setup_toolbar)
    Toolbar setupToolbar;
    @BindView(R.id.setup_firstname)
    TextInputLayout setupFirstname;
    @BindView(R.id.setup_lastname)
    TextInputLayout setupLastname;
    @BindView(R.id.setup_school)
    TextInputLayout setupSchool;
    @BindView(R.id.setup_department)
    TextInputLayout setupDepartment;
    @BindView(R.id.setup_btn)
    TextView setupBtn;

    private DoSnack doSnack;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private AccountSetupViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);
        ButterKnife.bind(this);

        setSupportActionBar(setupToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_back));

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        doSnack = new DoSnack(this, AccountSetupActivity.this);
        mSharedPreferences = getSharedPreferences(getApplicationContext().getPackageName(),MODE_PRIVATE);
        editor = getSharedPreferences(getApplicationContext().getPackageName(),MODE_PRIVATE).edit();

        // View model
        AccountSetupViewModel.Factory factory = new AccountSetupViewModel.Factory(
                AccountSetupActivity.this.getApplication(), mAuth, mFirestore);

        model = ViewModelProviders.of(AccountSetupActivity.this, factory)
                .get(AccountSetupViewModel.class);

        //ui observer
        uiObserver();
    }

    @OnClick(R.id.setup_btn)
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
            pDialog.setCancelable(false);
            pDialog.show();

            String fname = setupFirstname.getEditText().getText().toString();
            String lname = setupLastname.getEditText().getText().toString();
            String school = setupSchool.getEditText().getText().toString();
            String dept = setupDepartment.getEditText().getText().toString();

            Map<String, Object> lecMap = new HashMap<>();
            lecMap.put("firstname", fname);
            lecMap.put("lastname", lname);
            lecMap.put("school", school);
            lecMap.put("department", dept);

            setPrefs(fname, lname);

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

    private void sendToSetClass() {

        Intent aIntent = new Intent(this, AddClassActivity.class);
        startActivity(aIntent);
        finish();
    }

    private boolean validate() {
        boolean valid = true;

        String fname = setupFirstname.getEditText().getText().toString();
        String lname = setupLastname.getEditText().getText().toString();
        String school = setupSchool.getEditText().getText().toString();
        String dept = setupDepartment.getEditText().getText().toString();

        if (fname.isEmpty()) {
            setupFirstname.setError("enter name");
            valid = false;
        } else {
            setupFirstname.setError(null);
        }

        if (lname.isEmpty()) {
            setupLastname.setError("enter name");
            valid = false;
        } else {
            setupLastname.setError(null);
        }

        if (school.isEmpty()) {
            setupSchool.setError("enter school");
            valid = false;
        } else {
            setupSchool.setError(null);
        }

        if (dept.isEmpty()) {
            setupDepartment.setError("enter dept");
            valid = false;
        } else {
            setupDepartment.setError(null);
        }

        return valid;
    }

    private void uiObserver() {
        model.getLecUserMediatorLiveData().observe(this, new Observer<LecUser>() {
            @Override
            public void onChanged(@Nullable LecUser lecUser) {
                if (lecUser != null) {
                    setupFirstname.getEditText().setText(lecUser.getFirstname());
                    setupLastname.getEditText().setText(lecUser.getLastname());
                    setupSchool.getEditText().setText(lecUser.getSchool());
                    setupDepartment.getEditText().setText(lecUser.getDepartment());
                }
            }
        });
    }

    private void setPrefs(String fName,String lName){
        editor.putString(FIRST_NAME_PREF_NAME,fName);
        editor.putString(LAST_NAME_PREF_NAME,lName);
        editor.apply();
    }
}
