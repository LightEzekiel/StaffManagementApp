package com.potentnetwork.phrankstars;

import android.app.ProgressDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.potentnetwork.phrankstars.PHS.RecyclerViewDataPass;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.ViewHolder> {

    Context mContext;
    List<PCA> mTeacher;

    OnItemClickListener mOnItemClickListener;
    ImageView staff_imageView;


    FirebaseFirestore db;

    double remainingLoanUpdate, StaffLoanPaid;
    long diff, seconds, minutes, hours, days;
    int count = 1;
    int count2 = 1;

    WriteBatch batch;
    double saving, savingPerMth;
    String currentDate, trackingDate;
    boolean isFinished = false;
    Date date,newDate;
    SimpleDateFormat dt;
    String string_date;

    private ArrayList<String> arraySearchStrings = new ArrayList<>();

    MaterialAlertDialogBuilder builder;


    RecyclerViewDataPass recyclerViewDataPass;

    public TeacherAdapter(Context mContext, List<PCA> mTeacher, OnItemClickListener mOnItemClickListener, RecyclerViewDataPass recyclerViewDataPass) {
        this.mContext = mContext;
        this.mTeacher = mTeacher;
        this.mOnItemClickListener = mOnItemClickListener;
        this.recyclerViewDataPass = recyclerViewDataPass;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.teachers_list_layout, parent, false);
        db = FirebaseFirestore.getInstance();

        return new ViewHolder(view, mOnItemClickListener);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        Glide.with(mContext).load(mTeacher.get(position).getTeacherImageUri())
                .centerCrop()
                .placeholder(R.drawable.ic_glide_person)
                .into(staff_imageView);

        Collections.sort(mTeacher, PCA.staffName);
        holder.teacherName.setText(mTeacher.get(position).getStaff_name1());

    }

            

    

    @Override
    public int getItemCount() {
        return mTeacher.size();
    }




    public  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView teacherName;
        CardView teacherCardView;
        OnItemClickListener onItemClickListener;



        public ViewHolder(@NonNull View itemView,OnItemClickListener onItemClickListener) {
            super(itemView);
            teacherCardView = itemView.findViewById(R.id.teacherView);
            teacherName = itemView.findViewById(R.id.teacherName);
            staff_imageView = itemView.findViewById(R.id.staff_imageView);


            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);

            teacherCardView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
        onItemClickListener.onItemclick(getAdapterPosition());

        }
    }

    public interface OnItemClickListener {
        void onItemclick(int position);
    }



}
