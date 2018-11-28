package com.job.darasastudent.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.button.MaterialButton;
import android.support.design.chip.Chip;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.job.darasastudent.R;
import com.job.darasastudent.appexecutor.DefaultExecutorSupplier;
import com.job.darasastudent.model.CourseYear;
import com.job.darasastudent.model.QRParser;
import com.job.darasastudent.model.StudentMessage;
import com.job.darasastudent.model.StudentScanClass;
import com.job.darasastudent.util.AppStatus;
import com.job.darasastudent.util.DoSnack;
import com.job.darasastudent.util.ImageProcessor;
import com.job.darasastudent.util.LessonMessage;
import com.job.darasastudent.viewmodel.ScanViewModel;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.job.darasastudent.util.Constants.COURSE_PREF_NAME;
import static com.job.darasastudent.util.Constants.CURRENT_SEM_PREF_NAME;
import static com.job.darasastudent.util.Constants.CURRENT_YEAROFSTUDY_PREF_NAME;
import static com.job.darasastudent.util.Constants.CURRENT_YEAR_PREF_NAME;
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

public class AdvertClassActivity extends AppCompatActivity implements OnMenuItemClickListener {

    //region CONSTANTS

    private static final String TAG = "Advert";
    public static final String QRPARSEREXTRA = "QRPARSEREXTRA";
    public static final String VENUEEXTRA = "VENUEEXTRA";
    public static final String LECTEACHIDEXTRA = "LECTEACHIDEXTRA";
    private static final int TTL_IN_SECONDS = 30 * 60; // Three minutes.

