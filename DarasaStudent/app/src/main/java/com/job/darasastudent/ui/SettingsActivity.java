package com.job.darasastudent.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.job.darasastudent.R;
import com.job.darasastudent.ui.auth.AccountSetupActivity;
import com.job.darasastudent.ui.auth.CurrentSetupActivity;
import com.job.darasastudent.ui.auth.WelcomeActivity;
import com.job.darasastudent.util.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.settings_toolbar)
    Toolbar settingsToolbar;

    @BindView(R.id.settings_manage_account)
    MaterialButton settingsManageAccount;
    @BindView(R.id.settings_logout)
    MaterialButton settingsLogout;
    @BindView(R.id.settings_current)
    MaterialButton settingsCurrent;
    @BindView(R.id.settings_help)
    MaterialButton settingsHelp;
    @BindView(R.id.settings_faq)
    MaterialButton settingsFaq;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_back));

        //firebase
        mAuth = FirebaseAuth.getInstance();

        //login credentials
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
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
        mGoogleSignInClient.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(this, WelcomeActivity.class);
        startActivity(loginIntent);
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
        Constants.createEmailIntent(this,R.string.dev_email,R.string.dev_subject,
                "Case Id \n"+mAuth.getCurrentUser().getUid()+" \n \n "+mAuth.getCurrentUser().getDisplayName()+"\n");
    }

    @OnClick(R.id.settings_faq)
    public void onSettingsFaqClicked() {
        Intent fIntent = new Intent(this, FaqActivity.class);
        startActivity(fIntent);

    }
}
