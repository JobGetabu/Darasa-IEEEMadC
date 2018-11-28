package com.job.darasalecturer.model;

/**
 * Created by Job on Saturday : 11/24/2018.
 */
public class StudentMessage {

    private String studFirstName;
    private String studSecondName;
    private String studentid;
    private String regNo;

    public StudentMessage() {
    }

    public StudentMessage(String studFirstName, String studSecondName, String studentid, String regNo) {
        this.studFirstName = studFirstName;
        this.studSecondName = studSecondName;
        this.studentid = studentid;
        this.regNo = regNo;
    }

    public String getStudFirstName() {
        return studFirstName;
    }

    public void setStudFirstName(String studFirstName) {
        this.studFirstName = studFirstName;
    }

    public String getStudSecondName() {
        return studSecondName;
    }

    public void setStudSecondName(String studSecondName) {
        this.studSecondName = studSecondName;
    }

    public String getStudentid() {
        return studentid;
    }

    public void setStudentid(String studentid) {
        this.studentid = studentid;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }
}
