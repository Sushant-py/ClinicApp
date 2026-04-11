package com.siddhant.nasya.clinicapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EligibilityActivity extends AppCompatActivity {

    RadioButton rbNosebleedYes, rbSurgeryYes;
    Button btnCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eligibility);

        rbNosebleedYes = findViewById(R.id.rbNosebleedYes);
        rbSurgeryYes = findViewById(R.id.rbSurgeryYes);
        btnCheck = findViewById(R.id.btnCheckEligibility);

        btnCheck.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("TrialPrefs", Context.MODE_PRIVATE);
            
            // Clinical Triage Logic: Check for contraindications
            if (rbNosebleedYes.isChecked() || rbSurgeryYes.isChecked()) {
                // Hard-stop: User is ineligible
                Intent intent = new Intent(this, IneligibleActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Eligible: Mark as eligible and proceed to Consent
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("IS_ELIGIBLE", true);
                editor.apply();

                Intent intent = new Intent(this, ConsentActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}