package com.job.darasastudent.ui;

import android.app.DatePickerDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.job.darasastudent.BuildConfig;
import com.job.darasastudent.R;
import com.job.darasastudent.model.StudentDetails;
import com.job.darasastudent.model.Timetable;
import com.job.darasastudent.ui.auth.AccountSetupActivity;
import com.job.darasastudent.ui.auth.CurrentSetupActivity;
import com.job.darasastudent.ui.auth.WelcomeActivity;
import com.job.darasastudent.util.Constants;
import com.job.darasastudent.util.DoSnack;
import com.job.darasastudent.util.ImageProcessor;
import com.job.darasastudent.util.LessonViewHolder;
import com.job.darasastudent.viewmodel.AccountSetupViewModel;
import com.robertlevonyan.views.customfloatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.fabric.sdk.android.Fabric;

import static com.job.darasastudent.util.Constants.COURSE_PREF_NAME;
import static com.job.darasastudent.util.Constants.CURRENT_YEAROFSTUDY_PREF_NAME;
import static com.job.darasastudent.util.Constants.STUDENTDETAILSCOL;
import static com.job.darasastudent.util.Constants.TIMETTCOL;

public class MainActivity extends AppCompatActivity {

    //this works < 19
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    private static final String TAG = "main";

    @BindView(R.id.main_fab)
    FloatingActionButton mainFab;
    @BindView(R.id.main_bar)
    BottomAppBar bar;
    @BindView(R.id.main_no_class)
    View noClassView;
    @BindView(R.id.main_toolbartitle)
    TextView mainToolbartitle;
    @BindView(R.id.main_list)
    RecyclerView mainList;
    @BindView(R.id.main_user_list_info)
    View mainUserListView;
    @BindView(R.id.user_info_image)
    CircleImageView userInfoImage;
    @BindView(R.id.user_info_username)
    TextView userInfoUsername;
    @BindView(R.id.user_info_time)
    TextView userInfoTime;
    @BindView(R.id.user_info_course)
    TextView userInfoCourse;
    @BindView(R.id.user_info_img_notification)
    ImageButton userInfoImgNotification;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userId;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirestoreRecyclerAdapter adapter;
    private Query mQuery = null;
    private SharedPreferences mSharedPreferences;

    private DoSnack doSnack;
    private ImageProcessor imageProcessor;
    private AccountSetupViewModel accountSetupViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initStrictMode();
        setSupportActionBar(bar);

        mainFab.setFabIcon(AppCompatResources.getDrawable(this,R.drawable.ic_add));
        //subtitle
        showDateOfClasses(Calendar.getInstance());

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        imageProcessor = new ImageProcessor(this);
        mSharedPreferences = getSharedPreferences(getApplicationContext().getPackageName(),MODE_PRIVATE);

