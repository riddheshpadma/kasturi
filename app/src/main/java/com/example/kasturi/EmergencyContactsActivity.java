package com.example.kasturi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kasturi.classes.EmergencyContact;
import com.example.kasturi.classes.EmergencyContactAdapter;
import com.example.kasturi.fragment.ProfileFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmergencyContactsActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private RecyclerView rvEmergencyContacts;
    private EmergencyContactAdapter adapter;
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();
    private ActivityResultLauncher<Intent> contactPickerLauncher;
    private FirebaseFirestore db;
    private String userId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set click listener for back button
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize views
        rvEmergencyContacts = findViewById(R.id.rvEmergencyContacts);
        MaterialButton btnAddContact = findViewById(R.id.btnAddEmergencyContact);

        // Set up RecyclerView
        rvEmergencyContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmergencyContactAdapter(emergencyContacts, this::removeEmergencyContact);
        rvEmergencyContacts.setAdapter(adapter);

        // Initialize contact picker launcher
        contactPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        handleSelectedContact(result.getData());
                    }
                });

        // Set click listeners
        btnAddContact.setOnClickListener(v -> checkContactsPermission());

        // Load existing contacts
        loadEmergencyContacts();
    }



    private void checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            openContactsPicker();
        }
    }

    private void openContactsPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        contactPickerLauncher.launch(intent);
    }

    private void handleSelectedContact(Intent data) {
        Uri contactUri = data.getData();
        if (contactUri == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            queryContactNewApi(contactUri);
        } else {
            queryContactLegacyApi(contactUri);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void queryContactNewApi(Uri contactUri) {
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        try (Cursor cursor = getContentResolver().query(
                contactUri,
                projection,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String name = cursor.getString(nameIndex);
                String phone = cursor.getString(numberIndex);

                addContactToEmergencyList(name, phone);
            }
        }
    }

    private void queryContactLegacyApi(Uri contactUri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    contactUri,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                Cursor phoneCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id},
                        null
                );

                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    @SuppressLint("Range") String name = phoneCursor.getString(phoneCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    @SuppressLint("Range") String phone = phoneCursor.getString(phoneCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));

                    addContactToEmergencyList(name, phone);
                }

                if (phoneCursor != null) {
                    phoneCursor.close();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void addContactToEmergencyList(String name, String phone) {
        String formattedPhone = phone.replaceAll("[^0-9+]", "");
        emergencyContacts.add(new EmergencyContact(name, formattedPhone));
        adapter.notifyItemInserted(emergencyContacts.size() - 1);
        updateEmergencyContactsInFirestore();
        Toast.makeText(this, "Added " + name + " to emergency contacts", Toast.LENGTH_SHORT).show();
    }

    private void removeEmergencyContact(int position) {
        emergencyContacts.remove(position);
        adapter.notifyItemRemoved(position);
        updateEmergencyContactsInFirestore();
    }

    private void updateEmergencyContactsInFirestore() {
        List<Map<String, String>> contactsToSave = new ArrayList<>();
        for (EmergencyContact contact : emergencyContacts) {
            Map<String, String> contactMap = new HashMap<>();
            contactMap.put("name", contact.getName());
            contactMap.put("phone", contact.getPhone());
            contactsToSave.add(contactMap);
        }

        db.collection("users").document(userId)
                .update("emergencyContacts", contactsToSave)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Contacts updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update contacts", Toast.LENGTH_SHORT).show());
    }

    private void loadEmergencyContacts() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("emergencyContacts")) {
                        List<Map<String, String>> contacts = (List<Map<String, String>>) documentSnapshot.get("emergencyContacts");
                        emergencyContacts.clear();
                        for (Map<String, String> contact : contacts) {
                            emergencyContacts.add(new EmergencyContact(
                                    contact.get("name"),
                                    contact.get("phone")
                            ));
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load contacts", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContactsPicker();
            } else {
                Toast.makeText(this, "Permission needed to access contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }
}