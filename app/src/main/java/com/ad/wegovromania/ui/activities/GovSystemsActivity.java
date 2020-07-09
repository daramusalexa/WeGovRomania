package com.ad.wegovromania.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.GovSystem;
import com.ad.wegovromania.ui.adapters.GovSystemsRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class GovSystemsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private GovSystemsRecyclerAdapter mGovSystemsRecyclerAdapter;
    private Toolbar mToolbar;

    private List<GovSystem> mGovSystems;
    private List<String> mGovSystemsIDs;

    private static final String TAG = "Gov Systems Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gov_systems);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mGovSystems = new ArrayList<>();
        mGovSystemsIDs = new ArrayList<>();

        mToolbar = findViewById(R.id.toolbar);
        // Configure Toolbar
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.gov_systems));
        }

        mProgressBar = findViewById(R.id.progressBar);
        mRecyclerView = findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mGovSystemsRecyclerAdapter = new GovSystemsRecyclerAdapter(mGovSystems);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mGovSystemsRecyclerAdapter);

        mProgressBar.setVisibility(View.VISIBLE);
        loadGovSystems();
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
        switch(item.getItemId()) {
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
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Load Gov Systems from CSV File
    public void loadGovSystems() {
        mGovSystems = new ArrayList<>();
        mGovSystemsIDs = new ArrayList<>();

        mFirestore.collection("GovSystems").orderBy("name").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Load users
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        mGovSystems.add(document.toObject(GovSystem.class));
                        mGovSystemsIDs.add(document.getId());
                    }
                    mGovSystemsRecyclerAdapter.updateGovSystems(mGovSystems, mGovSystemsIDs);
                    mProgressBar.setVisibility(View.INVISIBLE);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }
}
