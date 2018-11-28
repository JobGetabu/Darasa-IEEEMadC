package com.job.darasastudent.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by Job on Saturday : 10/13/2018.
 */


@Keep
@Entity(tableName = "classScan", indices = {@Index(value = {"date_now"}, unique = true)})
public class ClassScan {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "teach_time")
    private String lecteachtimeid;

    @ColumnInfo(name = "class_time")
    private Date classtime;

    @ColumnInfo(name = "date_now")
    private Date date;

    @ColumnInfo(name = "day")
    private String day;

    public ClassScan() { }

    @Ignore
    public ClassScan(@NonNull String lecteachtimeid, @NonNull Date classtime, @NonNull Date date, String day) {
        this.lecteachtimeid = lecteachtimeid;
        this.classtime = classtime;
        this.date = date;
        this.day = day;
    }

    @NonNull
    public String getLecteachtimeid() {
        return lecteachtimeid;
    }

    public void setLecteachtimeid(@NonNull String lecteachtimeid) {
        this.lecteachtimeid = lecteachtimeid;
    }

    @NonNull
    public Date getClasstime() {
        return classtime;
    }

    public void setClasstime(@NonNull Date classtime) {
        this.classtime = classtime;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    public void setDate(@NonNull Date date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ClassScan{" +
                "id=" + id +
                ", lecteachtimeid='" + lecteachtimeid + '\'' +
                ", classtime=" + classtime +
                ", date=" + date +
                ", day='" + day + '\'' +
                '}';
    }
}
