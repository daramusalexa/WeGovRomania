package com.ad.wegovromania.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ad.wegovromania.R;

public class AccountTypeActivity extends AppCompatActivity {

    private Button mUserButton;
    private Button mCityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_type);

        mUserButton = findViewById(R.id.userButton);
        mCityButton = findViewById(R.id.cityButton);

        // When user clicks the User button
        mUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                intent.putExtra("ACCOUNT_TYPE", "CITIZEN");
                startActivity(intent);
            }
        });

        // When user clicks the City button
        mCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                intent.putExtra("ACCOUNT_TYPE", "CITY");
                startActivity(intent);
            }
        });
    }
}
