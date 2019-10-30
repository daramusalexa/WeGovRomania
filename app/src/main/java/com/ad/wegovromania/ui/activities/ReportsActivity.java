package com.ad.wegovromania.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ad.wegovromania.R;
import com.ad.wegovromania.ui.fragments.ActiveReportsFragment;
import com.ad.wegovromania.ui.fragments.SolvedReportsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ReportsActivity extends AppCompatActivity implements ActiveReportsFragment.OnFragmentInteractionListener, SolvedReportsFragment.OnFragmentInteractionListener {

    private FirebaseAuth mAuth;

    private Toolbar mToolbar;
    private BottomNavigationView bottomNavigationView;

    private ActiveReportsFragment activeRequestsFragment;
    private SolvedReportsFragment solvedRequestsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        activeRequestsFragment = new ActiveReportsFragment();
        solvedRequestsFragment = new SolvedReportsFragment();

        // Configure Toolbar
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.reports));
        }

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
            // Start the Add Report Activity
            case R.id.addReportButton:
                intent = new Intent(this, AddReportActivity.class);
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

