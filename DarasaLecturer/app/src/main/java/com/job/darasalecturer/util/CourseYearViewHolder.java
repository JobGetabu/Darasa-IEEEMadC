package com.job.darasalecturer.util;

import android.support.design.card.MaterialCardView;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.job.darasalecturer.R;
import com.job.darasalecturer.model.CourseYear;
import com.leodroidcoder.genericadapter.BaseViewHolder;
import com.leodroidcoder.genericadapter.OnRecyclerItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Job on Sunday : 11/18/2018.
 */
public class CourseYearViewHolder extends BaseViewHolder<CourseYear, OnRecyclerItemClickListener> {

    @BindView(R.id.sinc_yc_course)
    TextInputLayout textInputCourse;
    @BindView(R.id.sinc_yc_txt)
    TextView yrText;
    @BindView(R.id.card_sinc_yc_id)
    MaterialCardView cardView;
    @BindView(R.id.sinc_yc_drop)
    ImageButton imageButton;

    OnRecyclerItemClickListener listener;


    public CourseYearViewHolder(View itemView, final OnRecyclerItemClickListener listener,ImageClickListener imageClickListener) {
        super(itemView, listener);
        ButterKnife.bind(this,itemView);
        this.listener = listener;
        this.imageClickListener = imageClickListener;
    }

    @OnClick({R.id.card_sinc_yc_id,R.id.sinc_yc_txt,R.id.sinc_yc_course,R.id.sinc_yc_drop})
    public void onCardViewClicked(){
        //listener.onItemClick(getAdapterPosition());
        imageClickListener.onImageClick(getAdapterPosition(),listener,imageButton);
    }

    @Override
    public void onBind(CourseYear item) {

        textInputCourse.getEditText().setText(item.getCourse());
        yrText.setText("Year of Study: "+ item.getYearofstudy());

    }

    private ImageClickListener imageClickListener;

    public interface ImageClickListener{
        void onImageClick(int position, OnRecyclerItemClickListener listener, View itemView);
    }
}
