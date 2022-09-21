package com.potentnetwork.phrankstars;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.ThreeBounce;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.potentnetwork.phrankstars.PHS.PHS;
import com.potentnetwork.phrankstars.PHS.PHSTeacherAdapter;
import com.potentnetwork.phrankstars.PHS.PHSTeachers;
import com.potentnetwork.phrankstars.PHS.RecyclerViewDataPass;
import com.potentnetwork.phrankstars.Utility.NetworkChangeStateListener;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Teachers extends AppCompatActivity implements TeacherAdapter.OnItemClickListener {
    RecyclerView teacherRecyclerView;
    List<PCA> mTeacher;
    TeacherAdapter teacherAdapter;
    Toolbar teacherToolbar;
    RecyclerViewDataPass recyclerViewDataPass;
    int countUpdate = 0;

    DatabaseReference teacherDbReference;
    DatabaseReference dbReference;
    ValueEventListener valueEventListener;
    FloatingActionButton update_button;
    FirebaseFirestore db;

    PCA pca;

    ProgressBar progressBar;
    String loginId;

    double remainingLoanUpdate, StaffLoanPaid;
    long diff, seconds, minutes, hours, days;
    int count = 1;
    int count2 = 1;

    WriteBatch batch;
    double saving, savingPerMth;
    String currentDate, trackingDate;
    boolean isFinished = false;
    SimpleDateFormat f,f2;
    Date d,d2;
    long milliseconds,milliseconds2;

    TextView staffProfileTextView;


    Date date, newDate;
    SimpleDateFormat dt;
    String string_date;

    MaterialAlertDialogBuilder builder, builder2;
    SwipeRefreshLayout swipeRefreshLayout;

    NetworkChangeStateListener networkChangeStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachers);

        loginId = getIntent().getStringExtra("User");
        networkChangeStateListener = new NetworkChangeStateListener();

        teacherToolbar = findViewById(R.id.teachers_toolBar);
        setSupportActionBar(teacherToolbar);
        getSupportActionBar().setTitle("Basic School Staff");
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        progressBar = findViewById(R.id.spin_kit1);
        Sprite wave = new Wave();
        progressBar.setIndeterminateDrawable(wave);

        db = FirebaseFirestore.getInstance();
        teacherDbReference = FirebaseDatabase.getInstance().getReference();
        dbReference = teacherDbReference.child("TeacherProfile");
        update_button = findViewById(R.id.update_button);


        mTeacher = new ArrayList<>();
        teacherRecyclerView = findViewById(R.id.teacher_recyclerView);
        teacherAdapter = new TeacherAdapter(this, mTeacher, this, recyclerViewDataPass);
        teacherRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        teacherRecyclerView.setAdapter(teacherAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        teacherRecyclerView.addItemDecoration(dividerItemDecoration);

        updateRecyclerView(); ///recyclerView

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshEvents();
            }
        });
        if (loginId.equals("BOSS")){
            update_button.setVisibility(View.VISIBLE);
        }
        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder = new MaterialAlertDialogBuilder(v.getContext());
                builder.setIcon(R.drawable.ic_update_staff_profiles);
                builder.setTitle("UPDATE STAFF DETAILS?");
                builder.setMessage("Basic School Staff profiles (Allowances/Savings/Loan) will be updated.");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateStaffStatus();
                        updatePrimaryStaffAllowance();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();

            }
        });


    }

    private void updatePrimaryStaffAllowance() {
        date = new Date();
        newDate = new Date(date.getTime());
        SimpleDateFormat dt = new SimpleDateFormat("EEE,dd MMM, yy");
        currentDate = dt.format(newDate);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();

        date = new Date();
        newDate = new Date(date.getTime());
        dt = new SimpleDateFormat("EEE,dd MMM, yy");
        trackingDate = dt.format(newDate);

        for(PCA staff : mTeacher){

            String string_date = staff.getCreatedDate();
            f = new SimpleDateFormat("EEE,dd MMM, yy");
            f2 = new SimpleDateFormat("EEE,dd MMM, yy");
            try {
                d = f.parse(string_date);
                d2 = f2.parse(currentDate);
                milliseconds = d.getTime();
                milliseconds2 = d2.getTime();

                long milliseconds3 = tomorrow.getTime();

                diff = milliseconds2 - milliseconds;
                seconds = diff / 1000;
                minutes = seconds / 60;
                hours = minutes / 60;
                days = (hours / 24) + 1;
                Log.d("days", "" + days);
                //staffLoanPaid12.setText(String.valueOf(days) );
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (!staff.getStaffResignationAllowance1().isEmpty() && days >= 365){
                isFinished = true;
//            staffYearlyResignationBalanceTxtV12.setText(String.valueOf(days1)); //"Balance at "+currentDate+":"

                double staffYearAllowance = Double.parseDouble(staff.getStaffResignationAllowance1());
                String staffEndOfTheYearTxtV = "Balance at "+currentDate+":"; //pca1.getStaffEndOfYearAllowanceTxtV();
//            double staffAllowanceIncrement = totalPay + Double.parseDouble(pca1.getStaffResignationAllowance1());
//            staff_basic_allowances11 = String.valueOf(staffallw);

                WriteBatch batch7;
                batch7 = db.batch();
                DocumentReference sfRef2 = db.collection("PCA").document(staff.getId());
                batch7.update(sfRef2, "staffYearAllowanceBalance", staffYearAllowance);
                batch7.update(sfRef2, "staffEndOfYearAllowanceTxtV", staffEndOfTheYearTxtV);
                batch7.update(sfRef2, "createdDate", currentDate);
                batch7.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                        if (task.isSuccessful()) {
//                                        progressBar.setVisibility(View.INVISIBLE);
//                                        staffProfileTextView.setVisibility(View.INVISIBLE);
//                        Intent i = new Intent(PrimaryStaffProfile.this, Teachers.class);
//                        i.putExtra("User",boss);
//                        startActivity(i);
//                        finish();
//                        Toast.makeText(getApplicationContext(), pca1.getStaff_name1() + "\nResignation Allowance Updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                isFinished = false;
            }
        }
        refreshEvents();

    }

    private void updateRecyclerView() {

        db.collection("PCA").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        // after getting this list we are passing
                        // that list to our object class.

                        PCA pc = d.toObject(PCA.class);
                        progressBar.setVisibility(View.INVISIBLE);
//                        pc.setId(d.getId());
                        mTeacher.add(pc);
                        Collections.sort(mTeacher,PCA.staffName);
                        // and we will pass this object class
                        // inside our arraylist which we have
                        // created for recycler view.
                    }

                    teacherAdapter.notifyDataSetChanged();

                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(Teachers.this, "No staff yet", Toast.LENGTH_LONG).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Teachers.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.teachers_menu, menu);
        MenuItem item = menu.findItem(R.id.Staff_name_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Search name");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ProcessSearch(query);
                return false;
            }


            @Override
            public boolean onQueryTextChange(String newText) {
                ProcessSearch(newText);
                return false;
            }
        });
