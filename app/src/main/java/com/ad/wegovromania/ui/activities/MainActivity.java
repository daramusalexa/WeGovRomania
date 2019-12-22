package com.ad.wegovromania.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.GovSystem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mFirebaseUser;

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private Button mReportsButton;
    private Button mGovSystemsButton;
    private Button mUsersButton;
    private Button mLoadGovSystemsButton;
    private Button mAddReportButton;

    private static final String TAG = "Main Activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();

        mToolbar = findViewById(R.id.toolbar);
        mProgressBar = findViewById(R.id.progressBar);
        mReportsButton = findViewById(R.id.reportsButton);
        mGovSystemsButton = findViewById(R.id.govSystemsButton);
        mUsersButton = findViewById(R.id.usersButton);
        mLoadGovSystemsButton = findViewById(R.id.loadGovSystemsButton);
        mAddReportButton = findViewById(R.id.addReportButton);

        // Configure Toolbar
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        // Get user info from database
        if (mFirebaseUser != null) {
            mFirestore.collection("Users").document(mFirebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null) {
                        // If user city is not null show Add Report Button
                        String city = documentSnapshot.getString("city");
                        if (city == null) {
                            mAddReportButton.setVisibility(View.VISIBLE);
                        }
                        // If user is admin show Users Button
                        if (documentSnapshot.getBoolean("admin") != null) {
                            boolean admin = documentSnapshot.getBoolean("admin");
                            if (admin) {
                                mUsersButton.setVisibility(View.VISIBLE);
                                mLoadGovSystemsButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            });
        }


        // When user clicks the Reports Button
        mReportsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ReportsActivity.class);
                startActivity(intent);
            }
        });

        // When user clicks the Load GovSystems Button
        mGovSystemsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GovSystemsActivity.class);
                startActivity(intent);
            }
        });

        // When user clicks the Users Button
        mUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(intent);
            }
        });

        // When user clicks the Load GovSystems Button
        mLoadGovSystemsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Delete all documents from Gov Systems collections
                mFirestore.collection("GovSystems").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Load users
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                mFirestore.collection("GovSystems").document(document.getId())
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
                            }

                            // Get Gov Systems data from csv to Firestore
                            InputStreamReader inputStreamReader = new InputStreamReader(getResources().openRawResource(R.raw.gov_systems), Charset.forName("UTF-8"));
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                            try {
                                GovSystem govSystem;
                                String line;
                                bufferedReader.readLine();
                                while ((line = bufferedReader.readLine()) != null) {
                                    mProgressBar.setVisibility(View.VISIBLE);
                                    mLoadGovSystemsButton.setEnabled(false);
                                    String[] rowData = line.split(",");
                                    if (rowData.length > 8) {
                                        govSystem = new GovSystem(rowData[1], rowData[5], rowData[6], rowData[7]);
                                        // Add report in the Reports collection
                                        mFirestore.collection("GovSystems").document().set(govSystem)
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
                                    }
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    mLoadGovSystemsButton.setEnabled(true);
                                    Toast.makeText(getApplicationContext(), getString(R.string.finished_loading_csv),
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            } finally {
                                try {
                                    inputStreamReader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
            }
        });


        // When user clicks the Add Report Button
        mAddReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddReportActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is not signed in
        if (mFirebaseUser == null) {
            sendToLogin();
        }
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
                sendToLogin();
                return true;
            default:
                return false;
        }
    }

    // Start the Login Activity
    public void sendToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

