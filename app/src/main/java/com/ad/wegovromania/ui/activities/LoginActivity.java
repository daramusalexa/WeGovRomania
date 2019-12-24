package com.ad.wegovromania.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.Report;
import com.ad.wegovromania.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private SharedPreferences mPreferences;

    private ProgressBar mProgressBar;
    private TextInputLayout mEmailTextInputLayout;
    private EditText mEmailEditText;
    private TextInputLayout mPasswordTextInputLayout;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private Button mRegisterRedirectButton;
    private CheckBox mRememberMeCheckBox;

    private static final String TAG = "Login Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mProgressBar = findViewById(R.id.progressBar);
        mEmailTextInputLayout = findViewById(R.id.emailTextInputLayout);
        mEmailEditText = findViewById(R.id.emailEditText);
        mPasswordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        mPasswordEditText = findViewById(R.id.passwordEditText);
        mLoginButton = findViewById(R.id.loginButton);
        mRegisterRedirectButton = findViewById(R.id.loginRedirectButton);
        mRememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);

        // Get login credentials from shared preferences
        getLoginCredentials();

        // When the user clicks the Login button
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input from login form
                final String email = mEmailEditText.getText().toString().trim();
                final String password = mPasswordEditText.getText().toString().trim();

                // Clear errors
                mEmailTextInputLayout.setError(null);
                mPasswordTextInputLayout.setError(null);

                // Check user input
                if (TextUtils.isEmpty(email)) {
                    mEmailTextInputLayout.setError(getString(R.string.email_required_error));
                    mEmailEditText.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mEmailTextInputLayout.setError(getString(R.string.email_format_error));
                    mEmailEditText.requestFocus();
                } else if (TextUtils.isEmpty(password)) {
                    mPasswordTextInputLayout.setError(getString(R.string.password_required_error));
                    mPasswordEditText.requestFocus();
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    // Login user
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success
                                        setLoginCredentials(email, password);

                                        mFirestore.collection("Users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document != null) {
                                                        if (document.exists()) {
                                                            // Check if user is enabled
                                                            boolean enabled = document.getBoolean("enabled");
                                                            if (!enabled) {
                                                                Toast.makeText(getApplicationContext(), getString(R.string.account_not_enabled),
                                                                        Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        } else {
                                                            Log.d(TAG, "No such document");
                                                        }
                                                    }
                                                } else {
                                                    Log.d(TAG, "get failed with ", task.getException());
                                                }
                                            }
                                        });
                                    } else {
                                        try {
                                            throw task.getException();
                                        }
                                        // If user enters wrong email.
                                        catch (FirebaseAuthInvalidUserException invalidEmail) {
                                            mEmailTextInputLayout.setError(getString(R.string.email_unavailable_error));
                                            mEmailEditText.requestFocus();
                                            Log.d(TAG, "onComplete: invalid_email");
                                        }
                                        // if user enters wrong password.
                                        catch (FirebaseAuthInvalidCredentialsException wrongPassword) {
                                            mPasswordTextInputLayout.setError(getString(R.string.wrong_password_error));
                                            mPasswordEditText.requestFocus();
                                            Log.d(TAG, "onComplete: wrong_password");
                                        } catch (Exception e) {
                                            // Otherwise, display a message to the user.
                                            Toast.makeText(getApplicationContext(), getString(R.string.authentication_failed),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    mProgressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                }
            }
        });

        // When the user clicks the Register Redirect button
        mRegisterRedirectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AccountTypeActivity.class);
                startActivity(intent);
            }
        });
    }

    // Uses shared preferences to fill user form
    public void getLoginCredentials() {
        boolean isChecked = mPreferences.getBoolean("checked", false);
        String email = mPreferences.getString(getString(R.string.user_email), "");
        String password = mPreferences.getString(getString(R.string.user_password), "");

        if (isChecked) {
            mRememberMeCheckBox.setChecked(true);
            mEmailEditText.setText(email);
            mPasswordEditText.setText(password);
        }
    }

    // Uses shared preferences to remember user login info
    public void setLoginCredentials(String email, String password) {
        if (mRememberMeCheckBox.isChecked()) {
            mPreferences.edit().putBoolean("checked", true).apply();
        } else {
            mPreferences.edit().putBoolean("checked", false).apply();
        }
        mPreferences.edit().putString(getString(R.string.user_email), email).apply();
        mPreferences.edit().putString(getString(R.string.user_password), password).apply();
    }

    // Don't let user go back
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        }

        return false;
    }
}
