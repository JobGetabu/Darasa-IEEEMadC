package com.job.darasastudent.repository;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.job.darasastudent.model.ClassScan;

import java.util.Date;
import java.util.List;

/**
 * Created by Job on Saturday : 10/13/2018.
 */

@Dao
public interface ClassScanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ClassScan classScan);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(ClassScan classScan);

    @Query("DELETE FROM classScan")
    void deleteAll();

    @Delete
    void deleteScanClasses(ClassScan... classScan);

    @Query("SELECT * from classScan ORDER BY id ASC")
    LiveData<List<ClassScan>> getAllScannedClasses();

    @Query("SELECT * FROM classscan WHERE date_now == :today")
    LiveData<List<ClassScan>> getDateScannedClasses(Date today);

    @Query("SELECT * FROM classscan WHERE day == :day")
    LiveData<List<ClassScan>> getTodayScannedClasses(String day);
}
