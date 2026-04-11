package com.siddhant.nasya.clinicapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    int currentStep = 1;
    TextView tvStepDescription, tvTitle;
    Button btnNextStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load Locale
        LanguageHelper.loadLocale(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        tvTitle = findViewById(R.id.tvOnboardingTitle);
        tvStepDescription = findViewById(R.id.tvStepDescription);
        btnNextStep = findViewById(R.id.btnNextStep);

        updateStep();

        btnNextStep.setOnClickListener(v -> {
            currentStep++;
            updateStep();
        });
    }

    private void updateStep() {
        if (currentStep == 1) {
            String stepTitle = getString(R.string.step_1_title);
            String stepDesc = getString(R.string.step_1_desc);
            tvStepDescription.setText(stepTitle + "\n\n" + stepDesc);
            btnNextStep.setText(getString(R.string.next_step));
        } else if (currentStep == 2) {
            String stepTitle = getString(R.string.step_2_title);
            String stepDesc = getString(R.string.step_2_desc);
            tvStepDescription.setText(stepTitle + "\n\n" + stepDesc);
            btnNextStep.setText(getString(R.string.next_step));
        } else if (currentStep == 3) {
            String stepTitle = getString(R.string.step_3_title);
            String stepDesc = getString(R.string.step_3_desc);
            tvStepDescription.setText(stepTitle + "\n\n" + stepDesc);
            btnNextStep.setText(getString(R.string.start_trial));
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
