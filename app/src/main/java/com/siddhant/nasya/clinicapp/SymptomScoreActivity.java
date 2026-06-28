package com.siddhant.nasya.clinicapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SymptomScoreActivity extends AppCompatActivity {

    EditText etPatientId, etPatientName, etAssessmentDate, etAssessmentTime;
    Spinner spinInterval;
    Button btnSubmit;

    RadioGroup rgCongestion, rgRhinorrhea, rgSneezing, rgItching, rgPostNasal;
    RadioGroup rgSmell, rgEye, rgSleep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_score);

        // Bind core layout elements
        etPatientId = findViewById(R.id.etPatientId);
        etPatientName = findViewById(R.id.etPatientName);
        etAssessmentDate = findViewById(R.id.etAssessmentDate);
        etAssessmentTime = findViewById(R.id.etAssessmentTime);
        spinInterval = findViewById(R.id.spinVisitInterval);
        btnSubmit = findViewById(R.id.btnSubmitSymptoms);

        // Pre-fill Patient ID if passed from MonitorActivity
        String prefillId = getIntent().getStringExtra("patientId");
        if (prefillId != null && !prefillId.isEmpty()) {
            etPatientId.setText(prefillId);
            loadPatientName(prefillId);
        }

        etPatientId.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                loadPatientName(s.toString().trim().toUpperCase());
            }
        });

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

        // Date Picker Setup
        etAssessmentDate.setOnClickListener(v -> showDatePicker());
        
        // Time Picker Setup
        etAssessmentTime.setOnClickListener(v -> showTimePicker());

        setupSpinners();

        btnSubmit.setOnClickListener(v -> compileAndUploadScores());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            etAssessmentDate.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            etAssessmentTime.setText(time);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void setupSpinners() {
        // Setup Dropdown for the Assessment Interval
        String[] intervals = {"Baseline", "Week 1", "Week 2", "Week 4", "Week 8"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, intervals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinInterval.setAdapter(adapter);

        spinInterval.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadExistingVisitData(intervals[position]);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void loadPatientName(String patientId) {
        if (patientId.isEmpty()) return;
        FirebaseDatabase.getInstance().getReference("users").child(patientId)
                .child("fullName").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            etPatientName.setText(snapshot.getValue(String.class));
                        }
                    }
                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
                });
    }

    private void loadExistingVisitData(String visit) {
        String patientId = etPatientId.getText().toString().trim().toUpperCase();
        if (patientId.isEmpty()) return;

        FirebaseDatabase.getInstance().getReference("clinical_symptoms")
                .child(patientId).child(visit).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            SymptomRecord record = snapshot.getValue(SymptomRecord.class);
                            if (record != null) {
                                etAssessmentDate.setText(record.assessmentDate);
                                etAssessmentTime.setText(record.assessmentTime);
                                setRadioGroupScore(rgCongestion, record.congestion);
                                setRadioGroupScore(rgRhinorrhea, record.rhinorrhea);
                                setRadioGroupScore(rgSneezing, record.sneezing);
                                setRadioGroupScore(rgItching, record.itching);
                                setRadioGroupScore(rgPostNasal, record.postNasalDrip);
                                setRadioGroupScore(rgSmell, record.lossOfSmell);
                                setRadioGroupScore(rgEye, record.eyeSymptoms);
                                setRadioGroupScore(rgSleep, record.sleepDisturbance);
                                Toast.makeText(SymptomScoreActivity.this, "Existing data loaded for " + visit, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Clear fields for new entry
                            etAssessmentDate.setText("");
                            etAssessmentTime.setText("");
                            rgCongestion.clearCheck();
                            rgRhinorrhea.clearCheck();
                            rgSneezing.clearCheck();
                            rgItching.clearCheck();
                            rgPostNasal.clearCheck();
                            rgSmell.clearCheck();
                            rgEye.clearCheck();
                            rgSleep.clearCheck();
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
                });
    }

    private void setRadioGroupScore(RadioGroup rg, int score) {
        if (score >= 0 && score < rg.getChildCount()) {
            ((android.widget.RadioButton) rg.getChildAt(score)).setChecked(true);
        }
    }

    private int getScoreFromRadioGroup(RadioGroup rg) {
        if (rg == null) return 0;
        int selectedId = rg.getCheckedRadioButtonId();
        if (selectedId == -1) return 0;

        View radioButton = rg.findViewById(selectedId);
        int score = rg.indexOfChild(radioButton);
        return Math.max(0, Math.min(score, 3));
    }

    private void compileAndUploadScores() {
        String trialId = etPatientId.getText().toString().trim().toUpperCase();
        String aDate = etAssessmentDate.getText().toString().trim();
        String aTime = etAssessmentTime.getText().toString().trim();

        if (trialId.isEmpty() || aDate.isEmpty()) {
            Toast.makeText(this, "Please fill in Patient ID and Date", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String visit = spinInterval.getSelectedItem().toString();

        SymptomRecord record = new SymptomRecord(trialId, timestamp, visit);
        record.assessmentDate = aDate;
        record.assessmentTime = aTime;

        record.congestion = getScoreFromRadioGroup(rgCongestion);
        record.rhinorrhea = getScoreFromRadioGroup(rgRhinorrhea);
        record.sneezing = getScoreFromRadioGroup(rgSneezing);
        record.itching = getScoreFromRadioGroup(rgItching);
        record.postNasalDrip = getScoreFromRadioGroup(rgPostNasal);

        record.lossOfSmell = getScoreFromRadioGroup(rgSmell);
        record.eyeSymptoms = getScoreFromRadioGroup(rgEye);
        record.sleepDisturbance = getScoreFromRadioGroup(rgSleep);

        record.totalNasalScore = record.congestion + record.rhinorrhea + record.sneezing + record.itching + record.postNasalDrip;
        record.totalExtendedScore = record.totalNasalScore + record.lossOfSmell + record.eyeSymptoms + record.sleepDisturbance;

        FirebaseDatabase.getInstance().getReference("clinical_symptoms")
                .child(trialId)
                .child(visit)
                .setValue(record);

        Toast.makeText(this, "Evaluation Sheet Logged for " + trialId, Toast.LENGTH_SHORT).show();
        finish();
    }
}