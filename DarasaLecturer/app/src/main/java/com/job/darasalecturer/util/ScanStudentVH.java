package com.job.darasalecturer.util;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.GThumb;
import com.job.darasalecturer.R;
import com.job.darasalecturer.model.StudentMessage;
import com.leodroidcoder.genericadapter.BaseViewHolder;
import com.leodroidcoder.genericadapter.OnRecyclerItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.job.darasalecturer.util.Constants.STUDENTDETAILSCOL;

/**
 * Created by Job on Tuesday : 8/14/2018.
 */
public class ScanStudentVH extends BaseViewHolder<StudentMessage, OnRecyclerItemClickListener> {

    private static final String TAG = "ScanStudentVH";
    @BindView(R.id.ad_ls_gthumb)
    GThumb adLsGthumb;
    @BindView(R.id.ad_ls_regno)
    TextView adLsRegno;
    @BindView(R.id.ad_ls_stud_name)
    TextView adLsStudName;
    @BindView(R.id.ad_list_single)
    ConstraintLayout adListSingle;


    private StudentMessage model;
    private FirebaseFirestore mFirestore;

    public ScanStudentVH(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        //LayoutInflater.from(mContext).inflate(R.layout.ad_list_single, null);
        mFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onBind(StudentMessage item) {

        setUpUi(item);
    }

    public void setUpUi(final StudentMessage model) {

        adLsStudName.setText(model.getStudFirstName() + " " + model.getStudSecondName());
        adLsRegno.setText(model.getRegNo());
        mFirestore.collection(STUDENTDETAILSCOL).document(model.getStudentid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        String pUrl = documentSnapshot.getString("photourl");
                        adLsGthumb.loadThumbForName(pUrl, model.getStudFirstName() , model.getStudSecondName());
                    }
                });
    }
}
