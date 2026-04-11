package com.siddhant.nasya.clinicapp;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class TrialApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // THIS IS THE MAGIC LINE.
        // It tells Firebase: "If there is no Wi-Fi, save the data to the phone's hard drive.
        // When Wi-Fi comes back, upload it instantly."
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}