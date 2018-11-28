package com.job.darasalecturer.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.button.MaterialButton;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.job.darasalecturer.BuildConfig;
import com.job.darasalecturer.R;
import com.job.darasalecturer.adapter.ScanStudentAdapter;
import com.job.darasalecturer.appexecutor.DefaultExecutorSupplier;
import com.job.darasalecturer.model.CourseYear;
import com.job.darasalecturer.model.QRParser;
import com.job.darasalecturer.model.StudentMessage;
import com.job.darasalecturer.service.TransactionWorker;
import com.job.darasalecturer.util.AppStatus;
import com.job.darasalecturer.util.DoSnack;
import com.job.darasalecturer.util.LessonMessage;
import com.leodroidcoder.genericadapter.OnRecyclerItemClickListener;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.job.darasalecturer.service.TransactionWorker.KEY_QR_LECTEACHTIMEID_ARG;
import static com.job.darasalecturer.service.TransactionWorker.KEY_QR_LECTTEACHID_ARG;
import static com.job.darasalecturer.service.TransactionWorker.KEY_QR_UNITCODE_ARG;
import static com.job.darasalecturer.service.TransactionWorker.KEY_QR_UNITNAME_ARG;
import static com.job.darasalecturer.ui.AddAttendanceActivity.ADDATTENDANCE_EXTRA;
import static com.job.darasalecturer.util.Constants.FIRST_NAME_PREF_NAME;
import static com.job.darasalecturer.util.Constants.LAST_NAME_PREF_NAME;

public class AdvertClassActivity extends AppCompatActivity implements OnMenuItemClickListener, OnRecyclerItemClickListener {

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
    @BindView(R.id.ad_unit_name)
    TextView adUnitName;
    @BindView(R.id.ad_unit_code)
    TextView adUnitCode;
    @BindView(R.id.ad_course_chipgrp)
    ChipGroup adCourseChipgrp;
    @BindView(R.id.ad_status_txt)
    TextView adStatusTxt;
    @BindView(R.id.ad_start_scan_btn)
    Button adStartScanBtn;
    @BindView(R.id.ad_start_scan_animation_view)
    LottieAnimationView adStartScanAnimationView;
    @BindView(R.id.ad_start_scan_bck)
    ConstraintLayout adStartScanMain;
    @BindView(R.id.ad_list_students)
    ConstraintLayout adListStudentsMain;
    @BindView(R.id.ad_network_bck)
    ConstraintLayout adNetworkMain;
    @BindView(R.id.ad_bck)
    ConstraintLayout adMain;
    @BindView(R.id.ad_network_retry)
    MaterialButton adNetworkRetry;
    @BindView(R.id.ad_fab)
    FloatingActionButton adFab;
    @BindView(R.id.ad_students_bubbles)
    ConstraintLayout adStudentsBubbles;
    @BindView(R.id.ad_stud_txt)
    TextView adStudTxt;
    @BindView(R.id.ad_stud_list)
    RecyclerView adStudList;
    @BindView(R.id.ad_pop_txt)
    TextView adPopText;
    @BindView(R.id.ad_pop_bck)
    ConstraintLayout adPopMain;


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

    private QRParser qrParser;
    private Gson gson;
    private SharedPreferences mSharedPreferences;
    private String venue;
    private String lecteachid;
    private ScanStudentAdapter scanStudentAdapter;
    private List<StudentMessage> studentMessages;


