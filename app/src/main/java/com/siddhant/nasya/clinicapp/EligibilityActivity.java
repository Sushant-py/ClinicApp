package com.siddhant.nasya.clinicapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EligibilityActivity extends AppCompatActivity {

    RadioGroup rgNosebleed, rgSurgery;
    Button btnVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load Locale First
        LanguageHelper.loadLocale(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eligibility);

        rgNosebleed = findViewById(R.id.rgNosebleed);
        rgSurgery = findViewById(R.id.rgSurgery);
        btnVerify = findViewById(R.id.btnCheckEligibility);

        btnVerify.setOnClickListener(v -> {
            boolean hasNosebleed = ((RadioButton) findViewById(rgNosebleed.getCheckedRadioButtonId())).getText().toString().equals(getString(R.string.yes));
            boolean hasSurgery = ((RadioButton) findViewById(rgSurgery.getCheckedRadioButtonId())).getText().toString().equals(getString(R.string.yes));

            if (hasNosebleed || hasSurgery) {
                // If ineligible, show ineligible screen
                startActivity(new Intent(EligibilityActivity.this, IneligibleActivity.class));
            } else {
                // Mark as eligible
                SharedPreferences prefs = getSharedPreferences("TrialPrefs", Context.MODE_PRIVATE);
                prefs.edit().putBoolean("IS_ELIGIBLE", true).apply();
                
                // Proceed to Consent
                startActivity(new Intent(EligibilityActivity.this, ConsentActivity.class));
            }
            finish();
        });
    }
}
