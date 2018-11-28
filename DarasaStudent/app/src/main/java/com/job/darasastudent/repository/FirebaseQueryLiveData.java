package com.job.darasastudent.repository;

import android.arch.lifecycle.LiveData;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Created by Job on Thursday : 5/31/2018.
 */
public class FirebaseQueryLiveData  extends LiveData<QuerySnapshot> {

    //Improve codebase and add logic when it becomes available
    public static final String TAG = "FireDocLiveData";
    private Query query;
    private final MyValueEventListener listener = new MyValueEventListener();
    private ListenerRegistration listenerRegistration;

    private boolean listenerRemovePending = false;
    private final Handler handler = new Handler();


    public FirebaseQueryLiveData(Query query) {
       this.query = query;
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
                listenerRegistration = query.addSnapshotListener(listener);
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

    private class MyValueEventListener implements EventListener<QuerySnapshot> {
        @Override
        public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
            if (e != null){
                Log.e(TAG, "Can't listen to doc snapshots: " + queryDocumentSnapshots + ":::" + e.getMessage());
                return;
            }
            setValue(queryDocumentSnapshots);
        }
    }
}
