package com.job.darasalecturer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import java.util.ArrayList;
import java.util.Date;


/**
 * Created by Job on Tuesday : 7/24/2018.
 */

/*
* We will have only one course and selected year of study
* */
@Keep
public class LecTeachTime implements Parcelable {
    private String lecid;
    private String lecteachid;
    private String day;
    private Date time;
    private String unitcode;
    private String unitname;
    private String semester;
    private String studyyear;
    private String venue;
    private String lecteachtimeid;
    private ArrayList<CourseYear> courses;

    public LecTeachTime() {
    }

    public LecTeachTime(String lecid, String lecteachid, String day, Date time, String unitcode,
                        String unitname, String semester, String studyyear, String venue, String lecteachtimeid, ArrayList<CourseYear> courses) {
        this.lecid = lecid;
        this.lecteachid = lecteachid;
        this.day = day;
        this.time = time;
        this.unitcode = unitcode;
        this.unitname = unitname;
        this.semester = semester;
        this.studyyear = studyyear;
        this.venue = venue;
        this.lecteachtimeid = lecteachtimeid;
        this.courses = courses;
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

    public String getUnitcode() {
        return unitcode;
    }

    public void setUnitcode(String unitcode) {
        this.unitcode = unitcode;
    }

    public String getUnitname() {
        return unitname;
    }

    public void setUnitname(String unitname) {
        this.unitname = unitname;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getLecteachtimeid() {
        return lecteachtimeid;
    }

    public void setLecteachtimeid(String lecteachtimeid) {
        this.lecteachtimeid = lecteachtimeid;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getStudyyear() {
        return studyyear;
    }

    public void setStudyyear(String studyyear) {
        this.studyyear = studyyear;
    }

    @Override
    public String toString() {
        return "LecTeachTime{" +
                "lecid='" + lecid + '\'' +
                ", lecteachid='" + lecteachid + '\'' +
                ", day='" + day + '\'' +
                ", time=" + time +
                ", unitcode='" + unitcode + '\'' +
                ", unitname='" + unitname + '\'' +
                ", semester='" + semester + '\'' +
                ", studyyear='" + studyyear + '\'' +
                ", venue='" + venue + '\'' +
                ", lecteachtimeid='" + lecteachtimeid + '\'' +
                ", courses=" + courses +
                '}';
    }

    public ArrayList<CourseYear> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<CourseYear> courses) {
        this.courses = courses;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.lecid);
        dest.writeString(this.lecteachid);
        dest.writeString(this.day);
        dest.writeLong(this.time != null ? this.time.getTime() : -1);
        dest.writeString(this.unitcode);
        dest.writeString(this.unitname);
        dest.writeString(this.semester);
        dest.writeString(this.studyyear);
        dest.writeString(this.venue);
        dest.writeString(this.lecteachtimeid);
        dest.writeTypedList(this.courses);
    }

    protected LecTeachTime(Parcel in) {
        this.lecid = in.readString();
        this.lecteachid = in.readString();
        this.day = in.readString();
        long tmpTime = in.readLong();
        this.time = tmpTime == -1 ? null : new Date(tmpTime);
        this.unitcode = in.readString();
        this.unitname = in.readString();
        this.semester = in.readString();
        this.studyyear = in.readString();
        this.venue = in.readString();
        this.lecteachtimeid = in.readString();
        this.courses = in.createTypedArrayList(CourseYear.CREATOR);
    }

    public static final Creator<LecTeachTime> CREATOR = new Creator<LecTeachTime>() {
        @Override
        public LecTeachTime createFromParcel(Parcel source) {
            return new LecTeachTime(source);
        }

        @Override
        public LecTeachTime[] newArray(int size) {
            return new LecTeachTime[size];
        }
    };
}
