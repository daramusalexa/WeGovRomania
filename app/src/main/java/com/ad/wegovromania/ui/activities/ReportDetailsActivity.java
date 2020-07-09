package com.ad.wegovromania.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.Report;
import com.ad.wegovromania.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportDetailsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private TextView mNameTextView;
    private TextInputLayout mResolutionTextInputLayout;
    private EditText mResolutionEditText;
    private Switch mStatusSwitch;
    private Button mSaveButton;
    private Button mCancelButton;

    private String mReportID;
    private Report mReport;

    private static final String TAG = "Report Details Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        mToolbar = findViewById(R.id.toolbar);

        // Configure Toolbar
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.reports));
        }
        mProgressBar = findViewById(R.id.progressBar);
        mNameTextView = findViewById(R.id.nameTextView);
        mResolutionTextInputLayout = findViewById(R.id.resolutionTextInputLayout);
        mResolutionEditText = findViewById(R.id.resolutionEditText);

        Intent intent = getIntent();
        mReportID = intent.getStringExtra("REPORT_ID");

        mAuth = FirebaseAuth.getInstance();
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
                    mReport.setStatus(Constants.ReportStatus.Solved);
                } else {
                    mReport.setStatus(Constants.ReportStatus.Pending);
                }
            }
        });

        // When user clicks the Save button
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String resolution = mResolutionEditText.getText().toString().trim();

                // Clear errors
                mResolutionTextInputLayout.setError(null);

                mProgressBar.setVisibility(View.VISIBLE);

                if (resolution.length() > 200) {
                    mResolutionTextInputLayout.setError(getString(R.string.report_body_length_error));
                    mResolutionEditText.requestFocus();
                } else {

                    mProgressBar.setVisibility(View.VISIBLE);

                    modifyReport(resolution);
                }
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

    // Inflate toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reports_menu, menu);
        return true;
    }

    // When the user clicks a button in the toolbar menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            // Go to MainActivity
            case R.id.mainPageButton:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            // Start the Account Activity
            case R.id.settingsButton:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            // Log out user
            case R.id.logoutButton:
                mAuth.signOut();
                // Start the Login Activity
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return false;
        }
    }

    // Load Report from Firestore
    public void loadReport() {
        mFirestore.collection("Reports").document(mReportID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                mReport = documentSnapshot.toObject(Report.class);
                if (mReport != null && mReport.getStatus().equals(Constants.ReportStatus.Solved)) {
                    mStatusSwitch.setChecked(true);
                }
                if (mReport != null) {
                    mResolutionEditText.setText(mReport.getResolution());
                }
                // Load user name
                if (mReport != null) {
                    fillUserName(mReport.getUserId());
                }
            }
        });
    }

    // Modify Report in Firestore
    public void modifyReport(final String resolution) {
        mReport.setResolution(resolution);
        mFirestore.collection("Reports").document(mReportID).set(mReport).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Intent intent = new Intent(getApplicationContext(), ReportsActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    public void fillUserName(final String userId) {
        mFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        if (document.exists()) {
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            mNameTextView.setText(String.format("%s %s", firstName, lastName));
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}
