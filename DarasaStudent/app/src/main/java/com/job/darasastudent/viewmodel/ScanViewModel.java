package com.job.darasastudent.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.job.darasastudent.DarasaStudent;
import com.job.darasastudent.model.ClassScan;
import com.job.darasastudent.repository.ClassScanRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by Job on Saturday : 10/13/2018.
 */
public class ScanViewModel extends AndroidViewModel {

    private int scanCount = 1;
    private ClassScanRepository repository;

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<List<ClassScan>> mObservableScans;

    public ScanViewModel(@NonNull Application application) {
        super(application);

        mObservableScans = new MediatorLiveData<>();
        repository = ((DarasaStudent) application).getRepository();

        // set by default null, until we get data from the database.
        mObservableScans.setValue(null);

        LiveData<List<ClassScan>> scannedClasses = ((DarasaStudent) application).getRepository()
                .getScannedClasses();

        // observe the changes of the class scans from the database and forward them
        mObservableScans.addSource(scannedClasses, new Observer<List<ClassScan>>() {
            @Override
            public void onChanged(@Nullable List<ClassScan> classScans) {
                mObservableScans.setValue(classScans);
            }
        });
    }

    /**
     * Expose the LiveData Products query so the UI can observe it.
     */
    public LiveData<List<ClassScan>> getScannedClasses() {
        return mObservableScans;
    }

    public void insert (ClassScan classScan){
        repository.insert(classScan);
    }


    public LiveData<List<ClassScan>> getDateScannedClasses(final Date today){
        return repository.getDateScannedClasses(today);
    }

    public LiveData<List<ClassScan>> getTodayScannedClasses(String today){
        return repository.getTodayScannedClasses(today);
    }

    public int getScanCount() {
        return scanCount;
    }

    public void setScanCount(int scanCount) {
        this.scanCount = scanCount;
    }
}
