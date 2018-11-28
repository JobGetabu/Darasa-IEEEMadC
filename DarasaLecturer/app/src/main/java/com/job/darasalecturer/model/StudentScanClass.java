package com.job.darasalecturer.model;

import android.support.annotation.Keep;

import java.util.Date;

/**
 * Created by Job on Wednesday : 10/3/2018.
 */
@Keep
public class StudentScanClass {
    private String semester;
    private String year;
    private String studentid;
    private String lecteachtimeid;
    private Date classtime;
    private Date date;
    private String studentscanid;
    private String querydate;
    //req web fields
    private String studname;
    private String regno;
    private String unitname;
    private String unitcode;
    private String course;
    private String yearofstudy;

    public StudentScanClass() {
    }

    public StudentScanClass(String semester, String year, String studentid, String lecteachtimeid,
                            Date classtime, Date date, String studentscanid,String querydate) {
        this.semester = semester;
        this.year = year;
        this.studentid = studentid;
        this.lecteachtimeid = lecteachtimeid;
        this.classtime = classtime;
        this.date = date;
        this.studentscanid = studentscanid;
        this.querydate = querydate;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getStudentid() {
        return studentid;
    }

    public void setStudentid(String studentid) {
        this.studentid = studentid;
    }

    public String getLecteachtimeid() {
        return lecteachtimeid;
    }

    public void setLecteachtimeid(String lecteachtimeid) {
        this.lecteachtimeid = lecteachtimeid;
    }

    public Date getClasstime() {
        return classtime;
    }

    public void setClasstime(Date classtime) {
        this.classtime = classtime;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStudentscanid() {
        return studentscanid;
    }

    public void setStudentscanid(String studentscanid) {
        this.studentscanid = studentscanid;
    }

    public String getQuerydate() {
        return querydate;
    }

    public void setQuerydate(String querydate) {
        this.querydate = querydate;
    }

    public String getStudname() {
        return studname;
    }

    public void setStudname(String studname) {
        this.studname = studname;
    }

    public String getRegno() {
        return regno;
    }

    public void setRegno(String regno) {
        this.regno = regno;
    }

    public String getUnitname() {
        return unitname;
    }

    public void setUnitname(String unitname) {
        this.unitname = unitname;
    }

    public String getUnitcode() {
        return unitcode;
    }

    public void setUnitcode(String unitcode) {
        this.unitcode = unitcode;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getYearofstudy() {
        return yearofstudy;
    }

    public void setYearofstudy(String yearofstudy) {
        this.yearofstudy = yearofstudy;
    }
}