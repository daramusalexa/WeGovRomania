package com.ad.wegovromania.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ad.wegovromania.R;
import com.ad.wegovromania.ui.fragments.ActiveReportsFragment;
import com.ad.wegovromania.ui.fragments.SolvedReportsFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportsActivity extends AppCompatActivity implements ActiveReportsFragment.OnFragmentInteractionListener, SolvedReportsFragment.OnFragmentInteractionListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mFirebaseUser;

    private Toolbar mToolbar;
    private Button mAddReportButton;
    private BottomNavigationView bottomNavigationView;

    private ActiveReportsFragment activeRequestsFragment;
    private SolvedReportsFragment solvedRequestsFragment;

    private static String mCity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();

        mToolbar = findViewById(R.id.toolbar);
        mAddReportButton = findViewById(R.id.addReportButton);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        activeRequestsFragment = new ActiveReportsFragment();
        solvedRequestsFragment = new SolvedReportsFragment();


        // Configure Toolbar
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.reports));
        }

        // If user city is not null show Add Report Button
        mFirestore.collection("Users").document(mFirebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    mCity = documentSnapshot.getString("city");
                    if(mCity == null) {
                        mAddReportButton.setVisibility(View.VISIBLE);
                    }
                }
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

        // Set main fragment
        setFragment(activeRequestsFragment);

        // Change the fragment when user clicks the bottom navigation buttons
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.activeRequestsButton:
                        setFragment(activeRequestsFragment);
                        return true;
                    case R.id.solvedRequestsButton:
                        setFragment(solvedRequestsFragment);
                        return true;
                    default:
                        return false;
                }
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
        switch(item.getItemId()) {
            case R.id.settingsButton:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            // Log out user
            case R.id.logoutButton:
                mAuth.signOut();
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty
    }
}

