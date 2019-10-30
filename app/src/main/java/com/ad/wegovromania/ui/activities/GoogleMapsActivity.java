package com.ad.wegovromania.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.Report;
import com.ad.wegovromania.ui.adapters.CustomInfoWindowAdapter;
import com.ad.wegovromania.util.Constants;
import com.ad.wegovromania.util.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class GoogleMapsActivity extends AppCompatActivity
        implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng mLocation;
    private Marker mSelector;

    private GoogleMap mMap;
    private ImageView mCenterMarker;
    private Button mSubmitButton;

    private List<Report> mReports;

    private static final String TAG = "Google Maps Activity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mCenterMarker = findViewById(R.id.map_pin);
        mSubmitButton = findViewById(R.id.submitButton);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocation = null;

        mReports = new ArrayList<>();

        loadReports();

        // When the user clicks the submit button
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the city clicked on
                List<Address> addresses = Utils.getAdresses(mLocation, GoogleMapsActivity.this);
                String countryName = null;
                String cityName = null;
                if (addresses != null) {
                    countryName = addresses.get(0).getCountryName();
                    cityName = addresses.get(0).getLocality();
                } else {
                    Toast.makeText(GoogleMapsActivity.this, getString(R.string.location_selection_failed), Toast.LENGTH_SHORT).show();
                }

                // If in city limits send location to Add Report Activity
                if (countryName.equals(Constants.COUNTRY)) {
                    Intent intent = new Intent(GoogleMapsActivity.this, AddReportActivity.class);
                    intent.putExtra("LATITUDE", String.valueOf(mLocation.latitude));
                    intent.putExtra("LONGITUDE", String.valueOf(mLocation.longitude));
                    intent.putExtra("CITY", cityName);
                    intent.putExtra("ADDRESS", addresses.get(0).getAddressLine(0));
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(GoogleMapsActivity.this, getString(R.string.out_of_country_bounds), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Enables the My Location layer if the fine location permission has been granted.
    public void enableLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    // Center camera on user location
    public void centerCameraOnUser() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                                    .zoom(17)                   // Sets the zoom
                                    .build();                   // Creates a CameraPosition from the builder
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set a preference for minimum and maximum zoom.
        mMap.setMinZoomPreference(10);
        mMap.setMaxZoomPreference(20);

        // Get camera position
        mLocation = mMap.getCameraPosition().target;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        enableLocation();
        centerCameraOnUser();

        // Add center marker
        mSelector = mMap.addMarker(new MarkerOptions()
                .position(mMap.getCameraPosition().target).icon(Utils.toBitmap(GoogleMapsActivity.this, R.drawable.ic_pin))
                .draggable(true));

        // Hide center marker when moving
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                mCenterMarker.setVisibility(View.VISIBLE);
                mSelector.setVisible(false);
            }
        });

        // Get center coordinates
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                mLocation = mMap.getCameraPosition().target;
                mSelector.setVisible(true);
                mSelector.setPosition(mMap.getCameraPosition().target);
                mCenterMarker.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    mMap.setMyLocationEnabled(true);
                    centerCameraOnUser();
                } else {
                    // permission denied! // TODO
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    // Load reports from Firestore and display markers on the map
    public void loadReports() {
        mFirestore.collection("Reports").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Load reports
                        mReports = task.getResult().toObjects(Report.class);
                        // Display makers
                        GeoPoint geoPoint = document.getGeoPoint("location");
                        mLocation = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(mLocation)
                                .title("Sesizare"));

                        CustomInfoWindowAdapter customInfoWindowAdapter = new CustomInfoWindowAdapter(GoogleMapsActivity.this);
                        mMap.setInfoWindowAdapter(customInfoWindowAdapter);

                        Report report = new Report(geoPoint, document.getString("type"), document.getString("city"), document.getString("reportBody"), mAuth.getCurrentUser().getUid());
                        report.setTimestamp(document.getDate("timestamp"));
                        report.setImages((ArrayList<String>)document.get("images"));
                        marker.setTag(report);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
                Log.e(TAG, mReports.toString());
            }
        });
    }

    // Functions that need to be implemented
    // but not necessary to do anything
    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
}
