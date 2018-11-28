package com.job.darasalecturer.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
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
import com.job.darasalecturer.appexecutor.DefaultExecutorSupplier;
import com.job.darasalecturer.model.LecUser;
import com.job.darasalecturer.repository.FirebaseDocumentLiveData;

import static com.job.darasalecturer.util.Constants.LECUSERCOL;

/**
 * Created by Job on Tuesday : 9/25/2018.
 */
public class AccountSetupViewModel extends AndroidViewModel {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    public static final String TAG = "AccountSetupVM";

    //few db references
    private DocumentReference userRef;

    //live datas
    private FirebaseDocumentLiveData mUserLiveData;

    //mediators
    private MediatorLiveData<LecUser> lecUserMediatorLiveData = new MediatorLiveData<>();

    public AccountSetupViewModel(@NonNull Application application, FirebaseAuth mAuth, FirebaseFirestore mFirestore) {
        super(application);
        this.mAuth = mAuth;
        this.mFirestore = mFirestore;

        //init db refs 
        userRef = mFirestore.collection(LECUSERCOL).document(mAuth.getCurrentUser().getUid());


        //init livedatas
        mUserLiveData = new FirebaseDocumentLiveData(userRef);

        // Set up the MediatorLiveData to convert DataSnapshot objects into POJO objects
        workOnUsersLiveData();
    }

    private void workOnUsersLiveData() {

        lecUserMediatorLiveData.addSource(mUserLiveData, new Observer<DocumentSnapshot>() {
            @Override
            public void onChanged(@Nullable final DocumentSnapshot documentSnapshot) {

                if (documentSnapshot != null){

                    DefaultExecutorSupplier.getInstance().forBackgroundTasks()
                            .submit(new Runnable() {
                                @Override
                                public void run() {

                                    lecUserMediatorLiveData.postValue(documentSnapshot.toObject(LecUser.class));
                                }
                            });

                }else {
                    lecUserMediatorLiveData.postValue(null);
                }
            }
        });
    }


    public MediatorLiveData<LecUser> getLecUserMediatorLiveData() {
        return lecUserMediatorLiveData;
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
