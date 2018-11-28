package com.job.darasalecturer.util;

import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.job.darasalecturer.R;
import com.job.darasalecturer.model.LecTeach;
import com.job.darasalecturer.viewmodel.UnitsViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.refactor.library.SmoothCheckBox;

/**
 * Created by Job on Thursday : 11/8/2018.
 */
public class UnitsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.sinc_unitname)
    TextView sincUnitname;
    @BindView(R.id.sinc_courses)
    TextView sincCourses;
    @BindView(R.id.sinc_checkbox)
    SmoothCheckBox sincCheckbox;

    private UnitsViewModel unitsViewModel;
    private LecTeach lecTeach;
    private LifecycleOwner mActivity;

    public UnitsViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        //LayoutInflater.from(mContext).inflate(R.layout.single_class, null);

        sincCheckbox.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {

                if (isChecked) {
                    unitsViewModel.setIsChecked(true);
                    unitsViewModel.setLecTeachMutableLiveData(lecTeach);

                    //handling lists
                    unitsViewModel.getLecTeachList().getValue().add(lecTeach);
                    unitsViewModel.setLecTeachList( unitsViewModel.getLecTeachList().getValue());

                } else {
                    unitsViewModel.setIsChecked(false);
                    unitsViewModel.setLecTeachMutableLiveData(null);

                    //handling lists
                    unitsViewModel.getLecTeachList().getValue().remove(lecTeach);
                    unitsViewModel.setLecTeachList( unitsViewModel.getLecTeachList().getValue());
                }
            }
        });
    }

    public void init(LifecycleOwner mActivity, LecTeach model, UnitsViewModel unitsViewModel) {
        this.mActivity = mActivity;
        this.lecTeach = model;
        this.unitsViewModel = unitsViewModel;
    }

    public void setUpUi(LecTeach model) {

        sincUnitname.setText(model.getUnitname());
        sincCourses.setText(model.getUnitcode());

    }
}
