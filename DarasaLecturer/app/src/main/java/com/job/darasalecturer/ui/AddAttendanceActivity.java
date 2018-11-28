package com.job.darasalecturer.ui;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.job.darasalecturer.R;
import com.job.darasalecturer.adapter.AddStudentsAdapter;
import com.job.darasalecturer.appexecutor.DefaultExecutorSupplier;
import com.job.darasalecturer.model.CourseYear;
import com.job.darasalecturer.model.QRParser;
import com.job.darasalecturer.model.StudentDetails;
import com.job.darasalecturer.service.AddAttendanceWorker;
import com.job.darasalecturer.util.DoSnack;
import com.job.darasalecturer.util.StudentViewHolder;
import com.job.darasalecturer.viewmodel.AddStudentViewModel;
import com.leodroidcoder.genericadapter.OnRecyclerItemClickListener;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.job.darasalecturer.service.AddAttendanceWorker.KEY_QR_OBJ_ARG;
import static com.job.darasalecturer.service.AddAttendanceWorker.KEY_STUD_LIST_ARG;
import static com.job.darasalecturer.util.Constants.CURRENT_SEM_PREF_NAME;
import static com.job.darasalecturer.util.Constants.CURRENT_YEAR_PREF_NAME;
import static com.job.darasalecturer.util.Constants.STUDENTDETAILSCOL;

public class AddAttendanceActivity extends AppCompatActivity implements OnRecyclerItemClickListener {

    private static final String TAG = "AddStudent";

    public static final String ADDATTENDANCE_EXTRA = "ADDATTENDANCE_EXTRA";
    public static final String CURRENT_SEM_EXTRA = "CURRENT_SEM_EXTRA";
    public static final String CURRENT_YR_EXTRA = "CURRENT_YR_EXTRA";
    public static final int SEM_YR_REQUEST_CODE = 102;

    @BindView(R.id.stud_toolbar)
    Toolbar studToolbar;
    @BindView(R.id.stud_chipgroup)
    ChipGroup studChipgroup;
    @BindView(R.id.stud_list)
    RecyclerView studList;
    @BindView(R.id.stud_no_student)
    View noStudView;
    @BindView(R.id.stud_save_btn)
    MaterialButton studSaveBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirestoreRecyclerAdapter adapter;

    private MenuItem saveMenu;
    private boolean anySelected;
    private DoSnack doSnack;
    private SharedPreferences mSharedPreferences;
    private QRParser qrParser;
    private Gson gson;

    private AddStudentViewModel addStudentViewModel;
    AddStudentsAdapter addStudentsAdapter;

