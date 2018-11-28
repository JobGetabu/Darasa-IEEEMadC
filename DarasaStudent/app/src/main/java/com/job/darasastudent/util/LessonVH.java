package com.job.darasastudent.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.GThumb;
import com.job.darasastudent.R;
import com.job.darasastudent.appexecutor.DefaultExecutorSupplier;
import com.job.darasastudent.model.LecTeachTime;
import com.leodroidcoder.genericadapter.BaseViewHolder;
import com.leodroidcoder.genericadapter.OnRecyclerItemClickListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.job.darasastudent.util.Constants.LECUSERCOL;

/**
 * Created by Job on Tuesday : 11/20/2018.
 */
public class LessonVH extends BaseViewHolder<LecTeachTime, OnRecyclerItemClickListener> {

    @BindView(R.id.ls_unit_cd)
    TextView lsUnitcode;
    @BindView(R.id.ls_unit_nm)
    TextView lsUnitname;
    @BindView(R.id.ls_time)
    TextView lsTime;
    @BindView(R.id.ls_card)
    ConstraintLayout lsCard;
    @BindView(R.id.ls_venue)
    TextView lsVenue;
    @BindView(R.id.attn_gthumb)
    GThumb gThumb;
    @BindView(R.id.ls_lecname)
    TextView lsLecname;


    private Context mContext;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private LecTeachTime lecTeachTime;

    private static final String TAG = "LessonVH";

    public LessonVH(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
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

                        lsUnitcode.setText("Unit code: "+lecTeachTime.getUnitcode());
                        lsUnitname.setText("Unit :"+lecTeachTime.getUnitname());
                        lsVenue.setText("Venue : "+lecTeachTime.getVenue());
                        lessonTime(lecTeachTime.getTime());
                        gThumb.applyMultiColor();
                        gThumb.setBackgroundShape(GThumb.BACKGROUND_SHAPE.ROUND);
                        gThumb.loadThumbForName("", lecTeachTime.getUnitname());
                        //locationViewer(lecTeachTime);
                        setLecName(lecTeachTime.getLecid());
                    }
                });
    }

    private void setLecName(String lecid) {
        FirebaseFirestore.getInstance().collection(LECUSERCOL).document(lecid)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String firstname = documentSnapshot.getString("firstname");
                String lastname = documentSnapshot.getString("lastname");

                lsLecname.setText("lec: "+firstname+" "+lastname);
            }
        });
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

    @Override
    public void onBind(LecTeachTime item) {
        setUpUi(item);
    }
}
