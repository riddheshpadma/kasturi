package com.example.kasturi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.kasturi.fragment.ChatFragment;
import com.example.kasturi.fragment.HomeFragment;
import com.example.kasturi.fragment.MapFragment;
import com.example.kasturi.fragment.ProfileFragment;
import com.example.kasturi.fragment.SafetyTipsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 101;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS
    };

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);


        // Check and request permissions on startup
        checkAndRequestPermissions();

        // Bottom navigation setup
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_map) {
                selectedFragment = new MapFragment();
                if (!hasLocationPermission()) {
                    requestLocationPermission();
                }
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (id == R.id.nav_chatbot) {
                selectedFragment = new ChatFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    private void checkAndRequestPermissions() {
        if (!hasAllRequiredPermissions()) {
            if (shouldShowPermissionRationale()) {
                showPermissionRationaleDialog();
            } else {
                requestAllPermissions();
            }
        }
    }

    private boolean hasAllRequiredPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean shouldShowPermissionRationale() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Needed")
                .setMessage("This app needs permissions to function properly. Please grant them for full functionality.")
                .setPositiveButton("Continue", (dialog, which) -> requestAllPermissions())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "Some features may not work without permissions", Toast.LENGTH_LONG).show();
                })
                .show();
    }

    private void requestAllPermissions() {
        ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
        );
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_CODE_PERMISSIONS
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (hasAllRequiredPermissions()) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                showPartialPermissionGrantedMessage();
            }
        }
    }

    private void showPartialPermissionGrantedMessage() {
        boolean hasLocation = hasLocationPermission();

        StringBuilder message = new StringBuilder("Some features may not work:");
        if (!hasLocation) {
            message.append("\n- Location-based features");
        }

        Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show();
    }

    // Add these methods to your MainActivity class
    public void triggerEmergency() {
        // Implement your emergency protocol here
        Toast.makeText(this, "Emergency triggered!", Toast.LENGTH_SHORT).show();

        // You might want to:
        // 1. Send emergency alerts
        // 2. Share location with contacts
        // 3. Play an alarm sound
        // 4. Open emergency dialer
    }

    public void navigateToSafetyTips() {
        // Navigate to a fragment showing safety tips
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SafetyTipsFragment())
                .addToBackStack(null)
                .commit();
    }

    public void navigateToNearbyPolice() {
        startActivity(new Intent(this, NearbyPoliceActivity.class));
    }


}