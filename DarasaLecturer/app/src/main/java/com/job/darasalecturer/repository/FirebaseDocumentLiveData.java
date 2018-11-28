package com.job.darasalecturer.repository;

import android.arch.lifecycle.LiveData;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * Created by Job on Thursday : 4/12/2018.
 */
public class FirebaseDocumentLiveData extends LiveData<DocumentSnapshot> {

    //Improve codebase and add logic when it becomes available
    public static final String TAG = "FireDocLiveData";
    private DocumentReference documentReference;
    private final MyValueEventListener listener = new MyValueEventListener();
    private ListenerRegistration listenerRegistration;

    private boolean listenerRemovePending = false;
    private final Handler handler = new Handler();


    public FirebaseDocumentLiveData(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }


    private final Runnable removeListener = new Runnable() {
        @Override
        public void run() {
            //query.removeEventListener(listener);
            Log.d(TAG, "onInactive: removeListener");
            if (listenerRegistration != null) {
                listenerRegistration.remove();
            }
            listenerRemovePending = false;
        }
    };


    @Override
    protected void onActive() {
        super.onActive();

        Log.d(TAG, "onActive");

        if (listenerRemovePending){

            handler.removeCallbacks(removeListener);

        }else {
            if (listenerRegistration == null) {
                listenerRegistration = documentReference.addSnapshotListener(listener);
            }
        }

        listenerRemovePending = false;
    }

    @Override
    protected void onInactive() {
        super.onInactive();

        Log.d(TAG, "onInactive: onInactive()");


        // Listener removal is schedule on a two second delay
        // This is to save on counts against the quota or the bill of Firebase :)
        handler.postDelayed(removeListener, 2000);
        listenerRemovePending = true;

    }

    private class MyValueEventListener implements EventListener<DocumentSnapshot> {
        @Override
        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
            if (e != null){
                Log.e(TAG, "Can't listen to doc snapshots: " + documentSnapshot + ":::" + e.getMessage());
                return;
            }
            setValue(documentSnapshot);
        }
    }
}