package com.job.darasalecturer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.job.darasalecturer.R;
import com.job.darasalecturer.model.CourseYear;
import com.job.darasalecturer.util.CourseYearViewHolder;
import com.leodroidcoder.genericadapter.GenericRecyclerViewAdapter;
import com.leodroidcoder.genericadapter.OnRecyclerItemClickListener;

/**
 * Created by Job on Friday : 11/16/2018.
 */
public class CourseYearAdapter extends GenericRecyclerViewAdapter<CourseYear, OnRecyclerItemClickListener, CourseYearViewHolder>  {

    private CourseYearViewHolder.ImageClickListener imageClickListener;

    public CourseYearAdapter(Context context, OnRecyclerItemClickListener listener,CourseYearViewHolder.ImageClickListener imageClickListener) {
        super(context, listener);

        this.imageClickListener = imageClickListener;
    }

    @Override
    public CourseYearViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_yearcourse, parent, false);
        return new CourseYearViewHolder(v,getListener(), imageClickListener);
    }

}
