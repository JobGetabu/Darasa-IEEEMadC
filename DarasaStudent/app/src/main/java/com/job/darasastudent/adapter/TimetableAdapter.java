package com.job.darasastudent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.job.darasastudent.R;
import com.job.darasastudent.model.LecTeachTime;
import com.job.darasastudent.util.LessonVH;
import com.leodroidcoder.genericadapter.GenericRecyclerViewAdapter;
import com.leodroidcoder.genericadapter.OnRecyclerItemClickListener;

/**
 * Created by Job on Tuesday : 11/20/2018.
 */
public class TimetableAdapter extends GenericRecyclerViewAdapter<LecTeachTime, OnRecyclerItemClickListener, LessonVH> {

    public TimetableAdapter(Context context, OnRecyclerItemClickListener listener) {
        super(context, listener);
    }

    @Override
    public LessonVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_lesson, parent, false);

        return new LessonVH(view);
    }



}
