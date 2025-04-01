package com.example.kasturi.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kasturi.EmergencyContactsActivity;
import com.example.kasturi.MainActivity;
import com.example.kasturi.R;
import com.example.kasturi.classes.EmergencyContact;
import com.example.kasturi.classes.EmergencyHandler;
import com.example.kasturi.classes.RecentAlertsAdapter;
import com.example.kasturi.classes.RecentAlert;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recentAlertsRecyclerView;
    private RecentAlertsAdapter recentAlertsAdapter;
    private MaterialCardView emergencyCard, safetyTipsCard, emergencyContactsCard, nearbyPoliceCard;
    private EmergencyHandler emergencyHandler;
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupEmergencyHandler();
        setupClickListeners();
        setupRecentAlerts();

        return view;
    }

    private void initializeViews(View view) {
        emergencyCard = view.findViewById(R.id.emergencyCard);
        safetyTipsCard = view.findViewById(R.id.safetyTipsCard);
        emergencyContactsCard = view.findViewById(R.id.btn_emergency_contacts);
        nearbyPoliceCard = view.findViewById(R.id.nearbyPoliceCard);
        recentAlertsRecyclerView = view.findViewById(R.id.recentAlertsRecyclerView);
    }

    private void setupEmergencyHandler() {
        // Load emergency contacts from database or shared preferences
        // For now using dummy data
        emergencyContacts.add(new EmergencyContact("Police", "100"));
        emergencyContacts.add(new EmergencyContact("Ambulance", "108"));

        emergencyHandler = new EmergencyHandler(getActivity(), emergencyContacts);
    }

    private void setupClickListeners() {
        emergencyCard.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Get current location - you'll need to implement this
                String currentLocation = "Current Location Placeholder";
                emergencyHandler.triggerEmergency(currentLocation);
            }
        });

        emergencyCard.setOnLongClickListener(v -> {
            emergencyHandler.cancelEmergency();
            showEmergencyStoppedMessage();

            // Add haptic feedback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            } else {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }

            return true;
        });

        safetyTipsCard.setOnClickListener(v -> {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).navigateToSafetyTips();
            }
        });

        nearbyPoliceCard.setOnClickListener(v -> {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).navigateToNearbyPolice();
            }
        });

        emergencyContactsCard.setOnClickListener(v -> navigateToEmergencyContacts());
    }

    private void showEmergencyStoppedMessage() {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), "Emergency stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToEmergencyContacts() {

        Intent intent = new Intent(getActivity(), EmergencyContactsActivity.class);
        startActivity(intent);
    }

    private void setupRecentAlerts() {
        List<RecentAlert> recentAlerts = new ArrayList<>();
        recentAlerts.add(new RecentAlert("Emergency alert sent", "2 minutes ago", R.drawable.ic_alert));
        recentAlerts.add(new RecentAlert("Location shared", "15 minutes ago", R.drawable.ic_location_shared));
        recentAlerts.add(new RecentAlert("Safe zone entered", "1 hour ago", R.drawable.ic_safe_zone));

        recentAlertsAdapter = new RecentAlertsAdapter(recentAlerts);
        recentAlertsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recentAlertsRecyclerView.setAdapter(recentAlertsAdapter);
    }

    @Override
    public void onDestroy() {
        if (emergencyHandler != null) {
            emergencyHandler.stopAlarm();
        }
        super.onDestroy();
    }
}