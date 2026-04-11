package com.siddhant.nasya.clinicapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    int currentStep = 1;
    TextView tvStepDescription;
    Button btnNextStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        tvStepDescription = findViewById(R.id.tvStepDescription);
        btnNextStep = findViewById(R.id.btnNextStep);

        btnNextStep.setOnClickListener(v -> {
            currentStep++;
            updateStep();
        });
    }

    private void updateStep() {
        if (currentStep == 2) {
            tvStepDescription.setText("Step 2: Confirmation\n\nAfter applying, open this app and tap the 'Morning' or 'Evening' button to confirm your dose.");
        } else if (currentStep == 3) {
            tvStepDescription.setText("Step 3: Safety & Reporting\n\nIf you experience bleeding, severe pain, or trouble breathing, use the 'Report Problem' button immediately.");
            btnNextStep.setText("Start Trial Dashboard");
        } else {
            // Finish onboarding and mark as completed
            SharedPreferences prefs = getSharedPreferences("TrialPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("HAS_COMPLETED_ONBOARDING", true).apply();

            Intent intent = new Intent(this, PatientActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}