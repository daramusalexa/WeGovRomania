package com.ad.wegovromania.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.ad.wegovromania.R;
import com.ad.wegovromania.util.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;

    private Switch mStatusSwitch;

    private static final String TAG = "Report Details Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        mFirestore = FirebaseFirestore.getInstance();

        mStatusSwitch = findViewById(R.id.statusSwitch);

        mStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                Intent intent = getIntent();
                String reportID = intent.getStringExtra("REPORT_ID");
//                if (mStatusSwitch.isChecked()) {
                mFirestore.collection("Reports").document(reportID).update("status", Constants.Status.Solved).
                        addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });
            }
//            }
        });
    }
}
