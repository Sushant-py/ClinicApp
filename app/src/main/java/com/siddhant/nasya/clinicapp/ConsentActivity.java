package com.siddhant.nasya.clinicapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ConsentActivity extends AppCompatActivity {

    EditText etBatch, etExpiry;
    Button btnAgree;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load Locale
        LanguageHelper.loadLocale(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);

        prefs = getSharedPreferences("TrialPrefs", Context.MODE_PRIVATE);

        etBatch = findViewById(R.id.etBatchNumber);
        etExpiry = findViewById(R.id.etExpiryDate);
        btnAgree = findViewById(R.id.btnConsentAgree);

        btnAgree.setOnClickListener(v -> {
            String batch = etBatch.getText().toString().trim();
            String expiry = etExpiry.getText().toString().trim();

            if (batch.isEmpty() || expiry.isEmpty()) {
                Toast.makeText(this, "Batch and Expiry are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String trialId = prefs.getString("USER_NAME", "Unknown");
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(trialId);

            Map<String, Object> update = new HashMap<>();
            update.put("hasConsented", true);
            update.put("oilBatchNumber", batch);
            update.put("oilExpiryDate", expiry);

            userRef.updateChildren(update).addOnCompleteListener(task -> {
                prefs.edit().putBoolean("HAS_CONSENTED", true).apply();
                Intent intent = new Intent(ConsentActivity.this, OnboardingActivity.class);
                startActivity(intent);
                finish();
            });
        });
    }
}
