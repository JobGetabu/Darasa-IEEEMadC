package com.job.darasalecturer.viewmodel;

import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.CountDownTimer;

/**
 * Created by Job on Tuesday : 8/7/2018.
 */
public class ScannerViewModel extends ViewModel {

    private MediatorLiveData<String> timeLiveData = new MediatorLiveData<>();

    private MediatorLiveData<String> passcodeLiveData = new MediatorLiveData<>();

    public ScannerViewModel() {
        timer();
    }

    public MediatorLiveData<String> getTimeLiveData() {
        return timeLiveData;
    }

    public void setTimeLiveData(String timeLiveData) {
        this.timeLiveData.setValue(timeLiveData);
    }

    public void reStartTimer(){
        timer();
    }

    public void endTimer(){
        timeLiveData.setValue("done!");
    }

    //shows the time for scanning QR
    // 45000 => 4.5minutes, 1,800,000
    private void timer(){
        new CountDownTimer(1800000, 1000) {

            public void onTick(long millisUntilFinished) {
                String timer = (toMinutes(millisUntilFinished) +" : " + toSec(millisUntilFinished));
                timeLiveData.setValue(timer);
            }

            public void onFinish() {
                String timer = "done!";
                timeLiveData.setValue(timer);
                //scanLoading.stop();
            }
        }.start();
    }


    public MediatorLiveData<String> getPasscodeLiveData() {
        return passcodeLiveData;
    }

    public void setPasscodeLiveData(String passcodeLiveData) {
        this.passcodeLiveData.setValue(passcodeLiveData);
    }

    private String toMinutes(long millisUntilFinished){
        long min =  (millisUntilFinished) / (1000 * 60);
        return String.valueOf(min);
    }

    private String toSec(long millisUntilFinished){
        long remainedSecs = millisUntilFinished / 1000;
        return String.valueOf((remainedSecs % 60));
    }
}
