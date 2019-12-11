package com.ad.wegovromania.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ReportDetailsActivity extends AppCompatActivity {

    private static final String TAG = "Report Details Activity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private TextInputLayout mResolutionTextInputLayout;
    private EditText mResolutionEditText;
    private Switch mStatusSwitch;
    private Button mSaveButton;
    private Button mCancelButton;

    private String mReportID;
    private Report mReport;

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
                final String resolution = mResolutionEditText.getText().toString().trim();

                // Clear errors
                mResolutionTextInputLayout.setError(null);


                mProgressBar.setVisibility(View.VISIBLE);

                if (resolution.length() > 200) {
                    mResolutionTextInputLayout.setError(getString(R.string.report_body_length_error));
                    mResolutionEditText.requestFocus();
                } else {

                    mProgressBar.setVisibility(View.VISIBLE);

                    modifyReport(view, resolution);
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
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // When the user clicks a button in the toolbar menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
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
                return true;
            default:
                return false;
        }
    }

    // Load report from Firestore
    public void loadReport() {
        mFirestore.collection("Reports").document(mReportID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                mReport = documentSnapshot.toObject(Report.class);
                if (mReport.getStatus().equals(Constants.Status.Solved)) {
                    mStatusSwitch.setChecked(true);
                }
                mResolutionEditText.setText(mReport.getResolution());
            }
        });
    }

    // Modify report in Firestore
    public void modifyReport(final View view, String resolution) {
        mReport.setResolution(resolution);
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
        mProgressBar.setVisibility(View.INVISIBLE);
    }
}
