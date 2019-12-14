package com.ad.wegovromania.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.User;
import com.ad.wegovromania.ui.adapters.UserRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private UserRecyclerAdapter mUserRecyclerAdapter;
    private List<User> mUsers;
    private List<String> mUserIDs;

    private Toolbar mToolbar;

    private static final String TAG = "Users Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mToolbar = findViewById(R.id.toolbar);

        mUsers = new ArrayList<>();
        mUserIDs = new ArrayList<>();

        // Configure Toolbar
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.users));
        }

        mProgressBar = findViewById(R.id.progressBar);
        mRecyclerView = findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mUserRecyclerAdapter = new UserRecyclerAdapter(mUsers);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mUserRecyclerAdapter);

        mProgressBar.setVisibility(View.VISIBLE);
        loadUsers();

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
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    // Load enabled users from Firestore
    public void loadUsers() {
        mUsers = new ArrayList<>();
        mUserIDs = new ArrayList<>();

        mFirestore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Load users
                    for (QueryDocumentSnapshot document : task.getResult()) {
                            mUsers = task.getResult().toObjects(User.class);
                            mUserIDs.add(document.getId());
                    }
                    mUserRecyclerAdapter.updateUsers(mUsers, mUserIDs);
                    mProgressBar.setVisibility(View.INVISIBLE);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
                Log.e(TAG, mUsers.toString());
            }
        });
    }
}
