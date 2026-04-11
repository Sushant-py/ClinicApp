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

public class ConsentActivity extends AppCompatActivity {

    Button btnConsentAgree;
    EditText etBatchNumber, etExpiryDate;
    SharedPreferences prefs;
    String trialId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);

        prefs = getSharedPreferences("TrialPrefs", Context.MODE_PRIVATE);
        trialId = prefs.getString("USER_NAME", "");

        btnConsentAgree = findViewById(R.id.btnConsentAgree);
        etBatchNumber = findViewById(R.id.etBatchNumber);
        etExpiryDate = findViewById(R.id.etExpiryDate);

        btnConsentAgree.setOnClickListener(v -> submitConsent());
    }

    private void submitConsent() {
        String batchNum = etBatchNumber.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();

        if (batchNum.isEmpty() || expiryDate.isEmpty()) {
            Toast.makeText(this, "Please enter the Batch Number and Expiry Date of your Nasya Oil.", Toast.LENGTH_LONG).show();
            return;
        }

        // Save Drug Accountability to Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(trialId);
        userRef.child("oilBatchNumber").setValue(batchNum);
        userRef.child("oilExpiryDate").setValue(expiryDate);
        userRef.child("hasConsented").setValue(true);

        // Update Local Memory so they don't see this screen again
        prefs.edit().putBoolean("HAS_CONSENTED", true).apply();

        Toast.makeText(this, "Consent Saved.", Toast.LENGTH_SHORT).show();

        // Send them to Onboarding!
        Intent intent = new Intent(ConsentActivity.this, OnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}