        initList();

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // Sign in logic here.
                    sendToLogin();
                    finish();
                } else {

                    userId = mAuth.getCurrentUser().getUid();

                    classListQuery(Calendar.getInstance());

                    setUpUi(userId);

                    // View model
                    AccountSetupViewModel.Factory factory = new AccountSetupViewModel.Factory(
                            MainActivity.this.getApplication(), mAuth, mFirestore);
                    accountSetupViewModel = ViewModelProviders.of(MainActivity.this, factory)
                            .get(AccountSetupViewModel.class);

                    uiObserver();
                }
            }
        });

        doSnack = new DoSnack(this, MainActivity.this);


        //crashlytics
        if (!BuildConfig.DEBUG)
            Fabric.with(this, new Crashlytics());


        mainFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false);
                builder.setTitle(R.string.scan_lec_qr);
                builder.setCancelable(true);
                builder.setIcon(DoSnack.setDrawable(MainActivity.this, R.drawable.ic_qrcode_small));
                builder.setMessage(getString(R.string.this_scan_qr_txt));
                builder.setPositiveButton(getString(R.string.scan_qr), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(MainActivity.this, ScanActivity.class));
                    }
                });

                builder.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }
        });
    }

    private void uiObserver() {

        accountSetupViewModel.getLecUserMediatorLiveData().observe(this, new Observer<StudentDetails>() {
            @Override
            public void onChanged(@Nullable StudentDetails studUser) {
                if (studUser != null) {

                    if (studUser.getCourse() == null || mSharedPreferences.getString(COURSE_PREF_NAME,"").isEmpty()) {
                        DoSnack.showSnackbar(MainActivity.this,getString(R.string.add_ur_course), getString(R.string.add), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendToAccountSetup();
                            }
                        });
                    }
                    if (studUser.getCurrentsemester() == null || mSharedPreferences.getString(CURRENT_YEAROFSTUDY_PREF_NAME,"").isEmpty()) {
                        DoSnack.showSnackbar(MainActivity.this,getString(R.string.add_ur_sem), getString(R.string.add), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendToCurrentManage();
                            }
                        });
                    }
                } else {
                    //stud account is null
                    DoSnack.showSnackbar(MainActivity.this,getString(R.string.add_ur_info), getString(R.string.add), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mAuth.signOut();
                            sendToLogin();
                        }
                    });
                }
            }
        });
    }

    private void sendToAccountSetup() {
        Intent aIntent = new Intent(this, AccountSetupActivity.class);
        startActivity(aIntent);
    }

    private void sendToCurrentManage() {
        Intent aIntent = new Intent(this, CurrentSetupActivity.class);
        startActivity(aIntent);
    }

    private void setUpUi(String userId) {

        userInfoUsername.setText(mAuth.getCurrentUser().getDisplayName());

        mFirestore.collection(STUDENTDETAILSCOL).document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //Sem 2 - 2018/19
                        //Bs. BBIT
                        String sem = documentSnapshot.getString("currentsemester");
                        String academ = documentSnapshot.getString("yearofstudy");
                        String course = documentSnapshot.getString("course");
                        String picurl = documentSnapshot.getString("photourl");


                        userInfoTime.setText("Sem " + sem + " - Year " + academ);
                        userInfoCourse.setText(course);
                        imageProcessor.setMyImage(userInfoImage, picurl);
                    }
                });
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
                } else {
                    userId = mAuth.getCurrentUser().getUid();
                }
            }
        });

        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @OnClick(R.id.main_fab)
    public void onFabClicked() {
        //startActivity(new Intent(this, ScanActivity.class));
        startActivity(new Intent(this,AdvertClassActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_calender, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                BottomNavigationDrawerFragment bottomNavDrawerFragment = new BottomNavigationDrawerFragment();
                bottomNavDrawerFragment.show(getSupportFragmentManager(), BottomNavigationDrawerFragment.TAG);
                break;

            case R.id.menu_calender:

                final Calendar myCalendar = Calendar.getInstance();
                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String myFormat = "MM/dd/yy"; //In which you need put here
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);


                        showDateOfClasses(myCalendar);
                        classListQuery(myCalendar);
                        adapter.notifyDataSetChanged();
                    }
                };

                new DatePickerDialog(MainActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();

                break;
        }

        return true;
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, WelcomeActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void showDateOfClasses(Calendar c) {

        DateFormat dateFormat2 = new SimpleDateFormat("EEE, MMM d, ''yy"); //Wed, Jul 4, '18

        mainToolbartitle.setText("Showing: " + dateFormat2.format(c.getTime()));
        //mainToolbar.setSubtitleTextAppearance(this, R.style.ToolbarSubtitleAppearance);

        int day = c.get(Calendar.DAY_OF_WEEK);
        int daydate = c.get(Calendar.DAY_OF_MONTH);
    }

    private Query classListQuery(Calendar c) {

        //Due to change in structure we, have to incorporate complex query.

        final int day = c.get(Calendar.DAY_OF_WEEK);
        final String sDay = Constants.getDay(day);


        mFirestore.collection(TIMETTCOL)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {


                        mFirestore.collection(STUDENTDETAILSCOL).document(userId)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String currentsemester = documentSnapshot.getString("currentsemester");
                                        String currentyear = documentSnapshot.getString("currentyear");
                                        String course = documentSnapshot.getString("course");
                                        final String yearofstudy = documentSnapshot.getString("yearofstudy");

                                        // form query

                                        if (documentSnapshot.exists() && course != null) {

                                            mQuery = queryDocumentSnapshots
                                                    .getQuery()
                                                    .whereEqualTo("course", course)
                                                    .whereEqualTo("day", sDay)
                                                    .whereEqualTo("semester", currentsemester)
                                                    .whereEqualTo("currentyear", currentyear)
                                                    .whereEqualTo("yearofstudy", yearofstudy)
                                                    .orderBy("time", Query.Direction.ASCENDING);

                                            setUpList(mQuery);
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                // Show a snackbar on errors
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Update Settings or check connection.", Snackbar.LENGTH_INDEFINITE).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Update Settings or check connection.", Snackbar.LENGTH_INDEFINITE).show();
            }
        });

        return mQuery;
    }

    private void initList() {
        LinearLayoutManager linearLayoutManager = new
                LinearLayoutManager(this.getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mainList.setLayoutManager(linearLayoutManager);
        mainList.setHasFixedSize(true);
    }

    private void setUpList(Query mQuery) {

        initList();

        FirestoreRecyclerOptions<Timetable> options = new FirestoreRecyclerOptions.Builder<Timetable>()
                .setQuery(mQuery, Timetable.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Timetable, LessonViewHolder>(options) {

            @NonNull
            @Override
            public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_lesson, parent, false);

                return new LessonViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final LessonViewHolder holder, int position, @NonNull Timetable model) {

                holder.init(MainActivity.this, mFirestore, mAuth, model);
                holder.setUpUi(model);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();

                Log.d(TAG, "onError: ", e);
            }

            @Override
            public void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mainUserListView.setVisibility(View.GONE);
                    noClassView.setVisibility(View.VISIBLE);
                } else {
                    mainUserListView.setVisibility(View.VISIBLE);
                    noClassView.setVisibility(View.GONE);
                }
            }

        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        mainList.setAdapter(adapter);
    }

    /**
     * Handles user acceptance (or denial) of our permission request.
     */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, R.string.error_missing_permissions, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }

    private void initStrictMode() {

        if (BuildConfig.DEBUG) {

            StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder()
                    .detectCustomSlowCalls()
                    .detectNetwork()
                    .penaltyLog()
                    .build();

            StrictMode.setThreadPolicy(threadPolicy);
        }
    }
}
