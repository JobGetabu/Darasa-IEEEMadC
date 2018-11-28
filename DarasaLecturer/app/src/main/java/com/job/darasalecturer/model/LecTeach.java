package com.job.darasalecturer.model;

import android.support.annotation.Keep;

/**
 * Created by Job on Tuesday : 7/24/2018.
 */

@Keep
public class LecTeach {
    private String lecid;
    private String lecteachid;
    private String unitcode;
    private String unitname;
    private String semester;
    private String studyyear;
    private String academicyear;
    private Boolean combiner;
    private String school;
    private String department;

    //studyyear = 2018
    //academic year = 2018/19

    public LecTeach() {
    }


    public LecTeach(String lecid, String lecteachid, String unitcode, String unitname, String semester,
                    String studyyear, String academicyear, Boolean combiner, String school, String department) {
        this.lecid = lecid;
        this.lecteachid = lecteachid;
        this.unitcode = unitcode;
        this.unitname = unitname;
        this.semester = semester;
        this.studyyear = studyyear;
        this.academicyear = academicyear;
        this.combiner = combiner;
        this.school = school;
        this.department = department;
    }

    public String getLecid() {
        return lecid;
    }

    public void setLecid(String lecid) {
        this.lecid = lecid;
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

    public Boolean getCombiner() {
        return combiner;
    }

    public void setCombiner(Boolean combiner) {
        this.combiner = combiner;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAcademicyear() {
        return academicyear;
    }

    public void setAcademicyear(String academicyear) {
        this.academicyear = academicyear;
    }

    public String getLecteachid() {
        return lecteachid;
    }

    public void setLecteachid(String lecteachid) {
        this.lecteachid = lecteachid;
    }

    @Override
    public String toString() {
        return "LecTeach{" +
                "lecid='" + lecid + '\'' +
                ", unitcode='" + unitcode + '\'' +
                ", unitname='" + unitname + '\'' +
                ", semester='" + semester + '\'' +
                ", studyyear='" + studyyear + '\'' +
                ", academicyear='" + academicyear + '\'' +
                ", combiner=" + combiner +
                ", school='" + school + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
