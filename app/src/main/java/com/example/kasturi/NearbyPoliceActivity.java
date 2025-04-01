package com.example.kasturi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

public class NearbyPoliceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ProgressBar progressBar;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_police);

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        findViewById(R.id.back_button).setOnClickListener(v -> onBackPressed());

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        progressBar.setVisibility(View.VISIBLE);
        Task<Location> locationResult = fusedLocationClient.getLastLocation();
        locationResult.addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                findNearbyPoliceStations(currentLatLng);
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findNearbyPoliceStations(LatLng location) {
        // In a real app, you would use Google Places API here
        // For demo purposes, we'll add some dummy police stations near the user

        // Clear existing markers
        mMap.clear();

        // Add current location marker
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Your Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // Add dummy police stations (in real app, get these from Places API)
        addDummyPoliceStations(location);

        progressBar.setVisibility(View.GONE);
    }

    private void addDummyPoliceStations(LatLng center) {
        // Add 5 dummy police stations within 2km radius
        double lat = center.latitude;
        double lng = center.longitude;

        // Police station 1 (500m north-east)
        LatLng ps1 = new LatLng(lat + 0.0045, lng + 0.0045);
        mMap.addMarker(new MarkerOptions()
                .position(ps1)
                .title("Police Station 1")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Police station 2 (800m south-west)
        LatLng ps2 = new LatLng(lat - 0.0072, lng - 0.0072);
        mMap.addMarker(new MarkerOptions()
                .position(ps2)
                .title("Police Station 2")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Police station 3 (1.2km north)
        LatLng ps3 = new LatLng(lat + 0.0108, lng);
        mMap.addMarker(new MarkerOptions()
                .position(ps3)
                .title("Police Station 3")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Police station 4 (1.5km east)
        LatLng ps4 = new LatLng(lat, lng + 0.0135);
        mMap.addMarker(new MarkerOptions()
                .position(ps4)
                .title("Police Station 4")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Police station 5 (1.8km south-east)
        LatLng ps5 = new LatLng(lat - 0.0081, lng + 0.0081);
        mMap.addMarker(new MarkerOptions()
                .position(ps5)
                .title("Police Station 5")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}