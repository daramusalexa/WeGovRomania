package com.ad.wegovromania.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.GovSystem;
import com.ad.wegovromania.util.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class GovSystemDetailsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;

    private TextInputLayout mEmailSubjectTextInputLayout;
    private EditText mEmailSubjectEditText;
    private TextInputLayout mEmailBodyTextInputLayout;
    private EditText mEmailBodyEditText;
    private Button mSubmitButton;

    private Switch mStatusSwitch;
    private Button mSaveButton;
    private Button mCancelButton;

    private String mGovSystemID;
    private GovSystem mGovSystem;

    private static final int GOV_SYSTEM_REQUEST = 1;
    private static final String TAG = "GovSystem Det. Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gov_system_details);

        mToolbar = findViewById(R.id.toolbar);

        // Configure Toolbar
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.gov_systems));
        }
        mProgressBar = findViewById(R.id.progressBar);

        mEmailSubjectTextInputLayout = findViewById(R.id.emailSubjectTextInputLayout);
        mEmailSubjectEditText = findViewById(R.id.emailSubjectEditText);
        mEmailBodyTextInputLayout = findViewById(R.id.emailBodyTextInputLayout);
        mEmailBodyEditText = findViewById(R.id.emailBodyEditText);
        mSubmitButton = findViewById(R.id.submitButton);

        mStatusSwitch = findViewById(R.id.statusSwitch);
        mSaveButton = findViewById(R.id.saveButton);
        mCancelButton = findViewById(R.id.cancelButton);

        Intent intent = getIntent();
        mGovSystemID = intent.getStringExtra("GOV_SYSTEM_ID");
        // If user is admin change layout
        if(Objects.equals(intent.getStringExtra("USER_TYPE"), "admin")) {
            mEmailSubjectTextInputLayout.setVisibility(View.INVISIBLE);
            mEmailSubjectEditText.setVisibility(View.INVISIBLE);
            mEmailBodyTextInputLayout.setVisibility(View.INVISIBLE);
            mEmailBodyEditText.setVisibility(View.INVISIBLE);
            mSubmitButton.setVisibility(View.INVISIBLE);

            mStatusSwitch.setVisibility(View.VISIBLE);
            mSaveButton.setVisibility(View.VISIBLE);
            mCancelButton.setVisibility(View.VISIBLE);
        }

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        loadGovSystem();

        mStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                if (mStatusSwitch.isChecked()) {
                    mGovSystem.setStatus(Constants.GovSystemsStatus.On);
                } else {
                    mGovSystem.setStatus(Constants.GovSystemsStatus.Off);
                }
            }
        });

        // When user clicks the Save button
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                modifyGovSystem();
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

        // When the user clicks the submit Button
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input from login form
                final String emailSubject = mEmailSubjectEditText.getText().toString().trim();
                final String emailBody = mEmailBodyEditText.getText().toString().trim();

                // Clear errors
                mEmailSubjectTextInputLayout.setError(null);
                mEmailBodyTextInputLayout.setError(null);

                // Check user input
                if (TextUtils.isEmpty(emailSubject)) {
                    mEmailSubjectTextInputLayout.setError(getString(R.string.email_subject_required_error));
                    mEmailSubjectEditText.requestFocus();
                } else if (TextUtils.isEmpty(emailBody)) {
                    mEmailBodyTextInputLayout.setError(getString(R.string.email_body_required_error));
                    mEmailBodyEditText.requestFocus();
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"alexa.daramus@gmail.com"}); // TODO
                    intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                    intent.putExtra(Intent.EXTRA_TEXT, emailBody);
                    try {
                        startActivityForResult(Intent.createChooser(intent, getString(R.string.sending_email)), GOV_SYSTEM_REQUEST);
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), getString(R.string.email_client_error), Toast.LENGTH_SHORT).show();
                    }
                }
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
        switch (item.getItemId()) {
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

    // Load Gov System from Firestore
    public void loadGovSystem() {
        mFirestore.collection("GovSystems").document(mGovSystemID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                mGovSystem = documentSnapshot.toObject(GovSystem.class);
                if (mGovSystem != null && mGovSystem.getStatus().equals(Constants.GovSystemsStatus.On)) {
                    mStatusSwitch.setChecked(true);
                }
            }
        });
    }

    // Modify Gov System in Firestore
    public void modifyGovSystem() {
        mFirestore.collection("GovSystems").document(mGovSystemID).set(mGovSystem).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Intent intent = new Intent(getApplicationContext(), GovSystemsActivity.class);
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

    // After sending email
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Location result
        if (requestCode == GOV_SYSTEM_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, GovSystemsActivity.class);
                startActivity(intent);
            }
        }
    }
}
