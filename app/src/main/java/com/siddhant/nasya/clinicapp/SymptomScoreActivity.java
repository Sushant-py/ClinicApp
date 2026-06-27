package com.siddhant.nasya.clinicapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SymptomScoreActivity extends AppCompatActivity {

    EditText etPatientId;
    Spinner spinInterval;
    Button btnSubmit;

    // Fixed: Declared all target symptom evaluation RadioGroups explicitly
    RadioGroup rgCongestion, rgRhinorrhea, rgSneezing, rgItching, rgPostNasal;
    RadioGroup rgSmell, rgEye, rgSleep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_score);

        // Bind core layout elements
        etPatientId = findViewById(R.id.etPatientId);
        spinInterval = findViewById(R.id.spinVisitInterval);
        btnSubmit = findViewById(R.id.btnSubmitSymptoms);

        // Bind Core Nasal Symptom RadioGroup view mappings
        rgCongestion = findViewById(R.id.rgCongestion);
        rgRhinorrhea = findViewById(R.id.rgRhinorrhea);
        rgSneezing = findViewById(R.id.rgSneezing);
        rgItching = findViewById(R.id.rgItching);
        rgPostNasal = findViewById(R.id.rgPostNasal);

        // Bind Extended Observation RadioGroup view mappings
        rgSmell = findViewById(R.id.rgSmell);
        rgEye = findViewById(R.id.rgEye);
        rgSleep = findViewById(R.id.rgSleep);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Setup Dropdown for the Assessment Interval
        String[] intervals = {"Baseline", "Week 1", "Week 2", "Week 4", "Week 8"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, intervals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinInterval.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> compileAndUploadScores());
    }

    /**
     * Calculates the index of the selected item inside the RadioGroup hierarchy.
     * Fixed: Implemented Math.min/Math.max optimizations to manage boundaries cleanly.
     */
    private int getScoreFromRadioGroup(RadioGroup rg) {
        if (rg == null) return 0;
        int selectedId = rg.getCheckedRadioButtonId();
        if (selectedId == -1) return 0;

        View radioButton = rg.findViewById(selectedId);
        int score = rg.indexOfChild(radioButton);

        // Lint Optimization: Replace complex conditional trees with clean boundaries
        return Math.max(0, Math.min(score, 3));
    }

    private void compileAndUploadScores() {
        String trialId = etPatientId.getText().toString().trim();

        if (trialId.isEmpty()) {
            Toast.makeText(this, "Please enter a Patient ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String visit = spinInterval.getSelectedItem().toString();

        SymptomRecord record = new SymptomRecord(trialId, timestamp, visit);

        // Compute individual parameters
        record.congestion = getScoreFromRadioGroup(rgCongestion);
        record.rhinorrhea = getScoreFromRadioGroup(rgRhinorrhea);
        record.sneezing = getScoreFromRadioGroup(rgSneezing);
        record.itching = getScoreFromRadioGroup(rgItching);
        record.postNasalDrip = getScoreFromRadioGroup(rgPostNasal);

        record.lossOfSmell = getScoreFromRadioGroup(rgSmell);
        record.eyeSymptoms = getScoreFromRadioGroup(rgEye);
        record.sleepDisturbance = getScoreFromRadioGroup(rgSleep);

        // Compute clinical aggregates exactly as structured in documentation rules
        record.totalNasalScore = record.congestion + record.rhinorrhea + record.sneezing + record.itching + record.postNasalDrip;
        record.totalExtendedScore = record.totalNasalScore + record.lossOfSmell + record.eyeSymptoms + record.sleepDisturbance;

        // Push data collection record asynchronously to Firebase
        FirebaseDatabase.getInstance().getReference("clinical_symptoms")
                .child(trialId)
                .child(visit)
                .setValue(record);

        Toast.makeText(this, "Evaluation Sheet Logged for " + trialId, Toast.LENGTH_SHORT).show();
        finish();
    }
}