//        return super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                if (loginId.equals("BOSS")){
                    loginId = "BOSS";
                }else {
                    loginId = "ADMIN";
                }
                Intent addTeacher = new Intent(Teachers.this, PrimaryStaff.class);
                addTeacher.putExtra("User",loginId);
                startActivity(addTeacher);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void ProcessSearch(String s) {
        ArrayList<PCA> mainlist = new ArrayList<>();
        for (PCA pca : mTeacher) {
            if (pca.getStaff_name1().toLowerCase().contains(s.toLowerCase())) {
                mainlist.add(pca);
            }
        }
        TeacherAdapter teacherAdapter = new TeacherAdapter(Teachers.this, mainlist, this, recyclerViewDataPass);
        teacherRecyclerView.setAdapter(teacherAdapter);
    }

    private void updateStaffStatus() {
//        AlertDialog.Builder builder1 = new AlertDialog.Builder(Teachers.this);
//        View layout_dialog = LayoutInflater.from(Teachers.this).inflate(R.layout.update_staff_profile_loader, null);
//        builder1.setView(layout_dialog);
//
////      AppCompatButton retryBtn = layout_dialog.findViewById(R.id.btnRtry);
//        AlertDialog dialog1 = builder1.create();
//        dialog1.show();
//        dialog1.setCancelable(false);
//        dialog1.getWindow().setGravity(Gravity.CENTER);
//        dialog1.dismiss();

        date = new Date();
        newDate = new Date(date.getTime());
        dt = new SimpleDateFormat("EEE,dd MMM, yy");
        currentDate = dt.format(newDate);


        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();

        date = new Date();
        newDate = new Date(date.getTime());
        dt = new SimpleDateFormat("EEE,dd MMM, yy");
        trackingDate = dt.format(newDate);


        for (PCA staff : mTeacher) {
            string_date = staff.getTrackingDate();
            f = new SimpleDateFormat("EEE,dd MMM, yy");
            f2 = new SimpleDateFormat("EEE,dd MMM, yy");
            try {
                d = f.parse(string_date);
                d2 = f2.parse(currentDate);
                milliseconds = d.getTime();
                milliseconds2 = d2.getTime();

                long milliseconds3 = tomorrow.getTime();

                diff = milliseconds2 - milliseconds;
                seconds = diff / 1000;
                minutes = seconds / 60;
                hours = minutes / 60;
                days = (hours / 24) + 1;
                Log.d("days", "" + days);
//                holder.dayCount.setText(days+" days");
//                         staffLoanPaid12.setText(String.valueOf(days) );
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (!staff.getTrackingDate().isEmpty() && days >= 25) {
                isFinished =true;

//                if (!staff.getTrackingDate().isEmpty()) {

//        }else {
//            string_date = String.valueOf(tomorrow.getTime());
//        }



//
//                    if (!staff.getTrackingDate().isEmpty() && days >= 25
//                            && !staff.getStaffLoan1().isEmpty()
//                            || !staff.getStaffSavings1().isEmpty()
//                    ) {

                    if (days >= 25 && !staff.getStaffLoan1().isEmpty()) {
                        // clear staff debt,add staff debt to payable,
                        //update remaining loan
                        // update total savings; total savings + savings per month
                        // update total loan paid,add it to firebase
                        // update payable+debt
                        // update trackingDate to dateOfUpdate
//                        progressBar.setVisibility(View.VISIBLE);
//                        staffProfileTextView.setVisibility(View.VISIBLE);

                        remainingLoanUpdate = Double.parseDouble(staff.getRemainingLoan());
                        StaffLoanPaid = Double.parseDouble(staff.getLoanPaid());

                        count++;
                        isFinished = true;
                        StaffLoanPaid += Double.parseDouble(staff.getStaffLoanPayPerMonth1());
                        remainingLoanUpdate -= Double.parseDouble(staff.getStaffLoanPayPerMonth1());


                        batch = db.batch();
                        // Update the population of 'SF'
                        DocumentReference sfRef = db.collection("PCA").document(staff.getId());


//                        double payableUpdate = staff.getPayable();
//                        if (!staff.getStaffDeduction1().equals("")) {
//                            payableUpdate += Double.parseDouble(staff.getStaffDeduction1());
//                        }
//
//                        batch.update(sfRef, "staffDeduction1", "");
//                        batch.update(sfRef, "staffBonus1", "");

//                        batch.update(sfRef, "payable", payableUpdate);
                        batch.update(sfRef, "loanPaid", String.valueOf(StaffLoanPaid));
                        batch.update(sfRef, "remainingLoan", String.valueOf(remainingLoanUpdate));
                        batch.update(sfRef, "trackingDate", trackingDate);

// Delete the city 'LA'
//            DocumentReference laRef = db.collection("cities").document("LA");
//            batch.delete(laRef);

// Commit the batch
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // ...
                                if (task.isSuccessful()) {

//                                    progressBar.setVisibility(View.INVISIBLE);
//                                    staffProfileTextView.setVisibility(View.INVISIBLE);
//                                    Intent i = new Intent(PrimaryStaffProfile.this, Teachers.class);
//                                    i.putExtra("User",boss);
//                                    startActivity(i);
//                                    finish();
                                    Toast.makeText(Teachers.this, staff.getStaff_name1()+"\nloan Detail Updated", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });


                        if (remainingLoanUpdate < 0) {
                            isFinished = true;
                            double payableUpdate = staff.getPayable();
                            payableUpdate += Double.parseDouble(staff.getStaffLoanPayPerMonth1());
                            WriteBatch batch2;
                            batch2 = db.batch();
                            DocumentReference sfRef2 = db.collection("PCA").document(staff.getId());
                            batch2.update(sfRef2, "staffLoan1", "");
                            batch2.update(sfRef2, "staffLoanPayPerMonth1", "");
                            batch2.update(sfRef2, "remainingLoan", "");
                            batch2.update(sfRef2, "payable", payableUpdate);
                            batch2.update(sfRef2, "loanPaid", "");
                            batch2.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // ...
                                    if (task.isSuccessful()) {
//                                        progressBar.setVisibility(View.INVISIBLE);
//                                        staffProfileTextView.setVisibility(View.INVISIBLE);

                                        Toast.makeText(getApplicationContext(), staff.getStaff_name1() + " Loan Cleared", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

//                    DocumentReference ClearStaffLoan = db.collection("PCA").document(pca1.getId());
//                    ClearStaffLoan.update("staffLoan1","").addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void unused) {
//                            customProgressDialog.dismiss();
//                            Toast.makeText(PrimaryStaffProfile.this, "Staff Loan Cleared", Toast.LENGTH_SHORT).show();
//                        }
//                    });2


                            // if staffloanpaid is greater than staffloan, clear them from database
                            //add every details subtracted when creating the staff account


                            //  if deduction or bonus is not empty,clear them

                            //clear staff loan and the loan pay per month from database
                            // refresh the database
                            // Update Admin that total loan has been paid by staff(using Toast)
                            // update staff savings to database


                        }

                    }


                    if (days >= 25 && !staff.getStaffSavings1().isEmpty()) {
                        //Increase staff savings using savings per month
                        count2++;
//                        progressBar.setVisibility(View.VISIBLE);
//                        staffProfileTextView.setVisibility(View.VISIBLE);
                        saving = Double.parseDouble(staff.getStaffSavings1());
                        savingPerMth = Double.parseDouble(staff.getStaffSavingsPerMonth1());

                        if (!staff.getStaffSavings1().isEmpty()) {
                            isFinished = true;
                            trackingDate = currentDate;
                            saving += savingPerMth;
                            WriteBatch batch3;
                            batch3 = db.batch();
                            DocumentReference sfRef3 = db.collection("PCA").document(staff.getId());
                            batch3.update(sfRef3, "staffSavings1", String.valueOf(saving));
                            batch3.update(sfRef3, "trackingDate", trackingDate);
                            batch3.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // ...
                                    if (task.isSuccessful()) {

//                                        progressBar.setVisibility(View.INVISIBLE);
//                                        staffProfileTextView.setVisibility(View.INVISIBLE);
                                        Toast.makeText(Teachers.this, staff.getStaff_name1()+" Savings Updated", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            saving = Double.parseDouble("");
                            savingPerMth = Double.parseDouble("");
                        }

                    }
                if (days >= 25 && !staff.getStaffDeduction1().isEmpty()) {
                    isFinished = true;
                    double payableUpdate = staff.getPayable();
                    if (!staff.getStaffDeduction1().equals("")) {
                        payableUpdate += Double.parseDouble(staff.getStaffDeduction1());
                    }
                    WriteBatch batch4;
                    batch4 = db.batch();
                    DocumentReference sfRef2 = db.collection("PCA").document(staff.getId());
                    batch4.update(sfRef2, "payable", payableUpdate);
                    batch4.update(sfRef2, "staffDeduction1", "");
                    batch4.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                            if (task.isSuccessful()) {
//                                        progressBar.setVisibility(View.INVISIBLE);
//                                        staffProfileTextView.setVisibility(View.INVISIBLE);

                                Toast.makeText(getApplicationContext(), staff.getStaff_name1() + "\nLast Month Debt Cleared", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                if (days >= 25 && !staff.getStaffBonus1().isEmpty()) {
                    isFinished = true;
                    double payableUpdate = staff.getPayable();
                    if (!staff.getStaffBonus1().equals("")) {
                        payableUpdate -= Double.parseDouble(staff.getStaffBonus1());
                    }
                    WriteBatch batch5;
                    batch5 = db.batch();
                    DocumentReference sfRef2 = db.collection("PCA").document(staff.getId());
                    batch5.update(sfRef2, "payable", payableUpdate);
                    batch5.update(sfRef2, "staffBonus1", "");
                    batch5.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                            if (task.isSuccessful()) {
//                                        progressBar.setVisibility(View.INVISIBLE);
//                                        staffProfileTextView.setVisibility(View.INVISIBLE);

                                Toast.makeText(getApplicationContext(), staff.getStaff_name1() + "\nLast Month Bonus Cleared", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                if (days >= 25 && !staff.getStaffResignationAllowance1().isEmpty()) { ///deduction
                    isFinished = true;

                    double totalPay = (Double.parseDouble(staff.getStaffSalary1())/ 100.0f) * 10;
                    double staffAllowanceIncrement = totalPay + Double.parseDouble(staff.getStaffResignationAllowance1());

                    WriteBatch batch6;
                    batch6 = db.batch();
                    DocumentReference sfRef2 = db.collection("PCA").document(staff.getId());
                    batch6.update(sfRef2, "StaffResignationAllowance1", staffAllowanceIncrement);
                    batch6.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                            if (task.isSuccessful()) {
//                                        progressBar.setVisibility(View.INVISIBLE);
//                                        staffProfileTextView.setVisibility(View.INVISIBLE);
//                                Intent i = new Intent(PrimaryStaffProfile.this, Teachers.class);
//                                i.putExtra("User",boss);
//                                startActivity(i);
//                                finish();
//                                Toast.makeText(getApplicationContext(), staff.getStaff_name1() + "\nResignation Allowance Updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                isFinished = false;
            }
        }

        refreshEvents();

        builder2 = new MaterialAlertDialogBuilder(Teachers.this);
        builder2.setIcon(R.drawable.ic_update_staff_profiles);
//                builder2.setTitle("UPDATE STAFF DETAILS?");
        builder2.setMessage("Basic Staff details UP-TO-DATE.");
        builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder2.show();
    }







    @Override
    public void onItemclick(int position) {
        pca = mTeacher.get(position);
        if (loginId.equals("BOSS")){
            loginId = "BOSS";
        }else {
            loginId = "ADMIN";
        }

        Intent staffProfile = new Intent(Teachers.this,PrimaryStaffProfile.class);
        staffProfile.putExtra("primary_staff",pca);
        staffProfile.putExtra("User",loginId);
       startActivity(staffProfile);

    }
    @Keep
    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeStateListener,filter);
        super.onStart();
    }

    @Keep
    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeStateListener);
        super.onStop();
    }



    public void refreshEvents()

    {
        mTeacher.clear();
        teacherAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.VISIBLE);
        updateRecyclerView();
        teacherAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

}