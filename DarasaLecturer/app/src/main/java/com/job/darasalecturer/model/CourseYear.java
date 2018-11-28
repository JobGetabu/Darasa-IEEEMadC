package com.job.darasalecturer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Job on Friday : 11/16/2018.
 */
public class CourseYear implements Parcelable {
    private String course;
    private int yearofstudy;

    public CourseYear() {
    }

    public CourseYear(String course, int yearofstudy) {
        this.course = course;
        this.yearofstudy = yearofstudy;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public int getYearofstudy() {
        return yearofstudy;
    }

    public void setYearofstudy(int yearofstudy) {
        this.yearofstudy = yearofstudy;
    }

    @Override
    public String toString() {
        return "CourseYear{" +
                "course='" + course + '\'' +
                ", yearofstudy=" + yearofstudy +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.course);
        dest.writeInt(this.yearofstudy);
    }

    protected CourseYear(Parcel in) {
        this.course = in.readString();
        this.yearofstudy = in.readInt();
    }

    public static final Parcelable.Creator<CourseYear> CREATOR = new Parcelable.Creator<CourseYear>() {
        @Override
        public CourseYear createFromParcel(Parcel source) {
            return new CourseYear(source);
        }

        @Override
        public CourseYear[] newArray(int size) {
            return new CourseYear[size];
        }
    };
}