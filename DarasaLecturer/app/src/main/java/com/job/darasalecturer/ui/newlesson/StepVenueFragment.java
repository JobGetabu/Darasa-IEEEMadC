package com.job.darasalecturer.ui.newlesson;


import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.abdeveloper.library.MultiSelectDialog;
import com.abdeveloper.library.MultiSelectModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.job.darasalecturer.R;
import com.job.darasalecturer.model.CourseYear;
import com.job.darasalecturer.viewmodel.AddClassViewModel;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.job.darasalecturer.util.Constants.DKUTCOURSES;

/**
 * A simple {@link Fragment} subclass.
 */
public class StepVenueFragment extends Fragment {


    @BindView(R.id.step_venue_name)
    TextInputLayout stepVenueName;
    @BindView(R.id.step_venue_department)
    TextInputLayout stepVenueDepartment;
    @BindView(R.id.step_venue_chipgroup)
    ChipGroup stepVenueChipgroup;
    @BindView(R.id.step_venue_course_btn)
    MaterialButton stepVenueCourseBtn;
    @BindView(R.id.step_venue_back)
    TextView stepVenueBack;
    @BindView(R.id.step_venue_next)
    TextView stepVenueNext;
    Unbinder unbinder;

    private AddClassViewModel model;
    private static final String TAG = "stepvenue";

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    public StepVenueFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_step_venue, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        model = ViewModelProviders.of(getActivity()).get(AddClassViewModel.class);
        stepVenueChipgroup.setVisibility(View.GONE);

        //firebase
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.step_venue_course_btn)
    public void onStepVenueCourseBtnClicked() {

        final SweetAlertDialog pDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));
        pDialog.setTitleText("Loading courses...");
        pDialog.setCancelable(true);
        pDialog.show();

        mFirestore.collection(DKUTCOURSES).document("dkut")
                .get(Source.DEFAULT)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            pDialog.dismiss();

                            Map<String, Object> mapdata = task.getResult().getData();

                            if (mapdata != null) {

                                //List of courses with Name and Id
                                ArrayList<MultiSelectModel> listOfCourses = new ArrayList<>();

                                int i = 1;
                                for (Map.Entry<String, Object> entry : mapdata.entrySet()) {
                                    //System.out.println(entry.getKey() + "/" + entry.getValue());

                                    listOfCourses.add(new MultiSelectModel(i, entry.getValue().toString()));
                                    i++;
                                }

                                promptCourseList(listOfCourses);
                            }
                        }
                    }
                });

    }

    private void promptCourseList(ArrayList<MultiSelectModel> listOfCourses) {
        //MultiSelectModel
        MultiSelectDialog multiSelectDialog = new MultiSelectDialog()
                .title(getResources().getString(R.string.select_course)) //setting title for dialog
                .titleSize(20)
                .positiveText("Done")
                .negativeText("Cancel")
                .setMinSelectionLimit(1) //you can set minimum checkbox selection limit (Optional)
                //.preSelectIDsList() //List of ids that you need to be selected
                .multiSelectList(listOfCourses) // the multi select model list with ids and name
                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                    @Override
                    public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {

                        stepVenueChipgroup.removeAllViews();
                        //will return list of selected IDS
                        for (int i = 0; i < selectedIds.size(); i++) {
                            //Toast.makeText(getContext(), "Selected Ids : " + selectedIds.get(i) + "\n" + "Selected Names : " + selectedNames.get(i) + "\n" + "DataString : " + dataString, Toast.LENGTH_SHORT).show();

                            addChipCourse(selectedNames.get(i));
                        }
                        model.setCourseList(selectedNames);
                        //init the courseYear
                        model.setCourseYearList(new ArrayList<CourseYear>());
                        for (String c : selectedNames) {
                            CourseYear courseYear = new CourseYear(c, 1);
                            Log.d(TAG, "courseYear: "+courseYear.toString());
                            model.getCourseYearList().getValue().add(courseYear);
                        }

                        stepVenueChipgroup.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Dialog cancelled");
                    }


                });
        multiSelectDialog.show(getActivity().getSupportFragmentManager(), "multiSelectDialog");
    }

    @OnClick(R.id.step_venue_back)
    public void onStepVenueBackClicked() {
        model.setCurrentStep(1);
    }

    @OnClick(R.id.step_venue_next)
    public void onStepVenueNextClicked() {
        if (validate()) {

            model.setCurrentStep(3);

            String venue = stepVenueName.getEditText().getText().toString();
            String dept = stepVenueDepartment.getEditText().getText().toString();

            model.getLecTeachMediatorLiveData().getValue().setDepartment(dept);

            model.getLecTeachTimeMediatorLiveData().getValue().setVenue(venue);

        }
    }

    public boolean validate() {
        boolean valid = true;

        String venue = stepVenueName.getEditText().getText().toString();
        String dept = stepVenueDepartment.getEditText().getText().toString();

        if (venue.isEmpty()) {
            stepVenueName.setError("enter venue");
            valid = false;
        } else {
            stepVenueName.setError(null);
        }

        if (dept.isEmpty()) {
            stepVenueDepartment.setError("enter department");
            valid = false;
        } else {
            stepVenueDepartment.setError(null);
        }

        if (stepVenueChipgroup.getVisibility() == View.GONE ){
            Toast.makeText(getContext(), "Select a Course", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private void addChipCourse(String course) {
        Chip chip = new Chip(getContext());
        chip.setChipText(course);
        //chip.setCloseIconEnabled(true);
        //chip.setCloseIconResource(R.drawable.ic_clear);

        chip.setChipBackgroundColorResource(R.color.lightGrey);
        chip.setTextAppearanceResource(R.style.ChipTextStyle2);
        chip.setChipStartPadding(4f);
        chip.setChipEndPadding(4f);

        stepVenueChipgroup.addView(chip);

    }
}
