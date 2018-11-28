package com.job.darasalecturer.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.abdeveloper.library.MultiSelectDialog;
import com.abdeveloper.library.MultiSelectModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.job.darasalecturer.R;
import com.job.darasalecturer.appexecutor.DefaultExecutorSupplier;
import com.job.darasalecturer.model.LecTeach;
import com.job.darasalecturer.ui.auth.AccountSetupActivity;
import com.job.darasalecturer.ui.auth.PasscodeActivity;
import com.job.darasalecturer.ui.auth.WelcomeActivity;
import com.job.darasalecturer.ui.newlesson.AddClassActivity;
import com.job.darasalecturer.util.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.job.darasalecturer.util.Constants.LECTEACHCOL;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "Settings";

    @BindView(R.id.settings_toolbar)
    Toolbar settingsToolbar;
    @BindView(R.id.settings_manage_account)
    MaterialButton settingsManageAccount;
    @BindView(R.id.settings_logout)
    MaterialButton settingsLogout;
    @BindView(R.id.settings_password)
    MaterialButton settingsPassword;
    @BindView(R.id.settings_manage_classes)
    MaterialButton settingsManageClasses;
    @BindView(R.id.settings_current)
    MaterialButton settingsCurrent;
    @BindView(R.id.settings_help)
    MaterialButton settingsHelp;
    @BindView(R.id.settings_faq)
    MaterialButton settingsFaq;
    @BindView(R.id.settings_remove_class)
    MaterialButton settingsRemoveClass;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private List<LecTeach> mLecTeachList;
    private ClassListFragment classListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(AppCompatResources.getDrawable(this,R.drawable.ic_back));

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        classListFragment = new ClassListFragment();
    }

    @OnClick(R.id.settings_manage_account)
    public void onSettingsManageAccountClicked() {
        Intent mIntent = new Intent(this, AccountSetupActivity.class);
        startActivity(mIntent);
        finish();
    }

    @OnClick(R.id.settings_logout)
    public void onSettingsLogoutClicked() {
        mAuth.signOut();

        sendToLogin();
    }

    @OnClick(R.id.settings_password)
    public void onPasswordClicked() {
        Intent intent = new Intent(SettingsActivity.this, PasscodeActivity.class);
        startActivity(intent);
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(this, WelcomeActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @OnClick(R.id.settings_manage_classes)
    public void onViewAddClassClicked() {
        Intent addclassIntent = new Intent(this, AddClassActivity.class);
        startActivity(addclassIntent);
        finish();
    }

    @OnClick(R.id.settings_current)
    public void onSettingsCurrentClicked() {
        Intent cIntent = new Intent(this, CurrentSetupActivity.class);
        startActivity(cIntent);
        finish();
    }

    @OnClick(R.id.settings_help)
    public void onSettingsHelpClicked() {
        Constants.createEmailIntent(this, R.string.dev_email, R.string.dev_subject,
                "Case Id \n" + mAuth.getCurrentUser().getUid() + " \n \n " + mAuth.getCurrentUser().getDisplayName() + "\n");
    }

    @OnClick(R.id.settings_faq)
    public void onSettingsFaqClicked() {
        Intent fIntent = new Intent(this, FaqActivity.class);
        startActivity(fIntent);

    }

    @OnClick(R.id.settings_remove_class)
    public void onRemoveClassViewClicked() {
        //loadClasses();

        classListFragment.show(getSupportFragmentManager(),ClassListFragment.TAG);
    }

    private void loadClasses() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));
        pDialog.setTitleText("Loading your classes...");
        pDialog.setCancelable(true);
        pDialog.show();

        mFirestore.collection(LECTEACHCOL)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {

                        pDialog.dismiss();
                        //List of courses with Name and Id
                        final ArrayList<MultiSelectModel> listOfTeachs = new ArrayList<>();

                        DefaultExecutorSupplier.getInstance().forMainThreadTasks()
                                .execute(new Runnable() {
                                    @Override
                                    public void run() {

                                        mLecTeachList = queryDocumentSnapshots.toObjects(LecTeach.class);

                                        int i=0;
                                        for (LecTeach lecTeach : mLecTeachList) {

                                            listOfTeachs.add(new MultiSelectModel(i, lecTeach.getUnitname()));
                                            i++;
                                        }

                                        promptCourseList(listOfTeachs);
                                    }
                                });
                    }
                });

    }

    private void promptCourseList(ArrayList<MultiSelectModel> listOfLecTeach) {
        //MultiSelectModel
        MultiSelectDialog multiSelectDialog = new MultiSelectDialog()
                .title(getResources().getString(R.string.select_class_delete)) //setting title for dialog
                .titleSize(20)
                .positiveText("Delete")
                .negativeText("Cancel")
                .setMinSelectionLimit(1) //you can set minimum checkbox selection limit (Optional)
                .setMaxSelectionLimit(1) //you can set max checkbox selection limit (Optional)
                //.preSelectIDsList() //List of ids that you need to be selected
                .multiSelectList(listOfLecTeach) // the multi select model list with ids and name
                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                    @Override
                    public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {

                        //will return list of selected IDS
                        for (int i = 0; i < selectedIds.size(); i++) {
                            //Toast.makeText(getContext(), "Selected Ids : " + selectedIds.get(i) + "\n" + "Selected Names : " + selectedNames.get(i) + "\n" + "DataString : " + dataString, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onSelected: "+mLecTeachList.get(i).getUnitname()+" == "+selectedNames.get(i));
                            Toast.makeText(SettingsActivity.this, "onSelected: "+mLecTeachList.get(i).getUnitname()+" == "+selectedNames.get(i), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Dialog cancelled");
                    }


                });
        multiSelectDialog.show(this.getSupportFragmentManager(), "multiSelectDialog_units");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // Sign in logic here.
                    finish();
                    sendToLogin();
                }
            }
        });
    }
}
