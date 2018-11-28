package com.job.darasalecturer.ui.newlesson;


import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.job.darasalecturer.R;
import com.job.darasalecturer.model.LecTeach;
import com.job.darasalecturer.model.LecTeachTime;
import com.job.darasalecturer.viewmodel.AddClassViewModel;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class StepUnitFragment extends Fragment {


    @BindView(R.id.step_unit_unitname)
    TextInputLayout stepUnitUnitname;
    @BindView(R.id.step_unit_unitcode)
    TextInputLayout stepUnitUnitcode;
    @BindView(R.id.step_unit_time)
    TextInputLayout stepUnitTime;
    @BindView(R.id.step_unit_time_btn)
    MaterialButton stepUnitTimeBtn;
    @BindView(R.id.step_unit_back)
    TextView stepUnitBack;
    @BindView(R.id.step_unit_next)
    TextView stepUnitNext;
    @BindView(R.id.step_unit__day)
    Spinner stepUnitDay;
    Unbinder unbinder;

    private AddClassViewModel model;
    Calendar mcurrentTime = Calendar.getInstance();

    public StepUnitFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_step_unit, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        model = ViewModelProviders.of(getActivity()).get(AddClassViewModel.class);
        stepUnitTime.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @OnClick(R.id.step_unit_time_btn)
    public void onStepUnitTimeBtnClicked() {

        stepUnitTime.setVisibility(View.GONE);


        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                stepUnitTime.setVisibility(View.VISIBLE);
                stepUnitTime.getEditText().setText( selectedHour + ":" + selectedMinute);

                mcurrentTime.set(Calendar.YEAR, 2018);
                mcurrentTime.set(Calendar.MONTH, 1);
                mcurrentTime.set(Calendar.DAY_OF_MONTH, 1);
                mcurrentTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                mcurrentTime.set(Calendar.MINUTE, selectedMinute);
            }
        }, hour, minute, false);//Yes 24 hour time
        mTimePicker.setTitle("Select Lesson Time");
        mTimePicker.show();
    }

    @OnClick(R.id.step_unit_back)
    public void onStepUnitBackClicked() {
        model.setCurrentStep(0);
    }

    @OnClick(R.id.step_unit_next)
    public void onStepUnitNextClicked() {
        if (validate()) {

            model.setCurrentStep(2);

            String unitname = stepUnitUnitname.getEditText().getText().toString();
            String unitcode = stepUnitUnitcode.getEditText().getText().toString();
            String day = stepUnitDay.getSelectedItem().toString();
            Date time = mcurrentTime.getTime();

            model.getLecTeachMediatorLiveData().setValue(new LecTeach());
            model.getLecTeachTimeMediatorLiveData().setValue(new LecTeachTime());

            model.getLecTeachMediatorLiveData().getValue().setUnitname(unitname);
            model.getLecTeachMediatorLiveData().getValue().setUnitcode(unitcode);

            model.getLecTeachTimeMediatorLiveData().getValue().setUnitname(unitname);
            model.getLecTeachTimeMediatorLiveData().getValue().setUnitcode(unitcode);
            model.getLecTeachTimeMediatorLiveData().getValue().setDay(day);
            model.getLecTeachTimeMediatorLiveData().getValue().setTime(time);

        }
    }

    private boolean validate() {
        boolean valid = true;

        String unitname = stepUnitUnitname.getEditText().getText().toString();
        String unitcode = stepUnitUnitcode.getEditText().getText().toString();
        String time = stepUnitTime.getEditText().getText().toString();

        if (unitname.isEmpty()) {
            stepUnitUnitname.setError("enter unit");
            valid = false;
        } else {
            stepUnitUnitname.setError(null);
        }

        if (unitcode.isEmpty()) {
            stepUnitUnitcode.setError("enter unit code");
            valid = false;
        } else {
            stepUnitUnitcode.setError(null);
        }

        if (time.isEmpty()) {
            stepUnitTime.setVisibility(View.VISIBLE);
            stepUnitTime.setError("Select time");
            valid = false;
        } else {
            stepUnitTime.setError(null);
            stepUnitTime.setVisibility(View.GONE);
        }


        return valid;
    }
}
