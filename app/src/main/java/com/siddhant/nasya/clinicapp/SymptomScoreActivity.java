package com.siddhant.nasya.clinicapp;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SymptomScoreActivity extends AppCompatActivity {

    Spinner spinInterval;
    RadioGroup rgCongestion;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_score);

        spinInterval = findViewById(R.id.spinVisitInterval);
        rgCongestion = findViewById(R.id.rgCongestion);
        btnSubmit = findViewById(R.id.btnSubmitSymptoms);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String[] intervals = {"Baseline", "Week 1", "Week 2", "Week 4", "Week 8"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, intervals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinInterval.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> compileAndUploadScores());
    }

    private int getScoreFromRadioGroup(RadioGroup rg) {
        if (rg == null) return 0;
        int id = rg.getCheckedRadioButtonId();
        if (id == -1 || id == R.id.rbCong0) return 0;
        if (id == R.id.rbCong1) return 1;
        if (id == R.id.rbCong2) return 2;
        return 3;
    }

    private void compileAndUploadScores() {
        String trialId = getSharedPreferences("TrialPrefs", Context.MODE_PRIVATE).getString("USER_NAME", "Unknown");
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String visit = spinInterval.getSelectedItem().toString();

        SymptomRecord record = new SymptomRecord(trialId, timestamp, visit);
        record.congestion = getScoreFromRadioGroup(rgCongestion);

        record.totalNasalScore = record.congestion; // Add other variables once XML is expanded

        FirebaseDatabase.getInstance().getReference("clinical_symptoms")
                .child(trialId)
                .child(visit)
                .setValue(record)
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, "Evaluation Logged", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}