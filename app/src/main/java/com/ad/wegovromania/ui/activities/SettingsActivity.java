package com.ad.wegovromania.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.CityUser;
import com.ad.wegovromania.models.User;
import com.ad.wegovromania.util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mFirebaseUser;

    private SharedPreferences mPreferences;

    private ProgressBar mProgressBar;
    private Toolbar mToolbar;
    private TextInputLayout mFirstNameTextInputLayout;
    private EditText mFirstNameEditText;
    private TextInputLayout mLastNameTextInputLayout;
    private EditText mLastNameEditText;
    private TextInputLayout mPhoneTextInputLayout;
    private EditText mPhoneEditText;
    private TextInputLayout mEmailTextInputLayout;
    private EditText mEmailEditText;
    private TextInputLayout mCityTextInputLayout;
    private EditText mCityEditText;
    private Button mSaveButton;
    private Button mDeleteButton;

    private static String mCity = null;

    private static final String TAG = "Account Activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Configure Toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.account_settings));
        }

        mProgressBar = findViewById(R.id.progressBar);
        mFirstNameTextInputLayout = findViewById(R.id.firstNameTextInputLayout);
        mFirstNameEditText = findViewById(R.id.firstNameEditText);
        mLastNameTextInputLayout = findViewById(R.id.lastNameTextInputLayout);
        mLastNameEditText = findViewById(R.id.lastNameEditText);
        mPhoneTextInputLayout = findViewById(R.id.phoneTextInputLayout);
        mPhoneEditText = findViewById(R.id.phoneEditText);
        mEmailTextInputLayout = findViewById(R.id.emailTextInputLayout);
        mEmailEditText = findViewById(R.id.emailEditText);
        mCityTextInputLayout = findViewById(R.id.cityTextInputLayout);
        mCityEditText = findViewById(R.id.cityEditText);
        mSaveButton = findViewById(R.id.saveButton);
        mDeleteButton = findViewById(R.id.deleteButton);

        // Set user city from Firestore
        setUserCity();

        fillForm();

        // When user clicks the Register button
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input from form
                final String firstName = mFirstNameEditText.getText().toString().trim();
                final String lastName = mLastNameEditText.getText().toString().trim();
                final String phone = mPhoneEditText.getText().toString().trim();
                final String email = mEmailEditText.getText().toString().trim();
                final String city = mCityEditText.getText().toString().trim();

                // Clear errors
                mFirstNameTextInputLayout.setError(null);
                mLastNameTextInputLayout.setError(null);
                mPhoneTextInputLayout.setError(null);
                mEmailTextInputLayout.setError(null);
                mCityTextInputLayout.setError(null);

                // Check user input
                if (TextUtils.isEmpty(firstName)) {
                    mFirstNameTextInputLayout.setError(getString(R.string.first_name_required_error));
                    mFirstNameEditText.requestFocus();
                } else if (TextUtils.isEmpty(lastName)) {
                    mLastNameTextInputLayout.setError(getString(R.string.last_name_required_error));
                    mLastNameEditText.requestFocus();
                } else if (TextUtils.isEmpty(phone)) {
                    mPhoneTextInputLayout.setError(getString(R.string.phone_required_error));
                    mPhoneEditText.requestFocus();
                } else if (TextUtils.isEmpty(phone)) {
                    mPhoneTextInputLayout.setError(getString(R.string.phone_required_error));
                    mPhoneEditText.requestFocus();
                } else if (TextUtils.isEmpty(email)) {
                    mEmailTextInputLayout.setError(getString(R.string.email_required_error));
                    mEmailEditText.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mEmailTextInputLayout.setError(getString(R.string.email_format_error));
                    mEmailEditText.requestFocus();
                } else if (mCity != null && TextUtils.isEmpty(city)) {
                    mCityTextInputLayout.setError(getString(R.string.city_required_error));
                    mCityEditText.requestFocus();
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);

                    // Change user info in database
                    modifyUser(firstName, lastName, phone, email, city);
                }
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int option) {
                        switch (option) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                deleteUser();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new MaterialAlertDialogBuilder(SettingsActivity.this);
                builder.setTitle(getString(R.string.delete_account_message_title));
                builder.setMessage(getString(R.string.delete_account_message)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
            }
        });
    }

    // Inflate toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    // When the user clicks a button in the toolbar menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Log out user
            case R.id.logoutButton:
                mAuth.signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return false;
        }
    }

    public void setUserCity() {
        // Get user info from database
        mFirestore.collection("Users").document(mFirebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    mCity = documentSnapshot.getString("city");
                    // If city user show city field
                    if(mCity != null) {
                        mCityTextInputLayout.setVisibility(View.VISIBLE);
                        mCityEditText.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    // Get user data and fill form
    public void fillForm() {
        // Get user info from database
        mFirebaseUser = mAuth.getCurrentUser();
        mFirestore.collection("Users").document(mFirebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    mFirstNameEditText.setText(documentSnapshot.getString("firstName"));
                    mLastNameEditText.setText(documentSnapshot.getString("lastName"));
                    mPhoneEditText.setText(documentSnapshot.getString("phone"));
                    if(mCity != null) {
                        mCityEditText.setText(documentSnapshot.getString("city"));
                    }
                }

                mProgressBar.setVisibility(View.INVISIBLE);
                mSaveButton.setEnabled(true);
            }
        });

        // Get email from Authentication
        mEmailEditText.setText(mFirebaseUser.getEmail());
    }

    // Update user in Authentication / Firestore
    public void modifyUser(final String firstName, final String lastName, final String phone, final String email, final String city) {
        // Create new user object
        User user;
        if(mCity == null) {
            user = new User(firstName, lastName, phone);
        } else {
            user = new CityUser(firstName, lastName, phone, city);
        }

        // Update user in the Users collection
        if (mFirebaseUser != null) {
            mFirestore.collection("Users").document(mFirebaseUser.getUid()).set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
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

            // Get email and password form shared preferences
            String userEmail = mPreferences.getString(getString(R.string.user_email), ""); // user actual email
            String password = mPreferences.getString(getString(R.string.user_password), "");

            AuthCredential credential = EmailAuthProvider.getCredential(userEmail, password);

            // Re-authenticate user
            mFirebaseUser.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "User re-authenticated.");
                            // Update email in Authentication
                            mFirebaseUser.updateEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User email address updated.");
                                                // Update shared preferences
                                                mPreferences.edit().putString(getString(R.string.user_email), email).apply();
                                            } else {
                                                Log.w(TAG, "User email address not updated.");
                                                try {
                                                    throw task.getException();
                                                } catch (Exception e) {
                                                    // Display a message to the user.
                                                    Toast.makeText(getApplicationContext(), getString(R.string.update_failed),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    });
                        }
                    });
        }

        mProgressBar.setVisibility(View.INVISIBLE);

        // Start Main Activity
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    // Delete user from Firestore / Authentication
    public void deleteUser() {
        // Delete user from Firestore
        mFirestore.collection("Users").document(mFirebaseUser.getUid())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });

        // Delete user from Authentication
        mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            mFirebaseUser.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User account deleted.");
                            }
                        }
                    });
        }

        // Clear shared preferences
        mPreferences.edit().putBoolean("checked", false).apply();
        mPreferences.edit().putString(getString(R.string.user_email), "").apply();
        mPreferences.edit().putString(getString(R.string.user_password), "").apply();

        // Start Main Activity
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }
}