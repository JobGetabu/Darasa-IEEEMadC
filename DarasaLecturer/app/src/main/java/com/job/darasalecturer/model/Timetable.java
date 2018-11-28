package com.job.darasalecturer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Job on Wednesday : 11/21/2018.
 */
public class Timetable implements Parcelable {

    //Populates students timetables

    private String lecid;
    private String lecteachid;
    private String lecteachtimeid;
    private String timetableid;
    private String day;
    private Date time;
    private String semester;
    private String currentyear;
    private String yearofstudy;
    private String course;

    public Timetable() {
    }

    public Timetable(String lecid, String lecteachid, String lecteachtimeid,
                     String timetableid, String day, Date time, String semester, String currentyear, String yearofstudy, String course) {
        this.lecid = lecid;
        this.lecteachid = lecteachid;
        this.lecteachtimeid = lecteachtimeid;
        this.timetableid = timetableid;
        this.day = day;
        this.time = time;
        this.semester = semester;
        this.currentyear = currentyear;
        this.yearofstudy = yearofstudy;
        this.course = course;
    }

    public String getLecid() {
        return lecid;
    }

    public void setLecid(String lecid) {
        this.lecid = lecid;
    }

    public String getLecteachid() {
        return lecteachid;
    }

    public void setLecteachid(String lecteachid) {
        this.lecteachid = lecteachid;
    }

    public String getLecteachtimeid() {
        return lecteachtimeid;
    }

    public void setLecteachtimeid(String lecteachtimeid) {
        this.lecteachtimeid = lecteachtimeid;
    }

    public String getTimetableid() {
        return timetableid;
    }

    public void setTimetableid(String timetableid) {
        this.timetableid = timetableid;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getCurrentyear() {
        return currentyear;
    }

    public void setCurrentyear(String currentyear) {
        this.currentyear = currentyear;
    }

    public String getYearofstudy() {
        return yearofstudy;
    }

    public void setYearofstudy(String yearofstudy) {
        this.yearofstudy = yearofstudy;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    @Override
    public String toString() {
        return "Timetable{" +
                "lecid='" + lecid + '\'' +
                ", lecteachid='" + lecteachid + '\'' +
                ", lecteachtimeid='" + lecteachtimeid + '\'' +
                ", timetableid='" + timetableid + '\'' +
                ", day='" + day + '\'' +
                ", time=" + time +
                ", semester='" + semester + '\'' +
                ", currentyear='" + currentyear + '\'' +
                ", yearofstudy='" + yearofstudy + '\'' +
                ", course='" + course + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.lecid);
        dest.writeString(this.lecteachid);
        dest.writeString(this.lecteachtimeid);
        dest.writeString(this.timetableid);
        dest.writeString(this.day);
        dest.writeLong(this.time != null ? this.time.getTime() : -1);
        dest.writeString(this.semester);
        dest.writeString(this.currentyear);
        dest.writeString(this.yearofstudy);
        dest.writeString(this.course);
    }

    protected Timetable(Parcel in) {
        this.lecid = in.readString();
        this.lecteachid = in.readString();
        this.lecteachtimeid = in.readString();
        this.timetableid = in.readString();
        this.day = in.readString();
        long tmpTime = in.readLong();
        this.time = tmpTime == -1 ? null : new Date(tmpTime);
        this.semester = in.readString();
        this.currentyear = in.readString();
        this.yearofstudy = in.readString();
        this.course = in.readString();
    }

    public static final Creator<Timetable> CREATOR = new Creator<Timetable>() {
        @Override
        public Timetable createFromParcel(Parcel source) {
            return new Timetable(source);
        }

        @Override
        public Timetable[] newArray(int size) {
            return new Timetable[size];
        }
    };
}
