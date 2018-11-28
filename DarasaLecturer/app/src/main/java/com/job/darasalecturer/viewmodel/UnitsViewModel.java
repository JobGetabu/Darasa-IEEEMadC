package com.job.darasalecturer.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.job.darasalecturer.model.LecTeach;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Job on Thursday : 11/8/2018.
 */
public class UnitsViewModel extends ViewModel {

    private MutableLiveData<Boolean> isChecked;
    private MutableLiveData<LecTeach> lecTeachMutableLiveData;
    private MutableLiveData<List<LecTeach>> lecTeachList;

    public UnitsViewModel() {
        isChecked = new MutableLiveData<>();
        lecTeachMutableLiveData = new MutableLiveData<>();
        lecTeachList = new MutableLiveData<>();
        setLecTeachList(new ArrayList<LecTeach>());
    }

    public MutableLiveData<Boolean> getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(Boolean isChecked) {
        this.isChecked.setValue(isChecked);
    }

    public MutableLiveData<LecTeach> getLecTeachMutableLiveData() {
        return lecTeachMutableLiveData;
    }

    public void setLecTeachMutableLiveData(LecTeach lecTeach) {
        this.lecTeachMutableLiveData.setValue(lecTeach);
    }

    public MutableLiveData<List<LecTeach>> getLecTeachList() {
        return lecTeachList;
    }

    public void setLecTeachList(List<LecTeach> lecTeachList) {
        this.lecTeachList.setValue(lecTeachList);
    }
}
