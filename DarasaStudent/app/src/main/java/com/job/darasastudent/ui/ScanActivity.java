package com.job.darasastudent.ui;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.job.darasastudent.R;
import com.job.darasastudent.model.ClassScan;
import com.job.darasastudent.model.CourseYear;
import com.job.darasastudent.model.QRParser;
import com.job.darasastudent.model.StudentScanClass;
import com.job.darasastudent.scanview.CodeScannerView;
import com.job.darasastudent.util.DoSnack;
import com.job.darasastudent.viewmodel.ScanViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;
import io.nlopez.smartlocation.location.providers.LocationManagerProvider;
import io.nlopez.smartlocation.location.providers.MultiFallbackProvider;

import static com.job.darasastudent.util.Constants.COMPLETED_GIF_PREF_NAME;
import static com.job.darasastudent.util.Constants.COURSE_PREF_NAME;
import static com.job.darasastudent.util.Constants.CURRENT_YEAROFSTUDY_PREF_NAME;
import static com.job.darasastudent.util.Constants.DATE_SCAN_FORMAT;
import static com.job.darasastudent.util.Constants.SCAN_CLASSTIME_PREF_NAME;
import static com.job.darasastudent.util.Constants.SCAN_DATE_PREF_NAME;
import static com.job.darasastudent.util.Constants.SCAN_DAY_PREF_NAME;
import static com.job.darasastudent.util.Constants.SCAN_LECTEACHID_PREF_NAME;
import static com.job.darasastudent.util.Constants.STUDENTDETAILSCOL;
import static com.job.darasastudent.util.Constants.STUDENTSCANCLASSCOL;
import static com.job.darasastudent.util.Constants.STUDFNAME_PREF_NAME;
import static com.job.darasastudent.util.Constants.STUDLNAME_PREF_NAME;
import static com.job.darasastudent.util.Constants.STUDREG_PREF_NAME;

