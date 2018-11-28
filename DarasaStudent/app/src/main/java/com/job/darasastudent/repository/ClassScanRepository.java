package com.job.darasastudent.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import com.job.darasastudent.appexecutor.DefaultExecutorSupplier;
import com.job.darasastudent.datasource.ClassRoomDatabase;
import com.job.darasastudent.model.ClassScan;

import java.util.Date;
import java.util.List;

/**
 * Created by Job on Saturday : 10/13/2018.
 */
public class ClassScanRepository {

    public static final String TAG = "Repository";

    private ClassScanDao classScanDao;

    private static ClassScanRepository sInstance;
    private final ClassRoomDatabase mDatabase;

    private MediatorLiveData<List<ClassScan>> mScannedClasses ;
    private MediatorLiveData<List<ClassScan>> mDateScannedClasses ;
    private MediatorLiveData<List<ClassScan>> mTodayScannedClasses ;

    public ClassScanRepository(ClassRoomDatabase database) {

        mDatabase = database;
        classScanDao = mDatabase.classScanDao();

        //init mediators
        mScannedClasses = new MediatorLiveData<>();
        mDateScannedClasses = new MediatorLiveData<>();
        mTodayScannedClasses = new MediatorLiveData<>();

        DefaultExecutorSupplier.getInstance().forBackgroundTasks()
                .submit(new Runnable() {
                    @Override
                    public void run() {

                        mScannedClasses.addSource(classScanDao.getAllScannedClasses(), new Observer<List<ClassScan>>() {
                            @Override
                            public void onChanged(@Nullable List<ClassScan> classScans) {
                                if (classScans != null)
                                    mScannedClasses.postValue(classScans);
                            }
                        });

                    }
                });
    }

    public static ClassScanRepository getInstance(final ClassRoomDatabase database) {
        if (sInstance == null) {
            synchronized (ClassScanRepository.class) {
                if (sInstance == null) {
                    sInstance = new ClassScanRepository(database);
                }
            }
        }
        return sInstance;
    }


    /**
     * Get the list of products from the database and get notified when the data changes.
     */
    public LiveData<List<ClassScan>> getScannedClasses() {
        return mScannedClasses;
    }

    public void insert(final ClassScan classScan) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks()
                .submit(new Runnable() {
                    @Override
                    public void run() {
                        classScanDao.insert(classScan);
                    }
                });
    }


    public LiveData<List<ClassScan>> getDateScannedClasses(final Date today) {
        return classScanDao.getDateScannedClasses(today);
    }

    public LiveData<List<ClassScan>> getTodayScannedClasses(final String today) {
       return classScanDao.getTodayScannedClasses(today);
    }
}