    /**
     * Sets the time in seconds for a published message or a subscription to live. Set to 30 min
     */
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS).build();

    //endregion

    //region binding views
    @BindView(R.id.ad_card_top)
    CardView adCardTop;
    @BindView(R.id.ad_status_txt)
    TextView adStatusTxt;
    @BindView(R.id.ad_start_scan_btn)
    Button adStartScanBtn;
    @BindView(R.id.ad_start_scan_animation_view)
    LottieAnimationView adStartScanAnimationView;
    @BindView(R.id.ad_start_scan_bck)
    ConstraintLayout adStartScanMain;
    @BindView(R.id.ad_pop_bck)
    ConstraintLayout adPopMain;
    @BindView(R.id.ad_network_bck)
    ConstraintLayout adNetworkMain;
    @BindView(R.id.ad_bck)
    ConstraintLayout adMain;
    @BindView(R.id.ad_network_retry)
    MaterialButton adNetworkRetry;
    @BindView(R.id.ad_fab)
    FloatingActionButton adFab;
    @BindView(R.id.user_info_image)
    CircleImageView userInfoImage;
    @BindView(R.id.user_info_username)
    TextView userInfoUsername;
    @BindView(R.id.user_info_time)
    TextView userInfoTime;
    @BindView(R.id.user_course_chip)
    Chip userCourseChip;
    @BindView(R.id.ad_pop_txt)
    TextView adPopText;


    //endregion

    //region NEARBY VARS

    /**
     * show state of the app
     * {@<code>SCANNING </code>}  app is still scanning
     * {@<code>FRESH </code>}     app is not scanned
     * {@<code>STOPPED </code>}   app is stopped scanning
     */
    private String STATE = "FRESH"; // SCANNING | FRESH | STOPPED | SUCCESS
    /**
     * The {@link Message} object used to broadcast information about the device to nearby devices.
     */
    private Message mPubMessage;

    /**
     * A {@link MessageListener} for processing messages from nearby devices.
     */
    private MessageListener mMessageListener;

    /**
     * Adapter for working with messages from nearby publishers.
     */
    //private ArrayAdapter<String> mNearbyDevicesArrayAdapter;


    //endregion

    private SharedPreferences mSharedPreferences;
    private ImageProcessor imageProcessor;
    private ScanViewModel model;


    //firebase
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    private ContextMenuDialogFragment mMenuDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advert_class);
        ButterKnife.bind(this);

        adFab.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.ic_add_small)); //ic_add_small

        //region Keep the screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //endregion

        initMenuFragment();

        //region INIT GLOBAL VARS
        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        //database
        model = ViewModelProviders.of(this).get(ScanViewModel.class);
        imageProcessor = new ImageProcessor(this);
        setUpUi();

        mSharedPreferences = getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);


        //endregion


        // Build the message that is going to be published. This contains the device owner and a UUID.
        String studFirstName = mSharedPreferences.getString(STUDFNAME_PREF_NAME, "");
        String studSecondName = mSharedPreferences.getString(STUDLNAME_PREF_NAME, "");
        String studentid = mAuth.getCurrentUser().getUid();
        String regNo = mSharedPreferences.getString(STUDREG_PREF_NAME, "");
        StudentMessage studentMessage = new StudentMessage(studFirstName, studSecondName, studentid, regNo);
        mPubMessage = LessonMessage.newNearbyMessage(DoSnack.getUUID(mSharedPreferences),
                null, null, null, studentMessage);

        initMessageListener();
    }

    //region UI SETUP


    private void initUI() {
        adStartScanAnimationView.setVisibility(View.GONE);
        adMain.setBackgroundColor(DoSnack.setColor(this, R.color.scan_blue));
        adStartScanMain.setVisibility(View.VISIBLE);
        adCardTop.setVisibility(View.VISIBLE);

        adNetworkMain.setVisibility(View.GONE);
        adPopMain.setVisibility(View.GONE);

        adStartScanBtn.setVisibility(View.VISIBLE);
        adStartScanBtn.setBackground(DoSnack.setDrawable(this, R.drawable.round_off_btn_bg));
        adStatusTxt.setText(R.string.start_scanning_for_lec_txt);
        adStartScanBtn.setText(R.string.start_scan);
        adStartScanBtn.setTextSize(16);
        adStartScanBtn.setPadding(40, 40, 40, 40);

        STATE = "FRESH";
    }

    private void initSuccessUI() {
        adMain.setBackgroundColor(DoSnack.setColor(this, R.color.scan_blue));
        adStartScanMain.setVisibility(View.GONE);
        adNetworkMain.setVisibility(View.GONE);

        adCardTop.setVisibility(View.VISIBLE);
        adPopMain.setVisibility(View.VISIBLE);

        //this where we place logic for success
        //4 seconds
        new CountDownTimer(4000, 100) {

            public void onTick(long millisUntilFinished) {
                //ticking
                changeTextColor();
            }

            public void onFinish() {

                initScanningUI();
                DoSnack.showShortSnackbar(AdvertClassActivity.this, getString(R.string.checking_for_lec_txt));
            }
        }.start();
    }

    private void initNetworkLostUI() {
        adMain.setBackgroundColor(DoSnack.setColor(this, R.color.white));
        adStartScanMain.setVisibility(View.GONE);

        adCardTop.setVisibility(View.VISIBLE);
        adCardTop.setCardBackgroundColor(DoSnack.setColor(this, R.color.contentDividerLine));
        adStatusTxt.setText(R.string.netlost_scanning_for_lec_txt);
        adNetworkMain.setVisibility(View.VISIBLE);

        STATE = "STOPPED";
    }

    private void initScanningUI() {
        adMain.setBackgroundColor(DoSnack.setColor(this, R.color.scan_blue));

        adNetworkMain.setVisibility(View.GONE);
        adPopMain.setVisibility(View.GONE);


        adStartScanMain.setVisibility(View.VISIBLE);
        adStartScanAnimationView.setVisibility(View.VISIBLE);

        adStartScanBtn.setBackground(DoSnack.setDrawable(this, R.drawable.round_btn_bg));
        adStartScanBtn.setText(R.string.scan_class);
        adStatusTxt.setText(R.string.scanning_for_lec_txt);
        adStartScanBtn.setTextSize(16);
        adStartScanBtn.setPadding(20, 40, 20, 40);
        adStartScanBtn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        adStartScanBtn.setEnabled(false);

        DoSnack.showShortSnackbar(this, getString(R.string.network_scanning_for_lec));

        STATE = "SCANNING";
    }

    //endregion

    @OnClick(R.id.ad_start_scan_btn)
    public void onStartScanClicked() {
        initScanningUI();

        if (AppStatus.getInstance(this).isOnline()) {
            subscribe();


        } else {

            initNetworkLostUI();
            STATE = "STOPPED";
        }

    }

    private void setUpUi() {

        String userId = mAuth.getCurrentUser().getUid();

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
                        setChip(course);
                        imageProcessor.setMyImage(userInfoImage, picurl);
                    }
                });

        initUI();
    }

    private void setChip(String course) {
        userCourseChip.setChipText(course);
        //chip.setCloseIconEnabled(true);
        //chip.setCloseIconResource(R.drawable.your_icon);
        //chip.setChipIconResource(R.drawable.your_icon);
        //chip.setChipBackgroundColorResource(R.color.red);
        userCourseChip.setTextAppearanceResource(R.style.ChipTextStyle);
        userCourseChip.setChipStartPadding(4f);
        userCourseChip.setChipEndPadding(4f);

    }

    @OnClick(R.id.ad_network_retry)
    public void onNetRetryClicked() {
        adStartScanBtn.setEnabled(true);

        if (AppStatus.getInstance(this).isOnline()) {
            initScanningUI();
            subscribe();

        } else {

            DoSnack.showShortSnackbar(this, getString(R.string.youre_offline));
        }
    }

    //region ContextMenuDialogFragment Menu

    private List<MenuObject> getMenuObjects() {
        // You can use any [resource, bitmap, drawable, color] as image:
        // item.setResource(...)
        // item.setBitmap(...)
        // item.setDrawable(...)
        // item.setColor(...)
        // You can set image ScaleType:
        // item.setScaleType(ScaleType.FIT_XY)
        // You can use any [resource, drawable, color] as background:
        // item.setBgResource(...)
        // item.setBgDrawable(...)
        // item.setBgColor(...)
        // You can use any [color] as text color:
        // item.setTextColor(...)
        // You can set any [color] as divider color:
        // item.setDividerColor(...)

        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject();
        close.setResource(R.drawable.ic_delete);
        //close.setColor(R.color.colorAccent);
        //close.setBgColor(R.color.white);

        MenuObject scanqr = new MenuObject("Scan Lec QR code");
        scanqr.setResource(R.drawable.ic_qrcode_small);

        MenuObject stopscan = new MenuObject("Stop scanning");
        stopscan.setResource(R.drawable.ic_stopsign);

        menuObjects.add(close);
        menuObjects.add(scanqr);
        menuObjects.add(stopscan);

        return menuObjects;
    }

    private void initMenuFragment() {
        DefaultExecutorSupplier.getInstance().forMainThreadTasks()
                .execute(new Runnable() {
                    @Override
                    public void run() {

                        MenuParams menuParams = new MenuParams();
                        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.tool_bar_height));
                        menuParams.setMenuObjects(getMenuObjects());
                        menuParams.setFitsSystemWindow(true);
                        menuParams.setClosableOutside(true);
                        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
                        mMenuDialogFragment.setItemClickListener(AdvertClassActivity.this);
                        //mMenuDialogFragment.setItemLongClickListener(this);
                    }
                });
    }

    @Override
    public void onMenuItemClick(View view, int position) {
        switch (position) {
            case 0: //close
                break;
            case 1: //scan qr
                sendToQr();
                break;
            case 2: //stop scanning
                STATE = "STOPPED";
                Nearby.getMessagesClient(this).unpublish(mPubMessage);
                Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
                adStartScanBtn.setEnabled(true);
                initUI();
                break;
        }
    }

    @OnClick(R.id.ad_fab)
    public void onFabClicked() {
        if (getSupportFragmentManager().findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
            mMenuDialogFragment.show(getSupportFragmentManager(), ContextMenuDialogFragment.TAG);
        }
    }

    //endregion

    @Override
    public void onBackPressed() {
        if (mMenuDialogFragment != null && mMenuDialogFragment.isAdded()) {
            mMenuDialogFragment.dismiss();
        } else {
            finish();
        }
    }

    //region SETTING UP NEARBY MECHANICS
    private void initMessageListener() {
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                // Called when a new message is found.
                //mNearbyDevicesArrayAdapter.add(DeviceMessage.fromNearbyMessage(message).getMessageBody());


                //check for lec message only
                LessonMessage lessonMessage = LessonMessage.fromNearbyMessage(message);
                if (lessonMessage != null) {
                    if (lessonMessage.getStudentMessage() == null) {
                        if (lessonMessage.getQrParser() != null) {
                            //this is is a Lec message
                            //try to confirm attendance

                            if (model.getScanCount() == 1) {

                                Log.d(TAG, "onFound: model.getScanCount()" + model.getScanCount());
                                Log.d(TAG, "onFound: " + lessonMessage.getQrParser().toString());

                                final SweetAlertDialog pDialog = new SweetAlertDialog(AdvertClassActivity.this, SweetAlertDialog.SUCCESS_TYPE);
                                pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));

                                QRParser qrParser = lessonMessage.getQrParser();

                                if (verifyCourseAndDetails(pDialog, qrParser)) {

                                    model.setScanCount(model.getScanCount() + 1);
                                }
                            }

                        }
                    }
                }
            }

            @Override
            public void onLost(Message message) {
                // Called when a message is no longer detectable nearby.
                //mNearbyDevicesArrayAdapter.remove(DeviceMessage.fromNearbyMessage(message).getMessageBody());
                LessonMessage lessonMessage = LessonMessage.fromNearbyMessage(message);
                if (lessonMessage != null) {
                    if (lessonMessage.getStudentMessage() == null) {
                        if (lessonMessage.getQrParser() != null) {
                            //this is is a Lec message
                            //try to confirm attendance
                            DoSnack.showShortSnackbar(AdvertClassActivity.this, "Lecturer closed the network");
                        }
                    }
                }
            }
        };
    }

    @Override
    public void onStop() {
        Nearby.getMessagesClient(this).unpublish(mPubMessage);
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);

        super.onStop();
    }

    private void subscribe() {
        Log.i(TAG, "Subscribing");
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "No longer subscribing");

                    }
                }).build();

        Nearby.getMessagesClient(this).subscribe(mMessageListener, options)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Log.i(TAG, "Subscribed successfully.");

                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {

                        Log.w(TAG, "onCanceled: cancelled");
                        Toast.makeText(AdvertClassActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: Could not subscribe, status =", e);
                    }
                });
    }

    /**
     * Publishes a message to nearby devices and updates the UI if the publication either fails or
     * TTLs.
     */
    private void publish() {
        Log.i(TAG, "Publishing");
        PublishOptions options = new PublishOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "No longer publishing");
                        initUI();
                    }
                }).build();

        Nearby.getMessagesClient(this).publish(mPubMessage, options)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Published successfully.");
                        //initScanningUI();
                        STATE = "SUCCESS";
                        initSuccessUI();
                        adStatusTxt.setText(R.string.checking_for_lec_txt);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: Publish error", e);
                        DoSnack.showShortSnackbar(AdvertClassActivity.this, e.getLocalizedMessage());
                        initNetworkLostUI();
                        STATE = "STOPPED";
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {

                        Log.w(TAG, "onCanceled: cancelled");
                        DoSnack.showShortSnackbar(AdvertClassActivity.this, "Publish Cancelled");
                    }
                });
    }

    //endregion

    private void sendToQr() {
        startActivity(new Intent(this, ScanActivity.class));
        finish();
    }

    //region SAVING THE CLASS

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

                Toast.makeText(AdvertClassActivity.this,
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
        scanClass.setCourse(mSharedPreferences.getString(COURSE_PREF_NAME, ""));
        scanClass.setStudname(mSharedPreferences.getString(STUDFNAME_PREF_NAME, "") +
                " " + mSharedPreferences.getString(STUDLNAME_PREF_NAME, ""));
        scanClass.setYearofstudy(mSharedPreferences.getString(CURRENT_YEAROFSTUDY_PREF_NAME, ""));
        scanClass.setRegno(mSharedPreferences.getString(STUDREG_PREF_NAME, ""));
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

                DoSnack.showShortSnackbar(AdvertClassActivity.this, e.getMessage());
                failScan(pDialog);

            }
        });

    }


    private void successScan(final SweetAlertDialog pDialog, QRParser qrParser) {
        pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));
        pDialog.setTitleText("Confirmed :" + qrParser.getUnitname() + " \n" + qrParser.getUnitcode() + "\n Location: proximity ON");
        pDialog.setCancelable(false);
        pDialog.show();

        STATE = "STOPPED";
        Nearby.getMessagesClient(this).unpublish(mPubMessage);
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
        adStartScanBtn.setEnabled(true);
        initUI();

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

        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();

                finish();
            }
        });
    }

    private void saveThisScanInPrefs(QRParser qrParser) {
        //save this class scan
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK);
        String dd = DoSnack.theDay(day);

        SharedPreferences.Editor sharedPreferencesEditor =
                getSharedPreferences(getApplicationContext().getPackageName(), MODE_PRIVATE).edit();

        sharedPreferencesEditor.putString(SCAN_LECTEACHID_PREF_NAME, qrParser.getLecteachtimeid());
        sharedPreferencesEditor.putString(SCAN_DAY_PREF_NAME, dd);
        sharedPreferencesEditor.putLong(SCAN_DATE_PREF_NAME, qrParser.getDate().getTime());
        sharedPreferencesEditor.putLong(SCAN_CLASSTIME_PREF_NAME, qrParser.getClasstime().getTime());

        sharedPreferencesEditor.apply();

    }

    private boolean verifyCourseAndDetails(final SweetAlertDialog pDialog, final QRParser qrParser) {

        //check course
        boolean mycourse = false;

        String course = mSharedPreferences.getString(COURSE_PREF_NAME, "");
        String currentsemester = mSharedPreferences.getString(CURRENT_SEM_PREF_NAME, "");
        String currentyear = mSharedPreferences.getString(CURRENT_YEAR_PREF_NAME, "");
        String yearofstudy = mSharedPreferences.getString(CURRENT_YEAROFSTUDY_PREF_NAME, "");

        for (CourseYear cs : qrParser.getCourses()) {
            if (course.equals(cs.getCourse()) && yearofstudy.equals(String.valueOf(cs.getYearofstudy()))) {

                mycourse = true;
                break;
            }
        }

        if (mycourse == false) {
            Log.d(TAG, "Not Allowed \n this unit is not \nregistered in your course");
            return false;
        }

        if (!currentsemester.equals(qrParser.getSemester())) {
            Log.d(TAG, "Not Allowed \n update your current semester \n this is for " + qrParser.getSemester());
            return false;
        }

        if (!currentyear.equals(qrParser.getYear())) {

            Log.d(TAG, "Not Allowed \n update your current study year \n this is for " + qrParser.getYear());
            return false;
        }

        //verify repeat
        if (isRepeatScan(qrParser)) {

            failVerifyCourseAndDetails(pDialog, "Not Allowed \n this class session \n has already been scanned");
            Log.d(TAG, "Not Allowed \n this class session \n has already been scanned");
            return false;
        }

        //publishing message only when student has successfully scanned class
        publish();
        saveThisInFirestore(qrParser, pDialog);

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

    private void failVerifyCourseAndDetails(final SweetAlertDialog pDialog, String message) {
        pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));

        pDialog.setTitleText(message);
        pDialog.setCancelable(false);
        pDialog.show();

        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();

                finish();
            }
        });
    }

    //endregion

    private void changeTextColor() {
        Random random = new Random();
        Integer integer = random.nextInt(4);
        switch (integer) {
            case 0:
                adPopText.setTextColor(DoSnack.setColor(this, R.color.scan_t3));
                break;
            case 1:
                adPopText.setTextColor(DoSnack.setColor(this, R.color.scan_t2));
                break;
            case 2:
                adPopText.setTextColor(DoSnack.setColor(this, R.color.scan_t1));
                break;
            case 3:
                adPopText.setTextColor(DoSnack.setColor(this, R.color.scan_t4));
                break;
            default:
                adPopText.setTextColor(DoSnack.setColor(this, R.color.white));
                break;
        }
    }
}
