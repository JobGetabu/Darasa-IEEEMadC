package com.job.darasastudent.datasource;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by Job on Saturday : 10/13/2018.
 */
public class DateConverter {
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
