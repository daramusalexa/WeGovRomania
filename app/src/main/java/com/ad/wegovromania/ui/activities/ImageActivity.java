package com.ad.wegovromania.ui.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.ad.wegovromania.R;

public class ImageActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageView = findViewById(R.id.imageView);

        // Get bitmap from Reports Activity
        Bundle extras = getIntent().getExtras();
        Bitmap bitmap = extras.getParcelable("IMAGE");

        imageView.setImageBitmap(bitmap);

    }
}

