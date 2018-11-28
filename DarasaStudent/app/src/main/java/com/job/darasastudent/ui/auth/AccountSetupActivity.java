package com.job.darasastudent.ui.auth;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.abdeveloper.library.MultiSelectDialog;
import com.abdeveloper.library.MultiSelectModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.job.darasastudent.R;
import com.job.darasastudent.model.StudentDetails;
import com.job.darasastudent.ui.MainActivity;
import com.job.darasastudent.util.AppStatus;
import com.job.darasastudent.util.DoSnack;
import com.job.darasastudent.viewmodel.AccountSetupViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.job.darasastudent.util.Constants.COURSE_PREF_NAME;
import static com.job.darasastudent.util.Constants.DKUTCOURSES;
import static com.job.darasastudent.util.Constants.STUDENTDETAILSCOL;
import static com.job.darasastudent.util.Constants.STUDFNAME_PREF_NAME;
import static com.job.darasastudent.util.Constants.STUDLNAME_PREF_NAME;
import static com.job.darasastudent.util.Constants.STUDREG_PREF_NAME;

//TODO: Check if document already exists,
//Restrict manipulation of course and registration number.

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
    @BindView(R.id.setup_regno)
    TextInputLayout setupRegno;
    @BindView(R.id.setup_course_btn)
    MaterialButton setupCourseBtn;
    @BindView(R.id.setup_course)
    TextInputLayout setupCourse;

    private static final String TAG = "acSetup";

    private DoSnack doSnack;

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

        // View model
        AccountSetupViewModel.Factory factory = new AccountSetupViewModel.Factory(
                AccountSetupActivity.this.getApplication(), mAuth, mFirestore);

        model = ViewModelProviders.of(AccountSetupActivity.this, factory)
                .get(AccountSetupViewModel.class);

        setupCourse.setVisibility(View.GONE);

        //ui observer
        uiObserver();
    }

    @OnClick(R.id.setup_btn)
    public void onViewSetupClicked() {

        if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {

            doSnack.showSnackbar(getString(R.string.your_offline), getString(R.string.retry), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onViewSetupClicked();
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

            final String fname = setupFirstname.getEditText().getText().toString();
            final String lname = setupLastname.getEditText().getText().toString();
            String school = setupSchool.getEditText().getText().toString();
            String dept = setupDepartment.getEditText().getText().toString();
            final String regno = setupRegno.getEditText().getText().toString().trim();
            final String course = setupCourse.getEditText().getText().toString().trim();

            Map<String, Object> studMap = new HashMap<>();
            studMap.put("firstname", fname);
            studMap.put("lastname", lname);
            studMap.put("school", school);
            studMap.put("department", dept);
            studMap.put("course", course);
            studMap.put("regnumber", regno.toUpperCase());


            //TODO: Check duplication of field regnumber

            //checkDuplicateReg(pDialog, studMap, regno, course);

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

                                    saveStudPref(fname,lname,course,regno.toUpperCase());
                                    sendToCurrentSetup();

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

    private void sendToMain() {
        Intent mIntent = new Intent(this, MainActivity.class);
        startActivity(mIntent);
        finish();
    }

    private void sendToCurrentSetup() {
        Intent cIntent = new Intent(this, CurrentSetupActivity.class);
        startActivity(cIntent);
        finish();
    }

    private boolean validate() {
        boolean valid = true;

        String fname = setupFirstname.getEditText().getText().toString();
        String lname = setupLastname.getEditText().getText().toString();
        String school = setupSchool.getEditText().getText().toString();
        String dept = setupDepartment.getEditText().getText().toString();
        String regno = setupRegno.getEditText().getText().toString();
        String course = setupCourse.getEditText().getText().toString();

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

        if (regno.isEmpty()) {
            setupRegno.setError("enter reg no");
            valid = false;
        } else {
            setupDepartment.setError(null);
        }

        if (course.isEmpty()) {
            setupCourse.setError("select course");
            valid = false;
        } else {
            setupCourse.setError(null);
        }

        return valid;
    }

    private void uiObserver() {
        model.getLecUserMediatorLiveData().observe(this, new Observer<StudentDetails>() {
            @Override
            public void onChanged(@Nullable StudentDetails studUser) {
                if (studUser != null) {
                    setupFirstname.getEditText().setText(studUser.getFirstname());
                    setupLastname.getEditText().setText(studUser.getLastname());
                    setupSchool.getEditText().setText(studUser.getSchool());
                    setupDepartment.getEditText().setText(studUser.getDepartment());
                    setupRegno.getEditText().setText(studUser.getDepartment());

                    if (studUser.getCourse() == null) {
                        setupCourse.setVisibility(View.GONE);
                    } else {
                        setupCourse.setVisibility(View.VISIBLE);
                        setupCourse.getEditText().setText(studUser.getCourse());
                    }
                }
            }
        });
    }

    @OnClick(R.id.setup_course_btn)
    public void onViewCourseClicked() {

        if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {

            doSnack.showSnackbar("You're offline", "Retry", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onViewSetupClicked();
                }
            });

            return;
        }

        mFirestore.collection(DKUTCOURSES).document("dkut")
                .get(Source.DEFAULT)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            Map<String, Object> mapdata = task.getResult().getData();

                            if (mapdata != null) {

                                //List of courses with Name and Id
                                ArrayList<MultiSelectModel> listOfCourses = new ArrayList<>();

                                int i = 1;
                                for (Map.Entry<String, Object> entry : mapdata.entrySet()) {
                                    //System.out.println(entry.getKey() + "/" + entry.getValue());

                                    listOfCourses.add(new MultiSelectModel(i, entry.getValue().toString()));
                                    i++;
                                }

                                promptCourseList(listOfCourses);
                            }
                        } else {
                            doSnack.showSnackbar(getString(R.string.your_offline), getString(R.string.retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    recreate();
                                }
                            });
                        }
                    }
                });
    }

    private void promptCourseList(ArrayList<MultiSelectModel> listOfCourses) {
        //MultiSelectModel
        MultiSelectDialog multiSelectDialog = new MultiSelectDialog()
                .title(getResources().getString(R.string.select_course)) //setting title for dialog
                .titleSize(20)
                .positiveText("Done")
                .negativeText("Cancel")
                .setMaxSelectionLimit(1)
                .setMinSelectionLimit(1) //you can set minimum checkbox selection limit (Optional)
                //.preSelectIDsList() //List of ids that you need to be selected
                .multiSelectList(listOfCourses) // the multi select model list with ids and name
                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                    @Override
                    public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {

                        setupCourse.setVisibility(View.VISIBLE);
                        setupCourse.getEditText().setText(selectedNames.get(0).trim());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Dialog cancelled");
                    }


                });
        multiSelectDialog.show(this.getSupportFragmentManager(), "multiSelectDialog");
    }

    private void saveStudPref(String fname, String lname, String course, String regno) {
        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences(getApplicationContext().getPackageName(),MODE_PRIVATE).edit();

        sharedPreferencesEditor.putString(COURSE_PREF_NAME, course);
        sharedPreferencesEditor.putString(STUDFNAME_PREF_NAME, fname);
        sharedPreferencesEditor.putString(STUDLNAME_PREF_NAME, lname);
        sharedPreferencesEditor.putString(STUDREG_PREF_NAME, regno);

        sharedPreferencesEditor.apply();
    }
}
