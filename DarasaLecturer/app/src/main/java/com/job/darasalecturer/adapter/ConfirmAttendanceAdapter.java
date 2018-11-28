package com.job.darasalecturer.adapter;

import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.GThumb;
import com.job.darasalecturer.R;
import com.job.darasalecturer.model.StudentDetails;
import com.job.darasalecturer.viewmodel.AddStudentViewModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.refactor.library.SmoothCheckBox;

/**
 * Created by Job on Friday : 11/2/2018.
 */
public class ConfirmAttendanceAdapter extends RecyclerView.Adapter<ConfirmAttendanceAdapter.StudViewHolder>{

    private List<StudentDetails> studentDetails;

    private LifecycleOwner mActivity;
    private FirebaseFirestore mFirestore;
    private AddStudentViewModel addStudentViewModel;

    public ConfirmAttendanceAdapter(List<StudentDetails> studentDetails, LifecycleOwner mActivity,
                                    FirebaseFirestore mFirestore, AddStudentViewModel addStudentViewModel) {
        this.studentDetails = studentDetails;
        this.mActivity = mActivity;
        this.mFirestore = mFirestore;
        this.addStudentViewModel = addStudentViewModel;
    }

    @NonNull
    @Override
    public StudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_attendance, parent, false);

        return new StudViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull StudViewHolder holder, int position) {

        StudentDetails model = studentDetails.get(position);
        holder.init(mActivity, mFirestore, model, addStudentViewModel);
        holder.setUpUi(model);
    }

    @Override
    public int getItemCount() {
        return studentDetails.size();
    }

    public class StudViewHolder extends RecyclerView.ViewHolder {

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

        private LifecycleOwner mActivity;
        private FirebaseFirestore mFirestore;
        private StudentDetails model;
        private AddStudentViewModel addStudentViewModel;

        public StudViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

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

        public void init(LifecycleOwner mActivity, FirebaseFirestore mFirestore, StudentDetails model, AddStudentViewModel addStudentViewModel) {
            this.mActivity = mActivity;
            this.mFirestore = mFirestore;
            this.model = model;
            this.addStudentViewModel = addStudentViewModel;
        }

        public void setUpUi(StudentDetails model) {

            attnStudName.setText(model.getFirstname() + " " + model.getLastname());
            attnRegNo.setText(model.getRegnumber());
            attnGthumb.loadThumbForName(model.getPhotourl(), model.getFirstname(), model.getLastname());

        }
    }

}
