package com.example.kasturi.classes;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.kasturi.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmergencyHandler {
    private static final String CHANNEL_ID = "emergency_channel";
    private static final int NOTIFICATION_ID = 101;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final Context context;
    private MediaPlayer alarmPlayer;
    private List<EmergencyContact> emergencyContacts;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private String userId;
    private boolean isFetchingContacts = false;

    public EmergencyHandler(Context context, List<EmergencyContact> emergencyContacts) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.emergencyContacts = new ArrayList<>();
        createNotificationChannel();
    }

    public void triggerEmergency(String currentLocation) {
        // First fetch the latest contacts from Firestore
        fetchEmergencyContacts(() -> {
            // Once contacts are loaded, proceed with emergency
            getCurrentLocation(location -> {
                String locationString = (location != null) ?
                        "Lat: " + location.getLatitude() + ", Long: " + location.getLongitude() :
                        "Location unavailable";

                sendEmergencyAlert(locationString);
                playAlarmSound();
                openEmergencyDialer();
            });
        });
    }

    private void fetchEmergencyContacts(final ContactsFetchCallback callback) {
        if (isFetchingContacts) return;

        isFetchingContacts = true;
        db.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        isFetchingContacts = false;
                        if (documentSnapshot.exists() && documentSnapshot.contains("emergencyContacts")) {
                            List<Map<String, String>> contacts =
                                    (List<Map<String, String>>) documentSnapshot.get("emergencyContacts");
                            emergencyContacts.clear();
                            for (Map<String, String> contact : contacts) {
                                emergencyContacts.add(new EmergencyContact(
                                        contact.get("name"),
                                        contact.get("phone")
                                ));
                            }
                            Log.d("EmergencyHandler", "Successfully loaded " + emergencyContacts.size() + " contacts");
                        }
                        callback.onContactsFetched();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        isFetchingContacts = false;
                        Log.e("EmergencyHandler", "Failed to fetch emergency contacts", e);
                        callback.onContactsFetched(); // Still proceed even if contacts fetch fails
                    }
                });
    }

    private void getCurrentLocation(LocationCallback callback) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationTask = fusedLocationClient.getLastLocation();
            locationTask.addOnSuccessListener(location -> {
                if (location != null) {
                    callback.onLocationReceived(location);
                } else {
                    Log.w("EmergencyHandler", "Location is null, using fallback");
                    callback.onLocationReceived(null);
                }
            }).addOnFailureListener(e -> {
                Log.e("EmergencyHandler", "Error getting location", e);
                callback.onLocationReceived(null);
            });
        } else {
            Log.w("EmergencyHandler", "Location permission not granted");
            callback.onLocationReceived(null);
        }
    }

    private void sendEmergencyAlert(String location) {
        if (emergencyContacts.isEmpty()) {
            Log.w("EmergencyHandler", "No emergency contacts available");
            showEmergencyNotification("Emergency alert triggered but no contacts available");
            return;
        }

        String message = "EMERGENCY! I need help! ";
        message += location;

        // Check SMS permission before sending
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            for (EmergencyContact contact : emergencyContacts) {
                try {
                    smsManager.sendTextMessage(
                            contact.getPhone(),
                            null,
                            message,
                            null,
                            null
                    );
                    Log.d("EmergencyHandler", "SMS sent to: " + contact.getName() + " (" + contact.getPhone() + ")");
                } catch (Exception e) {
                    Log.e("EmergencyHandler", "Failed to send SMS to " + contact.getName(), e);
                }
            }
        } else {
            Log.e("EmergencyHandler", "SMS permission not granted");
        }

        showEmergencyNotification(message);
        logEmergencyEvent("Emergency alert sent", message);
    }

    private void showEmergencyNotification(String message) {
        try {
            Intent intent = new Intent(context, context.getClass());
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    NOTIFICATION_ID,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            // Create a full-screen intent for high-priority alerts
            PendingIntent fullScreenIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_alert)
                    .setContentTitle("EMERGENCY ALERT")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(Notification.CATEGORY_ALARM)
                    .setContentIntent(pendingIntent)
                    .setFullScreenIntent(fullScreenIntent, true)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                    .setVibrate(new long[]{1000, 1000, 1000, 1000});

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        } catch (Exception e) {
            Log.e("EmergencyHandler", "Error showing notification", e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Emergency Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for emergency notifications");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000});
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    Notification.AUDIO_ATTRIBUTES_DEFAULT);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void playAlarmSound() {
        try {
            // Release any existing MediaPlayer instance
            if (alarmPlayer != null) {
                alarmPlayer.release();
                alarmPlayer = null;
            }

            // Create MediaPlayer with custom sound from raw resources
            alarmPlayer = MediaPlayer.create(context, R.raw.alarm_sound);

            if (alarmPlayer != null) {
                // Set looping and start playing
                alarmPlayer.setLooping(true);
                alarmPlayer.setVolume(1.0f, 1.0f); // Max volume
                alarmPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e("EmergencyHandler", "MediaPlayer error: " + what + ", " + extra);
                    resetMediaPlayer();
                    return true;
                });
                alarmPlayer.start();
            } else {
                Log.e("EmergencyHandler", "Failed to create MediaPlayer");
                // Fallback to default alarm sound if custom fails
                playDefaultAlarmSound();
            }
        } catch (Exception e) {
            Log.e("EmergencyHandler", "Error playing alarm sound", e);
            // Fallback to default alarm sound
            playDefaultAlarmSound();
        }
    }

    private void playDefaultAlarmSound() {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            alarmPlayer = MediaPlayer.create(context, alarmUri);
            if (alarmPlayer != null) {
                alarmPlayer.setLooping(true);
                alarmPlayer.start();
            }
        } catch (Exception e) {
            Log.e("EmergencyHandler", "Error playing default alarm sound", e);
        }
    }

    private void resetMediaPlayer() {
        if (alarmPlayer != null) {
            try {
                alarmPlayer.release();
            } catch (Exception e) {
                Log.e("EmergencyHandler", "Error releasing MediaPlayer", e);
            }
            alarmPlayer = null;
        }
    }

    public void stopAlarm() {
        if (alarmPlayer != null) {
            try {
                alarmPlayer.stop();
                alarmPlayer.release();
            } catch (Exception e) {
                Log.e("EmergencyHandler", "Error stopping alarm", e);
            }
            alarmPlayer = null;
        }
    }

    private void openEmergencyDialer() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:911"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Log.e("EmergencyHandler", "No dialer app available");
            }
        } catch (Exception e) {
            Log.e("EmergencyHandler", "Error opening emergency dialer", e);
        }
    }

    private void logEmergencyEvent(String title, String details) {
        // Log to Firestore or analytics
        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("details", details);
        event.put("timestamp", System.currentTimeMillis());

        db.collection("users").document(userId)
                .collection("emergency_logs")
                .add(event)
                .addOnSuccessListener(documentReference ->
                        Log.d("EmergencyHandler", "Emergency event logged"))
                .addOnFailureListener(e ->
                        Log.e("EmergencyHandler", "Error logging emergency event", e));
    }

    public void cancelEmergency() {
        stopAlarm();
        cancelNotification();
    }

    private void cancelNotification() {
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(NOTIFICATION_ID);
        } catch (Exception e) {
            Log.e("EmergencyHandler", "Error canceling notification", e);
        }
    }

    public interface LocationCallback {
        void onLocationReceived(Location location);
    }

    public interface ContactsFetchCallback {
        void onContactsFetched();
    }
}