package com.job.darasalecturer.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.job.darasalecturer.model.QRParser;
import com.job.darasalecturer.model.StudentDetails;
import com.job.darasalecturer.model.StudentScanClass;
import com.job.darasalecturer.util.NotificationUtil;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.job.darasalecturer.util.Constants.DATE_SCAN_FORMAT;
import static com.job.darasalecturer.util.Constants.STUDENTSCANCLASSCOL;

/**
 * Created by Job on Tuesday : 11/6/2018.
 */
public class AddAttendanceWorker extends Worker {

    private static final String TAG = "AttendWorker";

    private FirebaseFirestore mFirestore;
    private Gson gson;
    private QRParser qrParser;

    // Define the parameter keys:

    public static final String KEY_STUD_LIST_ARG = "X";
    public static final String KEY_QR_OBJ_ARG = "Y";



    public AddAttendanceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        mFirestore = FirebaseFirestore.getInstance();
        gson = new Gson();
    }

    @NonNull
    @Override
    public Result doWork() {

        if (mFirestore == null) {
            Log.d(TAG, "doWork:  mFirestore is null");
            mFirestore = FirebaseFirestore.getInstance();
        }

        String s = getInputData().getString(KEY_STUD_LIST_ARG);
        String q = getInputData().getString(KEY_QR_OBJ_ARG);

        Type listOfStudObject = new TypeToken<List<StudentDetails>>(){}.getType();
        Type qrObject = new TypeToken<QRParser>(){}.getType();

        List<StudentDetails> studentDetailsList = gson.fromJson(s, listOfStudObject);
        qrParser = gson.fromJson(q,qrObject);


        if (studentDetailsList != null){

            doTheTransaction(new MyResultCallback() {
                @Override
                public Result onResultCallback(Result result) {
                    Log.d(TAG, "onResultCallback: => Worker.Result " + result.name());

                    return result;
                }
            }, studentDetailsList);
        }else {
            Log.d(TAG, "doWork: null list passed");
        }


        return Result.FAILURE;
    }

    private void doTheTransaction(final MyResultCallback resultCallback, final List<StudentDetails> studentDetailsList ) {

        //get short date today
        Calendar c = Calendar.getInstance();
        DateFormat dateFormat2 = new SimpleDateFormat(DATE_SCAN_FORMAT);
        String today = dateFormat2.format(c.getTime());


        // Get a new write batch
        WriteBatch batch = mFirestore.batch();

        for (StudentDetails s:studentDetailsList){

            StudentScanClass scanClass = new StudentScanClass();
            String key = mFirestore.collection(STUDENTSCANCLASSCOL).document().getId();

            scanClass.setClasstime(qrParser.getClasstime());
            scanClass.setDate(qrParser.getDate());
            scanClass.setLecteachtimeid(qrParser.getLecteachtimeid());
            scanClass.setSemester(qrParser.getSemester());
            scanClass.setYear(qrParser.getYear());
            scanClass.setStudentid(s.getStudentid());
            scanClass.setStudentscanid(key);
            scanClass.setQuerydate(today);
            //web fields set
            scanClass.setCourse(s.getCourse());
            scanClass.setStudname(s.getFirstname()+" "+s.getLastname());
            scanClass.setYearofstudy(s.getYearofstudy());
            scanClass.setRegno(s.getRegnumber());
            scanClass.setUnitcode(qrParser.getUnitcode());
            scanClass.setUnitname(qrParser.getUnitname());

            // Set the value of student-scan
            DocumentReference scanRef =  mFirestore.collection(STUDENTSCANCLASSCOL).document(key);

            batch.set(scanRef, scanClass);

            Log.d(TAG, "doTheTransaction: added"+scanClass.toString());
        }

        //update the classes
        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        String title = "Attendance Confirmed";
                        String message = studentDetailsList.size() +" students added";
                        new NotificationUtil().showStandardHeadsUpNotification(getApplicationContext(), title, message);
                        // Indicate success or failure with your return value:
                        resultCallback.onResultCallback(Result.SUCCESS);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Transaction failure.  => Worker", e);

                //do a reschedule of the transaction
                resultCallback.onResultCallback(Result.RETRY);
            }
        });


        resultCallback.onResultCallback(Result.SUCCESS);

        if (studentDetailsList.isEmpty()){
            resultCallback.onResultCallback(Result.FAILURE);
            Log.d(TAG, "doTheTransaction: empty list");
        }
    }
}
