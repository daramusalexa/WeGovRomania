package com.ad.wegovromania.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.Report;
import com.ad.wegovromania.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ReportDetailsActivity extends AppCompatActivity {

    private static final String TAG = "Report Details Activity";
    private FirebaseFirestore mFirestore;
    private Switch mStatusSwitch;
    private Button mSaveButton;
    private Button mCancelButton;

    private String mReportID;
    private Report mReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        Intent intent = getIntent();
        mReportID = intent.getStringExtra("REPORT_ID");

        mFirestore = FirebaseFirestore.getInstance();

        mStatusSwitch = findViewById(R.id.statusSwitch);
        mSaveButton = findViewById(R.id.saveButton);
        mCancelButton = findViewById(R.id.cancelButton);

        loadReport();

        mStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                if (mStatusSwitch.isChecked()) {
                    mReport.setStatus(Constants.Status.Solved);
            } else {
                    mReport.setStatus(Constants.Status.Pending);
                }
            }
        });

        // When user clicks the Save button
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                mFirestore.collection("Reports").document(mReportID).set(mReport).
                        addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                Intent intent = new Intent(view.getContext(), ReportsActivity.class);
                                view.getContext().startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });
            }
        });

        // When user clicks the Cancel button
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ReportsActivity.class);
                view.getContext().startActivity(intent);
            }
        });
    }

    public void loadReport() {
        mFirestore.collection("Reports").document(mReportID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                mReport = documentSnapshot.toObject(Report.class);
                if (mReport.getStatus().equals(Constants.Status.Solved)) {
                    mStatusSwitch.setChecked(true);
                }
            }
        });



    }
}