public class ScanActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    private static final int LOCATION_PERMISSION_ID = 1001;
    private static final int CAMERA_PERMISSION_ID = 1021;
    private static final String TAG = "ScanActivity";


    @BindView(R.id.scan_toolbar)
    Toolbar scanToolbar;
    @BindView(R.id.qrdecoderview)
    QRCodeReaderView qrCodeReaderView;
    @BindView(R.id.scanner_view)
    CodeScannerView scannerView;
    @BindView(R.id.scan_gif)
    View scanGifView;
    @BindView(R.id.gif_gotit_btn)
    MaterialButton gifGotitBtn;


    private Gson gson;
    private LocationGooglePlayServicesProvider provider;
    private Location mLocation;
    private int locationAlreadyStarted = 1;
    private SweetAlertDialog pDialogLoc;
    private SharedPreferences mSharedPreferences;

    private ScanViewModel model;
    private DoSnack doSnack;
    private List<ClassScan> mTodayScannedClasses;

    //firestore
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewfinder);
        ButterKnife.bind(this);



        mSharedPreferences = getSharedPreferences(getApplicationContext().getPackageName(),MODE_PRIVATE);
        // Check if we need to display our GIF
        if (!mSharedPreferences.getBoolean(
                COMPLETED_GIF_PREF_NAME, false)) {
            // The user hasn't seen the GIF yet, so show it
            scanGifView.setVisibility(View.VISIBLE);
        }

        setSupportActionBar(scanToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_back));


        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        gson = new Gson();
        doSnack = new DoSnack(this, ScanActivity.this);

        //database
        model = ViewModelProviders.of(this).get(ScanViewModel.class);


        /*
        SmartLocation.with(this).location().state().locationServicesEnabled();
        // Location permission not granted
        if (ContextCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ScanActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
            return;
        }
        // Check if the location services are enabled
        //checkLocationOn();
        */


        if (ContextCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ScanActivity.this, new String[]{Manifest.permission.CAMERA}, LOCATION_PERMISSION_ID);
            return;
        }

        qrCodeReaderView.setOnQRCodeReadListener(this);


        // Use this function to enable/disable decoding
        qrCodeReaderView.setQRDecodingEnabled(true);

        // Use this function to change the autofocus interval (default is 5 secs)
        qrCodeReaderView.setAutofocusInterval(2000L);

        // Use this function to enable/disable Torch
        qrCodeReaderView.setTorchEnabled(true);

        // Use this function to set front camera preview
        //qrCodeReaderView.setFrontCamera();

        // Use this function to set back camera preview
        qrCodeReaderView.setBackCamera();

        scannerView.setQRCodeReaderView(qrCodeReaderView);


    }


    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed in View
    @Override
    public void onQRCodeRead(String text, PointF[] points) {


        QRParser qrParser = new QRParser().gsonToQRParser(gson, text);

        final SweetAlertDialog pDialog = new SweetAlertDialog(ScanActivity.this, SweetAlertDialog.SUCCESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));

        if (qrParser == null) {
            Log.d(TAG, "onQRCodeRead: " + text);
            unauthScanLocation(pDialog);
            return;
        }


        if (model.getScanCount() == 1) {
            verifyCourseAndDetails(pDialog, qrParser);
            model.setScanCount(model.getScanCount() + 1);
        }

    }

    private void successScan(final SweetAlertDialog pDialog, QRParser qrParser) {
        pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));
        pDialog.setTitleText("Confirmed :" + qrParser.getUnitname() + " \n" + qrParser.getUnitcode() + "\n Location: proximity OFF");
        pDialog.setCancelable(false);
        pDialog.show();

        qrCodeReaderView.stopCamera();

        //debugDb();

        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();

                finish();
            }
        });
    }

    private void failScan(final SweetAlertDialog pDialog) {

        pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));
        pDialog.setTitleText("Failed ");
        pDialog.setContentText("PLEASE RESCAN !");
        pDialog.setCancelable(false);
        pDialog.show();

        qrCodeReaderView.stopCamera();

        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();

                finish();
            }
        });
    }

    private void failScanLocationFar(final SweetAlertDialog pDialog, QRParser qrParser) {

        pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));

        pDialog.setTitleText("Failed : Too Far !" + " \n" + "\nYou're not in class!" + "\n Location: proximity WIDE");
        pDialog.setCancelable(false);
        pDialog.show();

        qrCodeReaderView.stopCamera();

        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();

                finish();
            }
        });
    }

    private void unauthScanLocation(final SweetAlertDialog pDialog) {

        pDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));

        pDialog.setTitleText("Not Allowed !" + "\n\nScan code from" + "\n Darasa Lecturer App");
        pDialog.setCancelable(false);
        pDialog.show();

        qrCodeReaderView.stopCamera();

        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();

                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //register location change broadcast
        //registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    @Override
    protected void onResume() {
        qrCodeReaderView.startCamera();
        //register location change broadcast
        //registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        super.onResume();
    }

    @Override
    protected void onPause() {
        qrCodeReaderView.stopCamera();

        /*
        stopLocation();
        //unregister location change broadcast
        try {
            unregisterReceiver(mGpsSwitchStateReceiver);
        } catch (IllegalArgumentException e) {
        }
        */
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopLocation();
        /*
        //unregister location change broadcast
        try {
            unregisterReceiver(mGpsSwitchStateReceiver);
        } catch (IllegalArgumentException e) {
        }
        */

        super.onDestroy();
    }

    private void startLocation() {

       /* provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);

        SmartLocation smartLocation = new SmartLocation.Builder(this)
                .logging(true)
                .build();

        smartLocation.location(provider).start(this);*/

        //register location change broadcast
        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        pDialogLoc = new SweetAlertDialog(ScanActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialogLoc.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));
        pDialogLoc.setTitleText("Accessing Location" + "\n Just a moment...");
        pDialogLoc.setCancelable(true);
        pDialogLoc.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (mLocation != null) {

                } else {
                    Toast.makeText(ScanActivity.this, "Location not acquired", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        pDialogLoc.show();

        LocationManagerProvider locationManagerProvider = new LocationManagerProvider();

        LocationProvider fallbackProvider = new MultiFallbackProvider.Builder()
                .withProvider(locationManagerProvider).withGooglePlayServicesProvider().build();


        SmartLocation.with(this)
                .location()
                .config(LocationParams.NAVIGATION)
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {

                        Log.d(TAG, "onLocationUpdated: " + location);

                        mLocation = location;

                        if (mLocation != null) {
                            if (pDialogLoc.isShowing()) {
                                pDialogLoc.dismiss();
                            }
                        }
                    }
                });

    }

    private void checkLocationOn() {

        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(R.string.location);  // GPS not found
        builder.setMessage(R.string.permission_rationale_location); // Want to enable?
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    ScanActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                } else {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dd = builder.create();

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            dd.show();
            setUpLocationUi(false, scannerView.getmAutoFocusButton());

        } else {
            dd.dismiss();
            setUpLocationUi(true, scannerView.getmAutoFocusButton());

            if (locationAlreadyStarted == 1) {
                startLocation();
                locationAlreadyStarted++;
            }
        }
    }

    /**
     * Following broadcast receiver is to listen the Location button toggle state in Android.
     */
    private BroadcastReceiver mGpsSwitchStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                // Make an action or refresh an already managed state.
                checkLocationOn();
            }
        }
    };


    private void stopLocation() {
        SmartLocation.with(this).location().stop();
        SmartLocation.with(this).geocoding().stop();

    }

    private void setUpLocationUi(Boolean on_off, ImageView scanLocImg) {
        if (on_off) {

       /*     DrawableHelper
                    .withContext(this)
                    .withColor(R.color.darkbluish)
                    .withDrawable(R.drawable.ic_location_on)
                    .tint()
                    .applyTo(scanLocImg);*/

            scanLocImg.setImageResource(R.drawable.ic_loc_on);
        } else {

            scanLocImg.setImageResource(R.drawable.ic_loc_off);
        }
    }

    @OnClick(R.id.gif_gotit_btn)
    public void gifBtnOnclick() {

        // User has seen GIF, so mark our SharedPreferences
        // flag as completed so that we don't show our GIF
        // the next time the user launches the app.
        SharedPreferences.Editor sharedPreferencesEditor =
                getSharedPreferences(getApplicationContext().getPackageName(),MODE_PRIVATE).edit();
        sharedPreferencesEditor.putBoolean(
                COMPLETED_GIF_PREF_NAME, true);

        sharedPreferencesEditor.apply();

        scanGifView.setVisibility(View.GONE);
    }

    private boolean distanceInMeters(Location locHere, double lat, double lon) {

        Location locLec = new Location("");
        locLec.setLatitude(lat);
        locLec.setLongitude(lon);

        float distanceInMeters = locHere.distanceTo(locLec);
        Log.d(TAG, "distanceInMeters: " + distanceInMeters);

        if (distanceInMeters < 2) {
            Toast.makeText(this, "less than 2 m", Toast.LENGTH_SHORT).show();

        }
        if (distanceInMeters > 200 && distanceInMeters <= 400) {

            Toast.makeText(this, "2 - 4 m", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (distanceInMeters > 400 && distanceInMeters <= 600) {

            Toast.makeText(this, "4 - 6 m", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (distanceInMeters > 600 && distanceInMeters <= 1000) {

            Toast.makeText(this, "6 - 10 m", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (distanceInMeters > 1000 && distanceInMeters <= 2000) {

            Toast.makeText(this, "10 - 40 m", Toast.LENGTH_SHORT).show();
        } else {
            return false;
        }
        return false;
    }

    private void verifyCourseAndDetails(final SweetAlertDialog pDialog, final QRParser qrParser) {

        mFirestore.collection(STUDENTDETAILSCOL).document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //check course
                        boolean mycourse = false;

                        String course = documentSnapshot.getString("course");
                        String currentsemester = documentSnapshot.getString("currentsemester");
                        String currentyear = documentSnapshot.getString("currentyear");
                        String yearofstudy = documentSnapshot.getString("yearofstudy");

                        for (CourseYear cs: qrParser.getCourses()) {
                            if (course.equals(cs.getCourse()) && yearofstudy.equals(String.valueOf(cs.getYearofstudy()))) {

                                mycourse = true;
                                break;
                            }
                        }

                        if (!mycourse) {
                            failVerifyCourseAndDetails(pDialog, "Not Allowed \n this unit is not \nregistered in your course");
                            return;
                        }

                        if (!currentsemester.equals(qrParser.getSemester())) {
                            failVerifyCourseAndDetails(pDialog, "Not Allowed \n update your current semester \n this is for " + qrParser.getSemester());
                            return;
                        }

                        if (!currentyear.equals(qrParser.getYear())) {
                            failVerifyCourseAndDetails(pDialog, "Not Allowed \n update your current study year \n this is for " + qrParser.getYear());
                            return;
                        }

                        //verify time
                        //3hrs difference
                        if (!timeDifference(qrParser, pDialog, "Not Allowed \n class session expired \n contact Lecturer")) {
                            return;
                        }

                        //verify repeat
                        if (isRepeatScan(qrParser)) {
                            failVerifyCourseAndDetails(pDialog, "Not Allowed \n this class session \n has already been scanned");
                            return;
                        }

                        saveThisInFirestore(qrParser, pDialog);
                    }
                });
    }

    private void failVerifyCourseAndDetails(final SweetAlertDialog pDialog, String message) {
        pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));

        pDialog.setTitleText(message);
        pDialog.setCancelable(false);
        pDialog.show();

        qrCodeReaderView.stopCamera();

        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();

                finish();
            }
        });
    }

    private boolean timeDifference(QRParser qrParser, SweetAlertDialog pDialog, String message) {

        Date now = Calendar.getInstance().getTime();
        Date classTime = qrParser.getDate();

        long millseconds = now.getTime() - classTime.getTime();

        long seconds = (millseconds / 1000);

        //3 hrs == 10800 secs

        if (seconds > 10800 || seconds == 10800 || seconds < 0) {
            //fail the scan
            failVerifyCourseAndDetails(pDialog, message);

            return false;
        }

        return true;
    }

    private boolean isRepeatScan(QRParser qrParser) {

        Calendar c = Calendar.getInstance();
        String dd = DoSnack.theDay(c.get(Calendar.DAY_OF_WEEK));


        String day = mSharedPreferences.getString(SCAN_DAY_PREF_NAME, "");
        String lecteachid = mSharedPreferences.getString(SCAN_LECTEACHID_PREF_NAME, "");
        long date = mSharedPreferences.getLong(SCAN_DATE_PREF_NAME, 0L);
        long classtime = mSharedPreferences.getLong(SCAN_CLASSTIME_PREF_NAME, 0L);

        return classtime == qrParser.getClasstime().getTime() &&
                date == qrParser.getDate().getTime() &&
                lecteachid.equals(qrParser.getLecteachtimeid());

    }

    private void saveThisScanInPrefs(QRParser qrParser) {
        //save this class scan
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK);
        String dd = DoSnack.theDay(day);

        SharedPreferences.Editor sharedPreferencesEditor =
                getSharedPreferences(getApplicationContext().getPackageName(),MODE_PRIVATE).edit();

        sharedPreferencesEditor.putString(SCAN_LECTEACHID_PREF_NAME, qrParser.getLecteachtimeid());
        sharedPreferencesEditor.putString(SCAN_DAY_PREF_NAME, dd);
        sharedPreferencesEditor.putLong(SCAN_DATE_PREF_NAME, qrParser.getDate().getTime());
        sharedPreferencesEditor.putLong(SCAN_CLASSTIME_PREF_NAME, qrParser.getClasstime().getTime());

        sharedPreferencesEditor.apply();

    }

    private void saveThisInFirestore(final QRParser qrParser, final SweetAlertDialog pDialog) {

        pDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));
        pDialog.setTitleText("Recording attendance...");
        pDialog.setCancelable(true);
        pDialog.setCanceledOnTouchOutside(true);
        pDialog.show();

        pDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

                Toast.makeText(ScanActivity.this,
                        "Attendance Confirmed.. Changes will sync when you're online",
                        Toast.LENGTH_LONG).show();
                //doSnack.showShortSnackbar("Attendance Confirmed.. Changes will sync when you're online");

                dialogInterface.dismiss();
                finish();

            }
        });

        //register the class in the prefs
        saveThisScanInPrefs(qrParser);

        //get short date today
        Calendar c = Calendar.getInstance();
        DateFormat dateFormat2 = new SimpleDateFormat(DATE_SCAN_FORMAT);
        String today = dateFormat2.format(c.getTime());
        StudentScanClass scanClass = new StudentScanClass();
        String key = mFirestore.collection(STUDENTSCANCLASSCOL).document().getId();

        scanClass.setClasstime(qrParser.getClasstime());
        scanClass.setDate(qrParser.getDate());
        scanClass.setLecteachtimeid(qrParser.getLecteachtimeid());
        scanClass.setSemester(qrParser.getSemester());
        scanClass.setYear(qrParser.getYear());
        scanClass.setStudentid(mAuth.getCurrentUser().getUid());
        //web fields set
        scanClass.setCourse(mSharedPreferences.getString(COURSE_PREF_NAME,""));
        scanClass.setStudname(mSharedPreferences.getString(STUDFNAME_PREF_NAME,"")+
                " "+mSharedPreferences.getString(STUDLNAME_PREF_NAME,""));
        scanClass.setYearofstudy(mSharedPreferences.getString(CURRENT_YEAROFSTUDY_PREF_NAME,""));
        scanClass.setRegno(mSharedPreferences.getString(STUDREG_PREF_NAME,""));
        scanClass.setUnitcode(qrParser.getUnitcode());
        scanClass.setUnitname(qrParser.getUnitname());

        scanClass.setStudentscanid(key);
        scanClass.setQuerydate(today);

        //update the classes
        mFirestore.collection(STUDENTSCANCLASSCOL).document(key)
                .set(scanClass)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        successScan(pDialog, qrParser);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                doSnack.showShortSnackbar(e.getMessage());
                failScan(pDialog);

            }
        });

    }

    private void debugDb() {

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK);

        String dd = DoSnack.theDay(day);

       /* List<ClassScan> todayScannedClasses = model.getTodayScannedClasses(dd);
        //check
        if (todayScannedClasses != null) {
            for (ClassScan cs : todayScannedClasses) {

                Log.d(TAG, "debugDb: " + cs.toString());
            }
        }else {
            Log.d(TAG, "debugDb: query is empty");
        }*/

        //LiveData<List<ClassScan>> scannedClasses = model.getScannedClasses();

        model.getTodayScannedClasses(dd).observe(this, new Observer<List<ClassScan>>() {
            @Override
            public void onChanged(@Nullable List<ClassScan> classScans) {

                mTodayScannedClasses = classScans;

                Log.d(TAG, "onChanged: "+classScans.size());
                if (mTodayScannedClasses != null) {
                    for (ClassScan cs : mTodayScannedClasses) {

                        Log.d(TAG, "debugDb: " + cs.toString());
                    }
                }else {
                    Log.d(TAG, "debugDb: query is empty");
                }
            }
        });
    }

}
