package com.job.darasalecturer.util;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.TextView;

import com.hbb20.GThumb;
import com.job.darasalecturer.R;
import com.job.darasalecturer.model.StudentDetails;
import com.job.darasalecturer.viewmodel.AddStudentViewModel;
import com.leodroidcoder.genericadapter.BaseViewHolder;
import com.leodroidcoder.genericadapter.OnRecyclerItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.refactor.library.SmoothCheckBox;

/**
 * Created by Job on Tuesday : 8/14/2018.
 */
public class StudentVH extends BaseViewHolder<StudentDetails, OnRecyclerItemClickListener> {

    private static final String TAG = "StudentVH";
    @BindView(R.id.attn_gthumb)
    GThumb attnGthumb;
    @BindView(R.id.attn_reg_no)
    TextView attnRegNo;
    @BindView(R.id.attn_stud_name)
    TextView attnStudName;
    @BindView(R.id.attn_check_box)
    SmoothCheckBox attnCheckBox;
    @BindView(R.id.attn_main)
    ConstraintLayout attnMain;

    private StudentDetails model;
    private AddStudentViewModel addStudentViewModel;

    public StudentVH(@NonNull View itemView, final AddStudentViewModel addStudentViewModel) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.addStudentViewModel = addStudentViewModel;
        //LayoutInflater.from(mContext).inflate(R.layout.single_attendance, null);

        attnCheckBox.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
                if (isChecked) {

                    addStudentViewModel.getStudentDetailsList().add(model);
                    addStudentViewModel.setStudListMediatorLiveData(addStudentViewModel.getStudentDetailsList());
                } else {

                    addStudentViewModel.getStudentDetailsList().remove(model);
                    addStudentViewModel.setStudListMediatorLiveData(addStudentViewModel.getStudentDetailsList());
                }
            }
        });
    }

    @Override
    public void onBind(StudentDetails item) {
        this.model = item;
        setUpUi(item);
    }

    public void setUpUi(StudentDetails model) {

        attnStudName.setText(model.getFirstname() + " " + model.getLastname());
        attnRegNo.setText(model.getRegnumber());
        attnGthumb.loadThumbForName(model.getPhotourl(), model.getFirstname(), model.getLastname());

    }
}
