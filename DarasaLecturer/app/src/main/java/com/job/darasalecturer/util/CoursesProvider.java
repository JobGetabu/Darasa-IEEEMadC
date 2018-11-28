package com.job.darasalecturer.util;

import com.job.darasalecturer.model.CourseYear;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Job on Monday : 11/19/2018.
 */
public class CoursesProvider {

    public static ArrayList<CourseYear> jsonWorker(Map<String, Object> courseObject) {

        ArrayList<CourseYear> courseYears = new ArrayList<>();
        for (int i = 0; i < courseObject.size(); i++) {
            Map<String, Object> zero = (Map<String, Object>) courseObject.get(String.valueOf(i));
            String course = (String) zero.get("course");
            long yearofstudy = (long) zero.get("yearofstudy");

            CourseYear cc = new CourseYear(course, (int) yearofstudy);
            courseYears.add(cc);
        }

        return courseYears;
    }
}
