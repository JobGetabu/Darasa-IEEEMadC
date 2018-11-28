package com.job.darasalecturer.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.button.MaterialButton;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.job.darasalecturer.R;
import com.job.darasalecturer.appexecutor.DefaultExecutorSupplier;
import com.job.darasalecturer.model.CourseYear;
import com.job.darasalecturer.model.LecTeachTime;
import com.job.darasalecturer.model.QRParser;
import com.job.darasalecturer.ui.AdvertClassActivity;
import com.job.darasalecturer.ui.ScannerActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.job.darasalecturer.ui.ScannerActivity.LECTEACHIDEXTRA;
import static com.job.darasalecturer.ui.ScannerActivity.QRPARSEREXTRA;
import static com.job.darasalecturer.ui.ScannerActivity.VENUEEXTRA;
import static com.job.darasalecturer.util.Constants.CURRENT_SEM_PREF_NAME;
import static com.job.darasalecturer.util.Constants.CURRENT_YEAR_PREF_NAME;
import static com.job.darasalecturer.util.Constants.LECTEACHCOL;
import static com.job.darasalecturer.util.Constants.LECTEACHCOURSESUBCOL;

/**
 * Created by Job on Monday : 8/6/2018.
 */
public class LessonViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.ls_qr_img)
    ImageView lsQrImg;
    @BindView(R.id.ls_chipgroup)
    ChipGroup lsChipgroup;
    @BindView(R.id.ls_unitcode)
    TextView lsUnitcode;
    @BindView(R.id.ls_unitname)
    TextView lsUnitname;
    @BindView(R.id.ls_time)
    TextView lsTime;
    @BindView(R.id.ls_btn)
    MaterialButton lsBtn;
    @BindView(R.id.ls_loc_img)
    ImageView lsLocImg;
    @BindView(R.id.ls_card)
    ConstraintLayout lsCard;
    @BindView(R.id.ls_venue)
    TextView lsVenue;

    private Context mContext;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private LecTeachTime lecTeachTime;

    private static final String TAG = "LessonVH";

    public LessonViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        //LayoutInflater.from(mContext).inflate(R.layout.single_lesson, null);

        lsBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setCancelable(false);
                builder.setTitle(R.string.generate_qr);
                builder.setCancelable(true);
                builder.setIcon(DoSnack.setDrawable(mContext, R.drawable.ic_qrcode));
                builder.setMessage(mContext.getString(R.string.this_create_qr_txt));
                builder.setPositiveButton(mContext.getString(R.string.create_qr), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendToQr();
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

    public void init(Context mContext, FirebaseFirestore mFirestore, FirebaseAuth mAuth, LecTeachTime lecTeachTime) {
        this.mContext = mContext;
        this.mFirestore = mFirestore;
        this.mAuth = mAuth;
        this.lecTeachTime = lecTeachTime;
    }

    @OnClick(R.id.ls_qr_img)
    public void onLsQrImgClicked() {
    }

    @OnClick(R.id.ls_btn)
    public void onLsBtnClicked() {

        QRParser qrParser = new QRParser();
        qrParser.setDate(Calendar.getInstance().getTime());
        qrParser.setClasstime(lecTeachTime.getTime());
        qrParser.setLecteachid(lecTeachTime.getLecteachid());
        qrParser.setCourses(lecTeachTime.getCourses());
        qrParser.setLecteachtimeid(lecTeachTime.getLecteachtimeid());
        qrParser.setUnitcode(lecTeachTime.getUnitcode());
        qrParser.setUnitname(lecTeachTime.getUnitname());
        getSemYearPref(qrParser);

        sendToAdvert(qrParser);
    }

    private void getSemYearPref(QRParser qrParser) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        // Check prefs and pass to qrparser
        qrParser.setSemester(sharedPreferences.getString(CURRENT_SEM_PREF_NAME, "0"));
        qrParser.setYear(sharedPreferences.getString(CURRENT_YEAR_PREF_NAME, "2000"));

    }

    @OnClick(R.id.ls_loc_img)
    public void onLsLocImgClicked() {
    }

    @OnClick(R.id.ls_card)
    public void onLsCardClicked() {
    }

    public void setUpUi(final LecTeachTime lecTeachTime) {

        //smoother experience...
        DefaultExecutorSupplier.getInstance().forMainThreadTasks()
                .execute(new Runnable() {
                    @Override
                    public void run() {

                        lsUnitcode.setText(lecTeachTime.getUnitcode());
                        lsUnitname.setText(lecTeachTime.getUnitname());
                        lsVenue.setText(lecTeachTime.getVenue());
                        lessonTime(lecTeachTime.getTime());
                        //locationViewer(lecTeachTime);
                        getCourses(lecTeachTime);
                    }
                });
    }

    private void locationViewer(LecTeachTime lecTeachTime) {

        //AppCompatResources.getDrawable(get,R.drawable.ic_location_on)

        if (lecTeachTime.getVenue().isEmpty()) {

            DrawableHelper
                    .withContext(mContext)
                    .withColor(R.color.greyish)
                    .withDrawable(R.drawable.ic_location_on)
                    .tint()
                    .applyTo(lsLocImg);
        } else {
            DrawableHelper
                    .withContext(mContext)
                    .withColor(R.color.darkbluish)
                    .withDrawable(R.drawable.ic_location_on)
                    .tint()
                    .applyTo(lsLocImg);
        }
    }

    private void getCourses(LecTeachTime lecTeachTime) {

        mFirestore.collection(LECTEACHCOL).document(lecTeachTime.getLecteachid()).collection(LECTEACHCOURSESUBCOL)
                .document("courses")
                .get()
                .addOnSuccessListener(DefaultExecutorSupplier.getInstance().forMainThreadTasks(),
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(final DocumentSnapshot docSnapshot) {

                                lsChipgroup.removeAllViews();
                                for (CourseYear courseYear : CoursesProvider.jsonWorker(docSnapshot.getData())) {

                                    addCourses(courseYear);
                                }
                            }
                        });
    }

    private ArrayList<CourseYear> jsonWorker(Map<String, Object> courseObject) {

        ArrayList<CourseYear> courseYears = new ArrayList<>();
        for (int i = 0; i < courseObject.size(); i++) {
            Map<String, Object> zero = (Map<String, Object>) courseObject.get(String.valueOf(i));
            String course = (String) zero.get("course");
            long yearofstudy = (long) zero.get("yearofstudy");

            CourseYear cc = new CourseYear(course, (int) yearofstudy);
            courseYears.add(cc);
            Log.d(TAG, "jsonWorker: " + String.valueOf(cc));
        }

        return courseYears;
    }

    private void addCourses(CourseYear course) {
        Chip chip = new Chip(mContext);
        chip.setChipText(course.getCourse());
        //chip.setCloseIconEnabled(true);
        //chip.setCloseIconResource(R.drawable.your_icon);
        //chip.setChipIconResource(R.drawable.your_icon);
        //chip.setChipBackgroundColorResource(R.color.red);
        chip.setTextAppearanceResource(R.style.ChipTextStyle);
        chip.setChipStartPadding(4f);
        chip.setChipEndPadding(4f);

        lsChipgroup.addView(chip);
    }

    private void lessonTime(Date timestamp) {
        //Timestamp timestamp = model.getTimestamp();
        if (timestamp != null) {

            Date date = timestamp;
            Calendar c = Calendar.getInstance();
            c.setTime(date);

            DateFormat dateFormat2 = new SimpleDateFormat("hh.mm aa");
            lsTime.setText(dateFormat2.format(date));
        }
    }

    private void sendToQr() {
        QRParser qrParser = new QRParser();
        qrParser.setDate(Calendar.getInstance().getTime());
        qrParser.setClasstime(lecTeachTime.getTime());
        qrParser.setLecteachid(lecTeachTime.getLecteachid());
        qrParser.setCourses(lecTeachTime.getCourses());
        qrParser.setLecteachtimeid(lecTeachTime.getLecteachtimeid());
        qrParser.setUnitcode(lecTeachTime.getUnitcode());
        qrParser.setUnitname(lecTeachTime.getUnitname());
        getSemYearPref(qrParser);

        Intent qrintent = new Intent(mContext, ScannerActivity.class);
        qrintent.putExtra(QRPARSEREXTRA, qrParser);
        qrintent.putExtra(VENUEEXTRA, lecTeachTime.getVenue());
        qrintent.putExtra(LECTEACHIDEXTRA, lecTeachTime.getLecteachid());
        mContext.startActivity(qrintent);
    }

    private void sendToAdvert(QRParser qrParser) {
        Intent qrintent = new Intent(mContext, AdvertClassActivity.class);
        qrintent.putExtra(QRPARSEREXTRA, qrParser);
        qrintent.putExtra(VENUEEXTRA, lecTeachTime.getVenue());
        qrintent.putExtra(LECTEACHIDEXTRA, lecTeachTime.getLecteachid());
        mContext.startActivity(qrintent);
    }
}
