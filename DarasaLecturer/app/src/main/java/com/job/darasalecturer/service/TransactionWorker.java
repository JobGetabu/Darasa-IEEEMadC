package com.job.darasalecturer.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.job.darasalecturer.util.NotificationUtil;

import java.util.HashMap;
import java.util.Map;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.job.darasalecturer.util.Constants.DONECLASSES;

/**
 * Created by Job on Friday : 10/19/2018.
 */
public class TransactionWorker extends Worker {

    private static final String TAG = "TransWorker";

    private FirebaseFirestore mFirestore;

    // Define the parameter keys:

    public static final String KEY_QR_LECTTEACHID_ARG = "W";
    public static final String KEY_QR_LECTEACHTIMEID_ARG = "X";
    public static final String KEY_QR_UNITNAME_ARG = "Y";
    public static final String KEY_QR_UNITCODE_ARG = "Z";


    public TransactionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        mFirestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {

        if (mFirestore == null) {
            Log.d(TAG, "doWork:  mFirestore is null");
            mFirestore = FirebaseFirestore.getInstance();
        }

        // Fetch the arguments (and specify default values):
        final String lecteachtimeid = getInputData().getString(KEY_QR_LECTEACHTIMEID_ARG);
        final String lecteachid = getInputData().getString(KEY_QR_LECTTEACHID_ARG);
        String unitname = getInputData().getString(KEY_QR_UNITNAME_ARG);
        String unitcode = getInputData().getString(KEY_QR_UNITCODE_ARG);


        doTheTransaction(new MyResultCallback() {
            @Override
            public Result onResultCallback(Result result) {
                Log.d(TAG, "onResultCallback: => Worker.Result " + result.name());

                return result;
            }
        }, lecteachid,lecteachtimeid,unitname, unitcode);


        //using failure cause it doesn't wait for our callback
        return Result.FAILURE;
    }

    private void doTheTransaction(final MyResultCallback resultCallback, final String lecteachid, final String lecteachtimeid, final String unitname, String unitcode) {

        Map<String, Object> doneClassMAp = new HashMap<>();
        doneClassMAp.put("lecteachtimeid",lecteachtimeid);
        doneClassMAp.put("lecteachid", lecteachid);
        doneClassMAp.put("unitname", unitname);
        doneClassMAp.put("unitcode", unitcode);

        mFirestore.collection(DONECLASSES).document(lecteachid)
                .update(doneClassMAp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        final DocumentReference cdDocRef = mFirestore.collection(DONECLASSES).document(lecteachid);
                        mFirestore.runTransaction(new Transaction.Function<Void>() {
                            @Override
                            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                DocumentSnapshot snapshot = transaction.get(cdDocRef);
                                double newPopulation = snapshot.getDouble("number") + 1;
                                transaction.update(cdDocRef, "number", newPopulation);

                                // Success
                                return null;
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Transaction success! => Worker");

                                //show notification

                                // Indicate success or failure with your return value:
                                resultCallback.onResultCallback(Result.SUCCESS);

                                String title = unitname;
                                String message = "Class has been successfully saved in the background";
                                new NotificationUtil().showStandardHeadsUpNotification(getApplicationContext(),title, message);


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Transaction failure.  => Worker", e);

                                //do a reschedule of the transaction
                                resultCallback.onResultCallback(Result.RETRY);
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Indicate success or failure with your return value:
                resultCallback.onResultCallback(Result.RETRY);
            }
        });
    }
}
