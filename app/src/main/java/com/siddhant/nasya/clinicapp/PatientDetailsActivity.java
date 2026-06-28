package com.siddhant.nasya.clinicapp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PatientDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);

        TextView tvFullResults = findViewById(R.id.tvFullResults);
        ImageButton btnBack = findViewById(R.id.btnBack);

        String data = getIntent().getStringExtra("patientData");
        if (data != null) {
            tvFullResults.setText(data);
        }

        btnBack.setOnClickListener(v -> finish());
    }
}