    private List<StudentDetails> studentDetailsList = new ArrayList<>();
    private List<StudentDetails> fullStudentDetailsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_attendance);
        ButterKnife.bind(this);

        setSupportActionBar(studToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(AppCompatResources.getDrawable(this, R.drawable.ic_back));

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        //init model
        addStudentViewModel = ViewModelProviders.of(this).get(AddStudentViewModel.class);

        doSnack = new DoSnack(this, AddAttendanceActivity.this);
        mSharedPreferences = getSharedPreferences(getApplicationContext().getPackageName(), MODE_PRIVATE);
        addStudentsAdapter = new AddStudentsAdapter(this, this, addStudentViewModel);
        gson = new Gson();

        //retrieve courses
        qrParser = getIntent().getParcelableExtra(ADDATTENDANCE_EXTRA);
        initList();
        loadData();

        Log.d(TAG, "onCreate: "+qrParser.toString());
        //init observers
        studentListObserver();
        //setup our list
    }

    private void loadData() {
        final String currentsemester = mSharedPreferences.getString(CURRENT_SEM_PREF_NAME, "0");
        final String currentyear = mSharedPreferences.getString(CURRENT_YEAR_PREF_NAME, "2000");

        final ArrayList<CourseYear> courseYears = qrParser.getCourses();
        //refresh list b4 foreach loop
        addStudentsAdapter.clear();
        fullStudentDetailsList.clear();
        studList.setVisibility(View.GONE);
        noStudView.setVisibility(View.VISIBLE);


        for (final CourseYear courseYear : courseYears) {

            mFirestore.collection(STUDENTDETAILSCOL)
                    .whereEqualTo("currentyear", currentyear)
                    .whereEqualTo("currentsemester", currentsemester)
                    .whereEqualTo("course", courseYear.getCourse())
                    .whereEqualTo("yearofstudy", String.valueOf(courseYear.getYearofstudy()))
                    .orderBy("regnumber", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(DefaultExecutorSupplier.getInstance().forMainThreadTasks()
                            ,new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                //heavy operation
                                StudentDetails studentDetails = doc.toObject(StudentDetails.class);
                                Log.d(TAG, "Record found!" + studentDetails.toString());
                                fullStudentDetailsList.add(studentDetails);
                            }

                            //list filled
                            //adapter init
                            addStudentsAdapter.setItems(fullStudentDetailsList);
                            addStudentsAdapter.notifyDataSetChanged();

                            if (addStudentsAdapter.getItemCount() == 0) {
                                studList.setVisibility(View.GONE);
                                noStudView.setVisibility(View.VISIBLE);
                            } else {
                                studList.setVisibility(View.VISIBLE);
                                noStudView.setVisibility(View.GONE);
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    studList.setVisibility(View.GONE);
                    noStudView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onError: ", e);
                    DoSnack.showShortSnackbar(AddAttendanceActivity.this,"Error: check logs for info.");
                }
            });
        }
    }

    public void updateSaveStatus() {

        if (anySelected) {
            studSaveBtn.setVisibility(View.VISIBLE);
        } else {
            studSaveBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();

        switch (id) {
            case R.id.amenu_save:
                Toast.makeText(this, "Save the attendees", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (qrParser == null) {
            finish();
        }
    }

    private void initList() {
        LinearLayoutManager linearLayoutManager = new
                LinearLayoutManager(this.getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        studList.setLayoutManager(linearLayoutManager);
        studList.setHasFixedSize(true);
        studList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        studList.setAdapter(addStudentsAdapter);
    }

    @Deprecated
    private void setUpList(Query query) {
        FirestoreRecyclerOptions<StudentDetails> options = new FirestoreRecyclerOptions.Builder<StudentDetails>()
                .setQuery(query, StudentDetails.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<StudentDetails, StudentViewHolder>(options) {

            @NonNull
            @Override
            public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_attendance, parent, false);

                return new StudentViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final StudentViewHolder holder, int position, @NonNull StudentDetails model) {

                holder.init(AddAttendanceActivity.this, mFirestore, model, addStudentViewModel);
                holder.setUpUi(model);

            }


            @Override
            public void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();

                Log.d(TAG, "onError: ", e);
            }

            @Override
            public void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    studList.setVisibility(View.GONE);
                    noStudView.setVisibility(View.VISIBLE);
                } else {
                    studList.setVisibility(View.VISIBLE);
                    noStudView.setVisibility(View.GONE);
                }
            }

        };


        adapter.startListening();
        adapter.notifyDataSetChanged();
        studList.setAdapter(adapter);

    }

    private void studentListObserver() {
        addStudentViewModel.getStudListMediatorLiveData().observe(this, new Observer<List<StudentDetails>>() {
            @Override
            public void onChanged(@Nullable List<StudentDetails> studentDetails) {
                if (studentDetails != null) {

                    studentDetailsList = studentDetails;
                    setToolTipText(studentDetails);
                    studChipgroup.removeAllViews();

                    for (StudentDetails stud : studentDetails) {
                        addChipStudent(stud);
                    }

                    if (studentDetails.isEmpty()) {

                        anySelected = false;
                        updateSaveStatus();
                    } else {

                        anySelected = true;
                        updateSaveStatus();
                    }

                }
            }
        });
    }

    private void setToolTipText(List<StudentDetails> studentDetails) {
        if (studentDetails.isEmpty()) {

            studToolbar.setSubtitle(getString(R.string.for_studs_unable));
        } else {
            studToolbar.setSubtitle("Selected " + studentDetails.size() + " students");
        }
    }

    private void addChipStudent(final StudentDetails model) {
        Chip chip = new Chip(this);
        chip.setChipText(model.getFirstname());
        //chip.setCloseIconEnabled(true);
        //chip.setCloseIconResource(R.drawable.ic_clear);

        chip.setChipBackgroundColorResource(R.color.lightGrey);
        chip.setTextAppearanceResource(R.style.ChipTextStyle2);
        chip.setChipStartPadding(4f);
        chip.setChipEndPadding(4f);

        studChipgroup.addView(chip);

    }

    @OnClick(R.id.textView_to_current_settings)
    public void onTextViewClicked() {
        Intent cIntent = new Intent(this, CurrentSetupActivity.class);
        startActivityForResult(cIntent, SEM_YR_REQUEST_CODE);
        Toast.makeText(this, "Click save to update settings", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            //handled by on resume.
            loadData();
        }
    }

    @OnClick(R.id.stud_save_btn)
    public void onSaveBtnViewClicked() {

        final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));
        pDialog.setCancelable(false);
        pDialog.setTitleText("Saving Attendance");
        pDialog.setTitleText(getString(R.string.you_set));
        pDialog.setCancelable(true);
        pDialog.show();

        saveStudents(addStudentViewModel.getStudListMediatorLiveData().getValue());

        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();

                finish();
            }
        });

    }

    private void saveStudents(List<StudentDetails> studentDetails) {

        Type qrObject = new TypeToken<QRParser>() {
        }.getType();
        String qrString = gson.toJson(qrParser, qrObject);

        Type listOfStudObject = new TypeToken<List<StudentDetails>>() {
        }.getType();
        String students = gson.toJson(studentDetails, listOfStudObject);

        // Create the Data object:
        Data myData = new Data.Builder()
                .putString(KEY_STUD_LIST_ARG, students)
                .putString(KEY_QR_OBJ_ARG, qrString)
                .build();

        //set network required
        Constraints myConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // ...then create and enqueue a OneTimeWorkRequest that uses those arguments
        OneTimeWorkRequest attendWork = new OneTimeWorkRequest.Builder(AddAttendanceWorker.class)
                .setConstraints(myConstraints)
                .setInputData(myData)
                .build();

        WorkManager.getInstance()
                .enqueue(attendWork);
    }

    @Override
    public void onItemClick(int position) {

    }
}
