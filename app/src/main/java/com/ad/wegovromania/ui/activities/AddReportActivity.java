package com.ad.wegovromania.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.Report;
import com.ad.wegovromania.util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class AddReportActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private StorageReference mStorageRef;

    private Toolbar mToolbar;
    private LatLng mLocation;
    private ProgressBar mProgressBar;
    private Button mAddLocationButton;
    private TextView mDateTextView;
    private Spinner mSpinner;
    private TextView mLocationTextView;
    private TextInputLayout mReportBodyInputLayout;
    private EditText mReportBodyEditText;
    private ImageView[] mImageViews;
    private Button mSubmitButton;

    private String mType;
    private String mCity;
    private ArrayList<Uri> mImageUris;
    private ImageView lastClickedImageView;
    private ArrayList<String> images;
    private int mCounter;

    private static final int ADD_LOCATION_REQUEST = 1;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;
    private static final String TAG = "Add Report Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mToolbar = findViewById(R.id.toolbar);

        // Configure Toolbar
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.add_report));
        }

        mImageUris = new ArrayList<>();
        images = new ArrayList<>();
        mCounter = 0;

        mProgressBar = findViewById(R.id.progressBar);
        mDateTextView = findViewById(R.id.dateTimeTextView);
        mSpinner = findViewById(R.id.spinner);
        mLocationTextView = findViewById(R.id.locationTextView);
        mAddLocationButton = findViewById(R.id.addLocationButton);
        mReportBodyEditText = findViewById(R.id.reportBodyEditText);
        mReportBodyInputLayout = findViewById(R.id.reportBodyTextInputLayout);
        mImageViews = new ImageView[Constants.REPORT_IMAGEVIEWS_NUMBER];
        mImageViews[0] = findViewById(R.id.imageView1);
        mImageViews[1] = findViewById(R.id.imageView2);
        mImageViews[2] = findViewById(R.id.imageView3);
        mSubmitButton = findViewById(R.id.submitButton);

        // Show current date and time
        String currentDate = DateFormat.format("MM/dd/yyyy HH:mm", new Date()).toString();
        mDateTextView.setText(currentDate);

        // Set up report type spinner
        mSpinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
                R.array.report_type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);

        // When user clicks the Add Location button
        mAddLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddReportActivity.this, GoogleMapsActivity.class);
                startActivityForResult(intent, ADD_LOCATION_REQUEST);
            }
        });

        // When user clicks on the Image Views
        for (final ImageView imageView : mImageViews) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enableImageCrop();
                    lastClickedImageView = imageView;
                }
            });
        }

        // When user clicks the Submit button
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String requestBody = mReportBodyEditText.getText().toString().trim();

                // Clear errors
                mReportBodyInputLayout.setError(null);

                // Check user
                if (mLocation == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.location_not_selected),
                            Toast.LENGTH_SHORT).show();

                } else if(mType.equals(getResources().getStringArray(R.array.report_type_array)[0])) {
                    TextView spinnerError = (TextView) mSpinner.getSelectedView();
                    spinnerError.setError("");
                    Toast.makeText(getApplicationContext(), getString(R.string.type_not_selected_error),
                            Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(requestBody)) {
                    mReportBodyInputLayout.setError(getString(R.string.report_body_required_error));
                    mReportBodyEditText.requestFocus();
                } else if (requestBody.length() > 200) {
                    mReportBodyInputLayout.setError(getString(R.string.report_body_length_error));
                    mReportBodyEditText.requestFocus();
                } else {

                    mProgressBar.setVisibility(View.VISIBLE);

                    // Create GeoPoint for location
                    GeoPoint location = new GeoPoint(mLocation.latitude, mLocation.longitude);

                    addReport(location, requestBody);
                }
            }
        });
    }

    // When user selects a report type from the spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        mType = parent.getItemAtPosition(position).toString();
    }
    public void onNothingSelected(AdapterView<?> arg0) {

    }

    // After Google Maps Activity or Crop Image returns the location
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Location result
        if (requestCode == ADD_LOCATION_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mLocation = new LatLng(Float.parseFloat(data.getStringExtra("LATITUDE")), Float.parseFloat(data.getStringExtra("LONGITUDE")));
                mLocationTextView.setText(data.getStringExtra("ADDRESS"));
                mCity = data.getStringExtra("CITY");

                mAddLocationButton.setText(getString(R.string.change_location));
            }
        }

        // Crop result
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUris.add(result.getUri());
                lastClickedImageView.setImageURI(result.getUri());
                lastClickedImageView.setEnabled(false);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError(); // TODO
            }
        }
    }

    // Inflate toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_report_menu, menu);
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
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return false;
        }
    }

    // Asks the user for accessing external storage
    public void enableImageCrop() {
        // Read permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        }
        // Write permission
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
    }

    // For adding the report only once, regardless of the number of images selected by the user
    public void writeReportToFirebase(final GeoPoint location, final String reportBody) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        // Create new report
        assert firebaseUser != null;
        Report report = new Report(location, mType, mCity, reportBody, firebaseUser.getUid());
        report.setImages(images);

        // Add report in the Reports collection
        mFirestore.collection("Reports").document().set(report)
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

    // Add report to Firestore
    public void addReport(final GeoPoint location, final String reportBody) {

        if(lastClickedImageView != null) {

            for (Uri imageUri : mImageUris) {
                // Add images to Firebase Storage
                File file = new File(imageUri.getPath());
                final StorageReference imageRef = mStorageRef.child("report_images/" + file.getName());
                UploadTask uploadTask = imageRef.putFile(imageUri);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        // Continue with the task to get the download URL
                        return imageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            // Get image Uri from Firebase Storage
                            Uri downloadUri = task.getResult();
                            images.add(downloadUri.toString());

                            // Write to database on last iteration
                            if(mCounter++ == mImageUris.size() - 1){
                                writeReportToFirebase(location, reportBody);
                            }
                        }
                    }
                });
            }

            Toast.makeText(getApplicationContext(), getString(R.string.report_sent),
                    Toast.LENGTH_SHORT).show();
            // Start Main Activity
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.images_not_selected),
                    Toast.LENGTH_SHORT).show();
        }

        mProgressBar.setVisibility(View.INVISIBLE);
    }

}

