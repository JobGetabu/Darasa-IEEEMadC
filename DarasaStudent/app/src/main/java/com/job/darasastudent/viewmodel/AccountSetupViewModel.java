package com.job.darasastudent.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.job.darasastudent.appexecutor.DefaultExecutorSupplier;
import com.job.darasastudent.model.LecTeachTime;
import com.job.darasastudent.model.StudentDetails;
import com.job.darasastudent.repository.FirebaseDocumentLiveData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.job.darasastudent.util.Constants.STUDENTDETAILSCOL;

/**
 * Created by Job on Tuesday : 9/25/2018.
 */
public class AccountSetupViewModel extends AndroidViewModel {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    public static final String TAG = "AccountSetupVM";

    //few db references
    private DocumentReference userRef;

    //live data
    private FirebaseDocumentLiveData mUserLiveData;
    private MediatorLiveData<List<LecTeachTime>> listLecTeachTimeResult;

    private LiveData<Calendar> calendarLiveData;


    //mediators
    private MediatorLiveData<StudentDetails> studUserMediatorLiveData;
    private MediatorLiveData<List<LecTeachTime>> lecTechTimeLiveData;

    public AccountSetupViewModel(@NonNull Application application, FirebaseAuth mAuth, FirebaseFirestore mFirestore) {
        super(application);
        this.mAuth = mAuth;
        this.mFirestore = mFirestore;


        //init mediators
        studUserMediatorLiveData = new MediatorLiveData<>();
        lecTechTimeLiveData = new MediatorLiveData<>();
        listLecTeachTimeResult = new MediatorLiveData<>();
        listLecTeachTimeResult.setValue(new ArrayList<LecTeachTime>());

        //init db refs 
        userRef = mFirestore.collection(STUDENTDETAILSCOL).document(mAuth.getCurrentUser().getUid());


        //init livedatas
        mUserLiveData = new FirebaseDocumentLiveData(userRef);

        // Set up the MediatorLiveData to convert DataSnapshot objects into POJO objects
        workOnUsersLiveData();
    }

    private void workOnUsersLiveData() {

        studUserMediatorLiveData.addSource(mUserLiveData, new Observer<DocumentSnapshot>() {
            @Override
            public void onChanged(@Nullable final DocumentSnapshot documentSnapshot) {

                if (documentSnapshot != null) {

                    DefaultExecutorSupplier.getInstance().forBackgroundTasks()
                            .submit(new Runnable() {
                                @Override
                                public void run() {

                                    studUserMediatorLiveData.postValue(documentSnapshot.toObject(StudentDetails.class));
                                }
                            });
                } else {
                    studUserMediatorLiveData.postValue(null);
                }
            }
        });
    }

    public MediatorLiveData<List<LecTeachTime>> getLecTechTimeLiveData() {
        return lecTechTimeLiveData;
    }

    public MediatorLiveData<StudentDetails> getLecUserMediatorLiveData() {
        return studUserMediatorLiveData;
    }

    /**
     * Factory for instantiating the viewmodel
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;
        private FirebaseAuth mAuth;
        private FirebaseFirestore mFirestore;

        public Factory(@NonNull Application mApplication, FirebaseAuth mAuth, FirebaseFirestore mFirestore) {
            this.mApplication = mApplication;
            this.mAuth = mAuth;
            this.mFirestore = mFirestore;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new AccountSetupViewModel(mApplication, mAuth, mFirestore);
        }
    }
}