    //firebase
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    private ContextMenuDialogFragment mMenuDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advert_class);
        ButterKnife.bind(this);

        if (BuildConfig.VERSION_CODE < 21) {

            adFab.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_add_small)); //ic_add_small
        }

        //region Keep the screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //endregion

        initMenuFragment();

        //region INIT GLOBAL VARS
        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        //get qrparser
        qrParser = getIntent().getParcelableExtra(QRPARSEREXTRA);
        venue = getIntent().getStringExtra(VENUEEXTRA);
        lecteachid = getIntent().getStringExtra(LECTEACHIDEXTRA);
        gson = new Gson();
        setUpUi(qrParser);

        mSharedPreferences = getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);


        //endregion


        // Build the message that is going to be published. This contains the device owner and a UUID.
        String lecFirstName = mSharedPreferences.getString(FIRST_NAME_PREF_NAME, "");
        String lecSecondName = mSharedPreferences.getString(LAST_NAME_PREF_NAME, "");
        mPubMessage = LessonMessage.newNearbyMessage(DoSnack.getUUID(mSharedPreferences),
                lecFirstName, lecSecondName, qrParser, null);

        initMessageListener();

        loadList();
    }

    //region UI SETUP


    private void initUI() {
        adStartScanAnimationView.setVisibility(View.GONE);
        adMain.setBackgroundColor(DoSnack.setColor(this, R.color.scan_blue));
        adStartScanMain.setVisibility(View.VISIBLE);
        adCardTop.setVisibility(View.VISIBLE);

        adNetworkMain.setVisibility(View.GONE);
        adListStudentsMain.setVisibility(View.GONE);
        adStudentsBubbles.setVisibility(View.GONE);
        adPopMain.setVisibility(View.GONE);

        adStartScanBtn.setBackground(DoSnack.setDrawable(this, R.drawable.round_off_btn_bg));
        adStatusTxt.setText(R.string.start_scanning_for_students_txt);
        adStartScanBtn.setText(R.string.start_scan);
        adStartScanBtn.setTextSize(16);
        adStartScanBtn.setPadding(40, 40, 40, 40);

        STATE = "FRESH";
    }

    private void initStudentListUI() {
        adMain.setBackgroundColor(DoSnack.setColor(this, R.color.scan_blue));
        adStartScanMain.setVisibility(View.GONE);
        adNetworkMain.setVisibility(View.GONE);
        adStudentsBubbles.setVisibility(View.GONE);
        adPopMain.setVisibility(View.GONE);

        adCardTop.setVisibility(View.VISIBLE);
        adListStudentsMain.setVisibility(View.VISIBLE);
    }

    private void initNetworkLostUI() {
        adMain.setBackgroundColor(DoSnack.setColor(this, R.color.white));
        adStartScanMain.setVisibility(View.GONE);
        adStudentsBubbles.setVisibility(View.GONE);
        adListStudentsMain.setVisibility(View.GONE);
        adPopMain.setVisibility(View.GONE);

        adCardTop.setVisibility(View.VISIBLE);
        adCardTop.setCardBackgroundColor(DoSnack.setColor(this, R.color.contentDividerLine));
        adStatusTxt.setText(R.string.netlost_scanning_for_students_txt);
        adNetworkMain.setVisibility(View.VISIBLE);

        STATE = "STOPPED";
    }

    private void initScanningUI() {
        adMain.setBackgroundColor(DoSnack.setColor(this, R.color.scan_blue));

        adNetworkMain.setVisibility(View.GONE);
        adStudentsBubbles.setVisibility(View.GONE);
        adListStudentsMain.setVisibility(View.GONE);
        adPopMain.setVisibility(View.GONE);

        adStartScanMain.setVisibility(View.VISIBLE);
        adStartScanAnimationView.setVisibility(View.VISIBLE);

        adStartScanBtn.setBackground(DoSnack.setDrawable(this, R.drawable.round_btn_bg));
        adStartScanBtn.setText(R.string.scan_class);
        adStatusTxt.setText(R.string.scanning_for_students_txt);
        adStartScanBtn.setTextSize(16);
        adStartScanBtn.setPadding(20, 40, 20, 40);
        adStartScanBtn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        adStartScanBtn.setEnabled(false);

        DoSnack.showShortSnackbar(this, getString(R.string.network_scanning_for_students));

        STATE = "SCANNING";
    }

    private void initSuccessUI() {
        adMain.setBackgroundColor(DoSnack.setColor(this, R.color.scan_blue));
        adNetworkMain.setVisibility(View.GONE);
        adStudentsBubbles.setVisibility(View.GONE);
        adListStudentsMain.setVisibility(View.GONE);
        adStartScanMain.setVisibility(View.GONE);


        adCardTop.setVisibility(View.VISIBLE);
        adPopMain.setVisibility(View.VISIBLE);

        //this where we place logic for success
        //6 seconds
        new CountDownTimer(6000, 100) {

            public void onTick(long millisUntilFinished) {
                //ticking
                changeTextColor();
            }

            public void onFinish() {

                initStudentListUI();
                DoSnack.showShortSnackbar(AdvertClassActivity.this, getString(R.string.checking_for_students_txt));
            }
        }.start();
    }

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

    //endregion

    @OnClick(R.id.ad_start_scan_btn)
    public void onStartScanClicked() {
        initScanningUI();

        if (AppStatus.getInstance(this).isNetworkAvailable()) {
            Log.d(TAG, "onStartScanClicked: ");
        }

        if (AppStatus.getInstance(this).isOnline()) {
            subscribe();
            publish();

        } else {

            initNetworkLostUI();
            STATE = "STOPPED";
        }

    }

    private void setUpUi(QRParser qrParser) {

        adUnitName.setText(qrParser.getUnitname());
        adUnitCode.setText(qrParser.getUnitcode());
        adCourseChipgrp.removeAllViews();
        for (CourseYear s : qrParser.getCourses()) {
            addCourses(s);
        }

        initUI();
    }

    private void addCourses(CourseYear course) {
        Chip chip = new Chip(this);
        chip.setChipText(course.getCourse());
        //chip.setCloseIconEnabled(true);
        //chip.setCloseIconResource(R.drawable.your_icon);
        //chip.setChipIconResource(R.drawable.your_icon);
        //chip.setChipBackgroundColorResource(R.color.red);
        chip.setTextAppearanceResource(R.style.ChipTextStyle);
        chip.setChipStartPadding(4f);
        chip.setChipEndPadding(4f);

        adCourseChipgrp.addView(chip);
    }

    @OnClick(R.id.ad_network_retry)
    public void onNetRetryClicked() {
        adStartScanBtn.setEnabled(true);

        if (AppStatus.getInstance(this).isOnline()) {
            initScanningUI();
            subscribe();
            publish();

        } else {

            DoSnack.showShortSnackbar(this, getString(R.string.youre_offline));
        }
        //init();
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

        MenuObject createqr = new MenuObject("Create QR code");
        createqr.setResource(R.drawable.ic_qrcode_small);
        //createqr.setColor(R.color.colorAccent);

        MenuObject saveclass = new MenuObject("Save Class");
        saveclass.setResource(R.drawable.ic_save);
        //saveclass.setColor(R.color.colorAccent);

        MenuObject addstudent = new MenuObject("Add offline Students");
        //BitmapDrawable bd = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.icn_3));
        addstudent.setResource(R.drawable.ic_classroom);
        //addstudent.setColor(R.color.colorAccent);

        MenuObject stopscan = new MenuObject("Stop scanning");
        stopscan.setResource(R.drawable.ic_stopsign);
        //stopscan.setColor(R.color.colorAccent);

        //temporary test for soe layouts
        MenuObject aa = new MenuObject("Show list of students");
        aa.setResource(R.drawable.ic_menu);

        MenuObject bb = new MenuObject("Show offline");
        bb.setResource(R.drawable.ic_menu);

        MenuObject cc = new MenuObject("Show bubbles");
        cc.setResource(R.drawable.ic_menu);


        menuObjects.add(close);
        menuObjects.add(createqr);
        menuObjects.add(saveclass);
        menuObjects.add(addstudent);
        menuObjects.add(stopscan);


        menuObjects.add(aa);
        menuObjects.add(bb);
        menuObjects.add(cc);
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
            case 1: //create qr
                sendToQr();
                break;
            case 2: //save class
                saveAndEndClass();
                break;
            case 3: //add offline students

                toAddAttendanceActivity();
                Toast.makeText(this, "Saved the class", Toast.LENGTH_SHORT).show();
                silentSavingClass();

                break;
            case 4: //stop scanning
                STATE = "STOPPED";
                Nearby.getMessagesClient(this).unpublish(mPubMessage);
                Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
                adStartScanBtn.setEnabled(true);
                initUI();


                break;
            case 5: //test Show list of students
                initStudentListUI();
                break;
            case 6: //test Show offline
                initNetworkLostUI();
                break;
            case 7: //test Show bubbles
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

            if (STATE.equals("SCANNING")) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle(R.string.title_scan);
                builder.setCancelable(true);
                builder.setIcon(DoSnack.setDrawable(this, R.drawable.ic_classroom));
                builder.setMessage(getString(R.string.title_scan_txt));
                builder.setPositiveButton(getString(R.string.title_scan_quite), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }

            if (STATE.equals("STOPPED")) {

                //assuming some students scanned the classes
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle(R.string.title_scan);
                builder.setCancelable(true);
                builder.setIcon(DoSnack.setDrawable(this, R.drawable.ic_classroom));
                builder.setMessage(getString(R.string.title_scan_txt));
                builder.setPositiveButton(getString(R.string.title_scan_quite), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }

            if (STATE.equals("FRESH")) {

                finish();
            }
        }
    }

    //region SETTING UP NEARBY MECHANICS
    private void initMessageListener() {
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                // Called when a new message is found.
                //mNearbyDevicesArrayAdapter.add(DeviceMessage.fromNearbyMessage(message).getMessageBody());

                Toast.makeText(AdvertClassActivity.this, "new device " + message.toString(), Toast.LENGTH_SHORT).show();

                //make sure is student message
                LessonMessage lessonMessage = LessonMessage.fromNearbyMessage(message);
                if (lessonMessage.getQrParser() == null && lessonMessage.getStudentMessage() != null) {

                    studentMessages.add(lessonMessage.getStudentMessage());
                    scanStudentAdapter.clear();
                    scanStudentAdapter.setItems(studentMessages);
                    adStudTxt.setText(scanStudentAdapter.getItemCount() + " Students Found");
                    scanStudentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onLost(Message message) {
                // Called when a message is no longer detectable nearby.
                //mNearbyDevicesArrayAdapter.remove(DeviceMessage.fromNearbyMessage(message).getMessageBody());
                LessonMessage lessonMessage = LessonMessage.fromNearbyMessage(message);
                if (lessonMessage.getQrParser() == null && lessonMessage.getStudentMessage() != null) {

                    Log.d(TAG, "onLost: Student device lost");
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
                        adStatusTxt.setText(R.string.checking_for_students_txt);
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

        Intent qrintent = new Intent(this, ScannerActivity.class);
        qrintent.putExtra(QRPARSEREXTRA, qrParser);
        qrintent.putExtra(VENUEEXTRA, venue);
        qrintent.putExtra(LECTEACHIDEXTRA, lecteachid);
        startActivity(qrintent);
        finish();
    }

    //region SAVING THE CLASS

    private void saveAndEndClass() {


        final SweetAlertDialog pDialog;
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setCancelable(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setContentText("Saving class attendance");
        pDialog.show();

        scheduleTransactionWork();

        pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();
                finish();
            }
        });

    }

    private void scheduleTransactionWork() {

        // Create the Data object:
        Data myData = new Data.Builder()
                .putString(KEY_QR_LECTEACHTIMEID_ARG, qrParser.getLecteachtimeid())
                .putString(KEY_QR_LECTTEACHID_ARG, qrParser.getLecteachid())
                .putString(KEY_QR_UNITNAME_ARG, qrParser.getUnitname())
                .putString(KEY_QR_UNITCODE_ARG, qrParser.getUnitcode())
                .build();

        //set network required
        Constraints myConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // ...then create and enqueue a OneTimeWorkRequest that uses those arguments
        OneTimeWorkRequest transWork = new OneTimeWorkRequest.Builder(TransactionWorker.class)
                .setConstraints(myConstraints)
                .setInputData(myData)
                .build();

        WorkManager.getInstance()
                .enqueue(transWork);


    }

    private void silentSavingClass() {
        scheduleTransactionWork();
    }

    private void toAddAttendanceActivity() {
        Intent addAttendIntent = new Intent(this, AddAttendanceActivity.class);
        addAttendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        addAttendIntent.putExtra(ADDATTENDANCE_EXTRA, qrParser);
        startActivity(addAttendIntent);
        finish();
    }

    //endregion

    //region LISTING SCAN STUDENTS

    private void loadList() {

        LinearLayoutManager linearLayoutManager = new
                LinearLayoutManager(this.getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        adStudList.setLayoutManager(linearLayoutManager);
        adStudList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adStudList.setHasFixedSize(true);

        studentMessages = new ArrayList<>();
        scanStudentAdapter = new ScanStudentAdapter(this, this);
        adStudList.setAdapter(scanStudentAdapter);
    }

    @Override
    public void onItemClick(int position) {
        //no click action assigned
    }

    //endregion

}
