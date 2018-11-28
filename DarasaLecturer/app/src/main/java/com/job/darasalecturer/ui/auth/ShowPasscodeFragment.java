package com.job.darasalecturer.ui.auth;


import android.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.hanks.passcodeview.PasscodeView;
import com.job.darasalecturer.R;
import com.job.darasalecturer.util.DoSnack;
import com.job.darasalecturer.viewmodel.ScannerViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.job.darasalecturer.util.Constants.LECAUTHCOL;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShowPasscodeFragment extends DialogFragment {

    public static final String TAG = "ShowPasscodeFragment";

    private OnSuccessFail onSuccessFail;
    private DoSnack doSnack;

    @BindView(R.id.passcodeView)
    PasscodeView passcodeView;

    private ScannerViewModel model;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    public void setOnSuccessFail(OnSuccessFail onSuccessFail) {
        this.onSuccessFail = onSuccessFail;
    }

    public ShowPasscodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Translucent_NoTitleBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_passcode, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        //init model
        model = ViewModelProviders.of(getActivity()).get(ScannerViewModel.class);

        doSnack = new DoSnack(getContext(), getActivity());

        mFirestore.collection(LECAUTHCOL).document(mAuth.getUid()).get(Source.CACHE)
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String pss = documentSnapshot.getString("localpasscode");

                        if (pss.isEmpty()) {
                            doSnack.showSnackbarDissaper("Set Password to continue", "Set", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(getContext(), PasscodeActivity.class);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            passcodeView
                                    .setPasscodeLength(4)
                                    .setLocalPasscode(pss)
                                    .setFirstInputTip(getResources().getString(R.string.enter_your_pin))
                                    .setPasscodeType(PasscodeView.PasscodeViewType.TYPE_CHECK_PASSCODE)
                                    .setListener(new PasscodeView.PasscodeViewListener() {
                                        @Override
                                        public void onFail(String wrongNumber) {
                                            onSuccessFail.onFail();
                                            dismiss();
                                        }

                                        @Override
                                        public void onSuccess(String number) {

                                            onSuccessFail.onSuccess();
                                            dismiss();
                                        }
                                    });
                        }
                    }
                });

        if (model.getPasscodeLiveData().getValue() != null) {

            passcodeView
                    .setPasscodeLength(4)
                    .setLocalPasscode(model.getPasscodeLiveData().getValue())
                    .setFirstInputTip(getResources().getString(R.string.enter_your_pin))
                    .setPasscodeType(PasscodeView.PasscodeViewType.TYPE_CHECK_PASSCODE)
                    .setListener(new PasscodeView.PasscodeViewListener() {
                        @Override
                        public void onFail(String wrongNumber) {


                            onSuccessFail.onFail();
                            dismiss();
                        }

                        @Override
                        public void onSuccess(String number) {

                            onSuccessFail.onSuccess();
                            dismiss();
                        }
                    });
        } else {
            doSnack.showSnackbarDissaper("Set Password to continue", "Set", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), PasscodeActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    //Declare an Interface
    public interface OnSuccessFail {
        void onSuccess();

        void onFail();
    }
}
