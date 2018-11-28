package com.job.darasastudent.datasource;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.job.darasastudent.model.ClassScan;
import com.job.darasastudent.repository.ClassScanDao;

/**
 * Created by Job on Saturday : 10/13/2018.
 */

@Database(entities = {ClassScan.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class ClassRoomDatabase extends RoomDatabase{

    public abstract ClassScanDao classScanDao();

    private static volatile ClassRoomDatabase INSTANCE;

    public static ClassRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ClassRoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ClassRoomDatabase.class, "class_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
