# SafetyKasturi - Emergency Safety App
📌 Overview
SafetyKasturi is an Android application designed to provide emergency assistance, safety tips, and quick access to emergency contacts and nearby police stations. The app integrates Firebase for authentication and data storage, Google Maps for location services, and a chatbot for safety guidance.

# Features

✅ User Authentication

    Email/Password login & registration

    Google Sign-In

    Password reset functionality

✅ Emergency Features

    Trigger emergency alerts

    Share location with emergency contacts

    Find nearby police stations

✅ Safety Tools

    Interactive chatbot for safety tips

    Emergency contact management

    Recent alerts history

✅ Location Services

    Real-time location tracking

    Nearby police station markers

✅ Profile Management

    Change password securely

    Store and manage emergency contacts

# Technical Stack
    Frontend: Android (Java, XML)

    Backend: Firebase (Authentication, Firestore)

    Maps & Location: Google Maps SDK, FusedLocationProvider

    Permissions: Location, Contacts, SMS

    Chatbot: Custom implementation with Gemini AI (via ChatbotService)

# Key Components
1. Authentication (LoginActivity, SignUpActivity, ForgotPasswordActivity)
Uses Firebase Auth for:

        Email/password login

        Google Sign-In

        Password reset via email

2. Emergency Handling (HomeFragment, EmergencyHandler)

        Emergency button triggers alerts

        Long press cancels emergency

        Haptic feedback for user confirmation

3. Location & Maps (MapFragment, NearbyPoliceActivity)
FusedLocationProvider for real-time location

    Google Maps integration

    Dummy police stations (can be replaced with Google Places API)

4. Emergency Contacts (EmergencyContactsActivity)
    
        Contacts permission required

        Add/Remove contacts stored in Firestore

        Phone number formatting

5. Chatbot (ChatFragment)
        Gemini AI integration (via ChatbotService)

        User & bot message differentiation

        Preloaded safety tips

# Permissions Required
        ACCESS_FINE_LOCATION (for maps & emergency alerts)

        READ_CONTACTS (for emergency contact management)

# Setup Instructions
1. Firebase Setup
        
        1. Create a Firebase project at firebase.google.com

        2. Enable Authentication (Email/Password & Google Sign-In)

        3. Enable Firestore Database

        4. Add google-services.json to your app/ folder


2. Google Maps API
    
    Get an API key from Google Cloud Console

        Add it in AndroidManifest.xml:


            <meta-data
                android:name="com.google.android.geo.API_KEY"
                android:value="YOUR_API_KEY" />

3. Run the App
        Clone the repository

        Open in Android Studio

        Build & run on an emulator or device
# License
This project is under the ABC License.

# Contact
For issues or contributions, please contact:

📩 riddheshpadma.dev@gmail.Components

🌐 https://mandaladevhub.vercel.app/

# Note
The Nearby Police Stations feature currently uses dummy data. Replace with Google Places API for real-world accuracy.

Ensure Firebase security rules are properly configured before production deployment.

This README.md provides a structured overview of the SafetyKasturi app. Let me know if you'd like any modifications or additional details! 
