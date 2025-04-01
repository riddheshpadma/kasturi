package com.example.kasturi.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.kasturi.ForgotPasswordActivity;
import com.example.kasturi.LoginActivty;
import com.example.kasturi.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ShapeableImageView ivProfile;
    private TextInputEditText etName, etEmail, etPhone;

    // Firestore variables
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        ivProfile = view.findViewById(R.id.ivProfile);
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);

        // Set click listeners
        view.findViewById(R.id.btnSave).setOnClickListener(v -> saveProfile());
        view.findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ForgotPasswordActivity.class));
        });
        view.findViewById(R.id.sign_out_button).setOnClickListener(v -> showSignOutConfirmation());

        // Load user data from Firestore
        loadUserData();
    }

    private void showSignOutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> performSignOut())
                .setNegativeButton("No", null)
                .show();
    }

    private void performSignOut() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // If using Google Sign-In, also sign out from Google
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireActivity(),
                GoogleSignInOptions.DEFAULT_SIGN_IN);
        googleSignInClient.signOut().addOnCompleteListener(task -> redirectToLogin());
    }

    private void redirectToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivty.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void loadUserData() {
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Set basic user info
                    etName.setText(document.getString("name"));
                    etEmail.setText(document.getString("email"));
                    etPhone.setText(document.getString("phone"));
                } else {
                    Toast.makeText(requireContext(), "User document doesn't exist", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name cannot be empty");
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone cannot be empty");
            return;
        }

        // Update profile in Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Profile saved successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save profile", Toast.LENGTH_SHORT).show());
    }
}