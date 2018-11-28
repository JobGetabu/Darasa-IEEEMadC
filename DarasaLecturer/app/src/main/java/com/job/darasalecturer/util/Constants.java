package com.job.darasalecturer.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

import com.job.darasalecturer.R;

/**
 * Created by Job on Friday : 8/3/2018.
 */
public class Constants {
    public static final String LECUSERCOL = "LecUser";
    public static final String LECTEACHCOL = "LecTeach";
    public static final String LECTEACHTIMECOL = "LecTeachTime";
    public static final String LECTEACHCOURSESUBCOL = "Courses";
    public static final String LECAUTHCOL = "LecAuth";
    public static final String DONECLASSES = "DoneClasses";
    public static final String STUDENTDETAILSCOL = "StudentDetails";
    public static final String STUDENTSCANCLASSCOL = "StudentScanClass";
    public static final String TIMETTCOL = "Timetable";
    public static final String DKUTCOURSES = "DkutCourses";
    public static final String DATE_SCAN_FORMAT = "yyyy-MM-dd";

    public static final String FRAG_MODEL_ARG = "FRAG_MODEL_ARG";

    //prefs
    public static final String CURRENT_SEM_PREF_NAME = "CURRENT_SEM_PREF_NAME";
    public static final String CURRENT_YEAR_PREF_NAME = "CURRENT_YEAR_PREF_NAME";
    public static final String FIRST_NAME_PREF_NAME = "FIRST_NAME_PREF_NAME";
    public static final String LAST_NAME_PREF_NAME = "LAST_NAME_PREF_NAME";
    public static final String KEY_UUID = "key_uuid";

    //app permissions
    public static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };

    /** Returns true if the app was granted all the permissions. Otherwise, returns false. */
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //for future  when we will access even finer location details
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.job.hacelaapp.util";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";
    public static final int LOCATION_INTERVAL = 10000;
    public static final int FASTEST_LOCATION_INTERVAL = 5000;

    public static void createEmailIntent(Context ctx,
                                           final @StringRes int int_email,
                                           final @StringRes int int_subject,
                                           final String message) {

        final String toEmail = ctx.getString(R.string.dev_email);
        final String subject = ctx.getString(R.string.dev_subject);

        Intent send = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode(toEmail) +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(message);
        Uri uri = Uri.parse(uriText);

        send.setData(uri);
        ctx.startActivity(Intent.createChooser(send, "Send Email to Darasa"));
    }

    public static String getDay(int day) {
        switch (day) {
            case 7:
                return "Saturday";

            case 6:
                return "Friday";

            case 5:
                return "Thursday";

            case 4:
                return "Wednesday";

            case 3:
                return "Tuesday";

            case 2:
                return "Monday";

            case 1:
                return "Sunday";

            default:
                return "";
        }
    }
}
