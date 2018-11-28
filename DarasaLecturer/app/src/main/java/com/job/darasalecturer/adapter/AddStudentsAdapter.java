package com.job.darasalecturer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.job.darasalecturer.R;
import com.job.darasalecturer.model.StudentDetails;
import com.job.darasalecturer.util.StudentVH;
import com.job.darasalecturer.viewmodel.AddStudentViewModel;
import com.leodroidcoder.genericadapter.GenericRecyclerViewAdapter;
import com.leodroidcoder.genericadapter.OnRecyclerItemClickListener;

/**
 * Created by Job on Sunday : 11/25/2018.
 */
public class AddStudentsAdapter extends GenericRecyclerViewAdapter<StudentDetails, OnRecyclerItemClickListener, StudentVH> {

    private AddStudentViewModel addStudentViewModel;

    public AddStudentsAdapter(Context context, OnRecyclerItemClickListener listener,AddStudentViewModel addStudentViewModel) {
        super(context, listener);
        this.addStudentViewModel = addStudentViewModel;
    }

    @Override
    public StudentVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_attendance, parent, false);

        return new StudentVH(view,addStudentViewModel);
    }

}
