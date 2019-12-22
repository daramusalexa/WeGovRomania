package com.ad.wegovromania.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.GovSystem;
import com.ad.wegovromania.models.Report;
import com.ad.wegovromania.util.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

    private String mGovSystemID;
    private GovSystem mGovSystem;

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

        Intent intent = getIntent();
        mGovSystemID = intent.getStringExtra("GOV_SYSTEM_ID");

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        loadGovSystem();

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

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{"gavrilam16@gmail.com"});
                    i.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                    i.putExtra(Intent.EXTRA_TEXT, emailBody);
                    try {
                        startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
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
            }
        });
    }

}
