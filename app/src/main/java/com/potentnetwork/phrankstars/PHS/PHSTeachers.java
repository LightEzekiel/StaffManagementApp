package com.potentnetwork.phrankstars.PHS;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.potentnetwork.phrankstars.PCA;
import com.potentnetwork.phrankstars.PrimaryStaff;
import com.potentnetwork.phrankstars.PrimaryStaffProfile;
import com.potentnetwork.phrankstars.R;
import com.potentnetwork.phrankstars.TeacherAdapter;
import com.potentnetwork.phrankstars.Teachers;
import com.potentnetwork.phrankstars.Utility.NetworkChangeStateListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PHSTeachers extends AppCompatActivity implements PHSTeacherAdapter.OnItemClickListener {

    RecyclerView phsTeacherRecyclerView;
    List<PHS> phsTeacher;
    PHSTeacherAdapter phsTeacherAdapter;
    Toolbar teacherToolbar;
    FirebaseFirestore db;
    FloatingActionButton update_button;

    PHS phs;

    ProgressBar progressBar;
    String loginId;
    SwipeRefreshLayout swipeRefreshLayout;
    MaterialAlertDialogBuilder builder, builder2;

    double remainingLoanUpdate,StaffLoanPaid,MainBasicSalary;
    long diff,seconds,minutes,hours,days;
    int count = 1;
    int count2 = 1;

    WriteBatch batch;
    double saving,savingPerMth;
    String currentDate,trackingDate,today;
    boolean isFinished = false;

    Date date,newDate;
    SimpleDateFormat f,f2;
    Date d,d2;
    long milliseconds, milliseconds2;

    NetworkChangeStateListener networkChangeStateListener = new NetworkChangeStateListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phsteachers);


        teacherToolbar = findViewById(R.id.phs_teachers_toolBar);
        setSupportActionBar(teacherToolbar);
        getSupportActionBar().setTitle("High School Staff");
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutSecondary);

        progressBar = findViewById(R.id.phs_spin_kit1);
        Sprite wave = new Wave();
        progressBar.setIndeterminateDrawable(wave);

        db = FirebaseFirestore.getInstance();
        loginId = getIntent().getStringExtra("User");
        update_button = findViewById(R.id.update_button_secondary);



        phsTeacher = new ArrayList<>();
        phsTeacherRecyclerView = findViewById(R.id.phs_teacher_recyclerView);
        phsTeacherAdapter = new PHSTeacherAdapter(this,phsTeacher,this);
        phsTeacherRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        phsTeacherRecyclerView.setAdapter(phsTeacherAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        phsTeacherRecyclerView.addItemDecoration(dividerItemDecoration);

        refreshRecyclerView();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshSecondaryStaffEvents();
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
                builder.setTitle("UPDATE HIGH SCHOOL STAFF DETAILS?");
                builder.setMessage("Staff profile (Savings/Loan) will be updated.");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateSecondaryStaffStatus();
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

    private void updateSecondaryStaffStatus() {
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

        for (PHS staff : phsTeacher) {
            String string_date = staff.getTrackingDate();
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

            if (!staff.getTrackingDate().isEmpty() && days >= 25) {
                isFinished = true;

                if (days >= 25 && !staff.getStaffLoan1().isEmpty()) {
                    // clear staff debt,add staff debt to payable,
                    //update remaining loan
                    // update total savings; total savings + savings per month
                    // update total loan paid,add it to firebase
                    // update payable+debt
                    // update trackingDate to dateOfUpdate

                    remainingLoanUpdate = Double.parseDouble(staff.getRemainingLoan());
                    StaffLoanPaid = Double.parseDouble(staff.getLoanPaid());


                    count++;
                    isFinished = true;
                    StaffLoanPaid += Double.parseDouble(staff.getStaffLoanPayPerMonth1());
                    remainingLoanUpdate -= Double.parseDouble(staff.getStaffLoanPayPerMonth1());


                    batch = db.batch();
                    // Update the population of 'SF'
                    DocumentReference sfRef = db.collection("PHS").document(staff.getId());

//                    double payableUpdate = staff.getPayable();
//                    if (!staff.getStaffDeduction1().equals("")) {
//                        payableUpdate += Double.parseDouble(staff.getStaffDeduction1());
//                    }


//                    batch.update(sfRef, "payable", payableUpdate);
                    batch.update(sfRef, "loanPaid", String.valueOf(StaffLoanPaid));
                    batch.update(sfRef, "remainingLoan", String.valueOf(remainingLoanUpdate));
                    batch.update(sfRef, "trackingDate", trackingDate);
//                    batch.update(sfRef, "staffDeduction1", "");
//                    batch.update(sfRef, "staffBonus1", "");

                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                            if (task.isSuccessful()) {
                                Toast.makeText(PHSTeachers.this, staff.getStaff_name1()+"\nloan Detail updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                    if (remainingLoanUpdate < 0) {
                        isFinished = true;
                        double payableUpdate = Double.parseDouble(staff.getStaffLoanPayPerMonth1()) + staff.getPayable();
                        WriteBatch batch2;
                        batch2 = db.batch();
                        DocumentReference sfRef2 = db.collection("PHS").document(staff.getId());
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

                                    Toast.makeText(PHSTeachers.this, staff.getStaff_name1() + " Loan Cleared", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }

                }

                if (days >= 25 && !staff.getStaffSavings1().isEmpty()) {
                    //Increase staff savings using savings per month
                    count2++;

                    saving = Double.parseDouble(staff.getStaffSavings1());
                    savingPerMth = Double.parseDouble(staff.getStaffSavingsPerMonth1());
                    if (!staff.getStaffSavings1().isEmpty()) {
                        isFinished = true;

                        saving += savingPerMth;
                        WriteBatch batch3;
                        batch3 = db.batch();
                        DocumentReference sfRef3 = db.collection("PHS").document(staff.getId());
                        batch3.update(sfRef3, "staffSavings1", String.valueOf(saving));
                        batch3.update(sfRef3, "trackingDate", trackingDate);
                        batch3.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // ...
                                if (task.isSuccessful()) {
                                    Toast.makeText(PHSTeachers.this, staff.getStaff_name1()+" Savings Updated", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    } else {
                        saving = Double.parseDouble("");
                        savingPerMth = Double.parseDouble("");
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

                                    Toast.makeText(getApplicationContext(), staff.getStaff_name1() + "\nLast Month Deductions Cleared", Toast.LENGTH_SHORT).show();
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
                }
                isFinished = false;
            }
        }
        refreshSecondaryStaffEvents();

        builder2 = new MaterialAlertDialogBuilder(PHSTeachers.this);
        builder2.setIcon(R.drawable.ic_update_staff_profiles);
//                builder2.setTitle("UPDATE STAFF DETAILS?");
        builder2.setMessage("High School Staff details UP-TO-DATE.");
        builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder2.show();

    }

    private void refreshSecondaryStaffEvents() {
        phsTeacher.clear();
        phsTeacherAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.VISIBLE);
        refreshRecyclerView();
        phsTeacherAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void refreshRecyclerView(){
        db.collection("PHS").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()){
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        // after getting this list we are passing
                        // that list to our object class.
                        PHS phs = d.toObject(PHS.class);
                        progressBar.setVisibility(View.INVISIBLE);
//                        pc.setId(d.getId());

                        phsTeacher.add(phs);
                        Collections.sort(phsTeacher,PHS.staffName);

                        // and we will pass this object class
                        // inside our arraylist which we have
                        // created for recycler view.

                    }
                    phsTeacherAdapter.notifyDataSetChanged();

                }else{
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(PHSTeachers.this, "No staff yet", Toast.LENGTH_LONG).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PHSTeachers.this, "Failed to get data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.phs_teacher_account, menu);
        MenuItem item=menu.findItem(R.id.Phs_Staff_name_search);
        SearchView searchView=(SearchView)item.getActionView();
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
        switch (item.getItemId()){
            case R.id.phs_add:
                if (loginId.equals("BOSS")){
                    loginId = "BOSS";
                }else {
                    loginId = "ADMIN";
                }
                Intent addTeacher = new Intent(PHSTeachers.this, SecondaryStaff.class);
                addTeacher.putExtra("User",loginId);
                startActivity(addTeacher);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void ProcessSearch(String s) {
        ArrayList<PHS> mainlist = new ArrayList<>();
        for (PHS phs: phsTeacher){
            if (phs.getStaff_name1().toLowerCase().contains(s.toLowerCase())){
                mainlist.add(phs);

            }
        }
        PHSTeacherAdapter phsTeacherAdapter= new PHSTeacherAdapter(PHSTeachers.this,mainlist,this);
        phsTeacherRecyclerView.setAdapter(phsTeacherAdapter);


    }


    @Override
    public void onItemclick(int position) {
        phs = phsTeacher.get(position);

        if (loginId.equals("BOSS")){
            loginId = "BOSS";
        }else {
            loginId = "ADMIN";
        }
        Intent staffProfile = new Intent(PHSTeachers.this, SecondaryStaffProfile.class);
        staffProfile.putExtra("phs_primary_staff", phs);
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

}