package com.siddhant.nasya.clinicapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MonitorActivity extends AppCompatActivity {

    EditText etSearchPatientId;
    Button btnSearchPatient, btnMarkReviewed, btnRunAudit, btnAddPatient, btnLogout;
    TextView tvMonitorResults, tvAdherenceRate, tvActionQueueHeader;
    RadioGroup rgTimeFilter;

    String currentPatientId = "";
    List<DataSnapshot> cachedAdrs = new ArrayList<>();
    
    private boolean isNavigatingToInternalActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Updated to match the new LanguageHelper method signature (no arguments)
        LanguageHelper.loadLocale();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        etSearchPatientId = findViewById(R.id.etSearchPatientId);
        btnSearchPatient = findViewById(R.id.btnSearchPatient);
        btnMarkReviewed = findViewById(R.id.btnMarkReviewed);
        btnRunAudit = findViewById(R.id.btnRunAudit);
        btnAddPatient = findViewById(R.id.btnAddPatient);
        btnLogout = findViewById(R.id.btnLogout);
        tvMonitorResults = findViewById(R.id.tvMonitorResults);
        tvAdherenceRate = findViewById(R.id.tvAdherenceRate);
        tvActionQueueHeader = findViewById(R.id.tvActionQueueHeader);
        rgTimeFilter = findViewById(R.id.rgTimeFilter);

        btnLogout.setOnClickListener(v -> logoutUser());

        btnAddPatient.setOnClickListener(v -> {
            isNavigatingToInternalActivity = true;
            Intent intent = new Intent(MonitorActivity.this, AddPatientActivity.class);
            startActivity(intent);
        });

        btnSearchPatient.setOnClickListener(v -> {
            currentPatientId = etSearchPatientId.getText().toString().trim().toUpperCase();
            if (currentPatientId.isEmpty()) {
                Toast.makeText(this, getString(R.string.patient_id_hint), Toast.LENGTH_SHORT).show();
                return;
            }
            fetchPatientData(currentPatientId);
        });

        rgTimeFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (!cachedAdrs.isEmpty()) {
                applyTimeFilter();
            }
        });

        btnMarkReviewed.setOnClickListener(v -> {
            if (currentPatientId.isEmpty() || cachedAdrs.isEmpty()) {
                Toast.makeText(this, "No data to review", Toast.LENGTH_SHORT).show();
                return;
            }
            DatabaseReference adrsRef = FirebaseDatabase.getInstance().getReference("adrs").child(currentPatientId);
            boolean updated = false;

            for (DataSnapshot ds : cachedAdrs) {
                String action = ds.child("investigatorAction").getValue(String.class);
                if ("Pending".equals(action)) {
                    adrsRef.child(ds.getKey()).child("investigatorAction").setValue("Reviewed");
                    adrsRef.child(ds.getKey()).child("investigatorOutcome").setValue("Resolved");
                    updated = true;
                }
            }

            if (updated) {
                Toast.makeText(this, "Pending Actions Cleared", Toast.LENGTH_SHORT).show();
                fetchPatientData(currentPatientId);
            }
        });

        btnRunAudit.setOnClickListener(v -> {
            if (currentPatientId.isEmpty()) {
                Toast.makeText(this, "Load a patient first", Toast.LENGTH_SHORT).show();
                return;
            }
            runDataQualityCheck(currentPatientId);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isNavigatingToInternalActivity = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("TrialPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(MonitorActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchPatientData(String patientId) {
        tvMonitorResults.setText(getString(R.string.loading_data) + "\n");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(patientId);
        DatabaseReference dosesRef = FirebaseDatabase.getInstance().getReference("doses").child(patientId);
        DatabaseReference adrsRef = FirebaseDatabase.getInstance().getReference("adrs").child(patientId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    String age = String.valueOf(snapshot.child("age").getValue());
                    String sex = snapshot.child("sex").getValue(String.class);
                    String arm = snapshot.child("randomizationArm").getValue(String.class);
                    String comorbidities = snapshot.child("comorbidities").getValue(String.class);
                    String batch = snapshot.child("oilBatchNumber").getValue(String.class);

                    tvMonitorResults.setText(getString(R.string.patient_profile_header) + "\n");
                    tvMonitorResults.append(getString(R.string.patient_name) + ": " + (name != null ? name : "N/A") + "\n");
                    tvMonitorResults.append(getString(R.string.patient_age) + ": " + (age.equals("null") ? "N/A" : age) + " | " + getString(R.string.patient_sex) + ": " + (sex != null ? sex : "N/A") + "\n");
                    tvMonitorResults.append(getString(R.string.trial_arm) + ": " + (arm != null ? arm : "N/A") + "\n");
                    tvMonitorResults.append(getString(R.string.comorbidities) + ": " + (comorbidities != null ? comorbidities : "None") + "\n");
                    tvMonitorResults.append(getString(R.string.drug_batch) + ": " + (batch != null ? batch : "N/A") + "\n");
                    tvMonitorResults.append("========================\n\n");
                } else {
                    tvMonitorResults.setText(getString(R.string.profile_not_found) + "\n\n");
                }
                fetchDosesAndAdrs(dosesRef, adrsRef);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fetchDosesAndAdrs(dosesRef, adrsRef);
            }
        });
    }

    private void fetchDosesAndAdrs(DatabaseReference dosesRef, DatabaseReference adrsRef) {
        dosesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalDosesLogged = snapshot.getChildrenCount();
                long expectedDoses = 14;
                long adherencePercentage = (totalDosesLogged * 100) / expectedDoses;
                if(adherencePercentage > 100) adherencePercentage = 100;

                tvAdherenceRate.setText(getString(R.string.aggregate_adherence) + ": " + adherencePercentage + "%");

                if (totalDosesLogged == 0) {
                    tvMonitorResults.append(getString(R.string.severe_non_adherence) + "\n");
                } else {
                    tvMonitorResults.append(getString(R.string.total_doses_logged) + ": " + totalDosesLogged + "\n");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        adrsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cachedAdrs.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        cachedAdrs.add(ds);
                    }
                }
                applyTimeFilter();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void applyTimeFilter() {
        tvMonitorResults.append("\n\n" + getString(R.string.reported_adrs_header) + "\n");
        if (cachedAdrs.isEmpty()) {
            tvMonitorResults.append(getString(R.string.no_adrs_reported));
            tvActionQueueHeader.setText(getString(R.string.pending_action_queue) + " (0)");
            return;
        }

        int pendingCount = 0;
        long timeLimitMillis = 0;
        long currentTime = System.currentTimeMillis();

        int checkedId = rgTimeFilter.getCheckedRadioButtonId();
        if (checkedId == R.id.rb24Hours) {
            timeLimitMillis = 24L * 60 * 60 * 1000;
        } else if (checkedId == R.id.rb72Hours) {
            timeLimitMillis = 72L * 60 * 60 * 1000;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        StringBuilder resultsBuilder = new StringBuilder();

        for (DataSnapshot ds : cachedAdrs) {
            String timestampStr = ds.child("timestamp").getValue(String.class);
            String desc = ds.child("description").getValue(String.class);
            String sev = ds.child("severity").getValue(String.class);
            String action = ds.child("investigatorAction").getValue(String.class);
            boolean isSae = ds.child("isSae").getValue(Boolean.class) != null ? ds.child("isSae").getValue(Boolean.class) : false;

            try {
                Date adrDate = sdf.parse(timestampStr);
                long diff = currentTime - adrDate.getTime();

                if (timeLimitMillis == 0 || diff <= timeLimitMillis) {
                    if ("Pending".equals(action)) {
                        pendingCount++;
                        resultsBuilder.append(getString(R.string.action_required) + "\n");
                    }
                    if (isSae) {
                        resultsBuilder.append(getString(R.string.sae_warning) + "\n");
                    }
                    resultsBuilder.append(getString(R.string.date_label) + ": ").append(timestampStr).append("\n");
                    resultsBuilder.append(getString(R.string.severity_label_simple) + ": ").append(sev).append("\n");
                    resultsBuilder.append(getString(R.string.desc_label) + ": ").append(desc).append("\n");
                    resultsBuilder.append(getString(R.string.status_label) + ": ").append(action).append("\n");
                    resultsBuilder.append("--------------------\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        tvActionQueueHeader.setText(getString(R.string.pending_action_queue) + " (" + pendingCount + " " + getString(R.string.action_required) + ")");
        if (resultsBuilder.length() == 0) {
            tvMonitorResults.append("No ADRs found for this time period.");
        } else {
            tvMonitorResults.append(resultsBuilder.toString());
        }
    }

    private void runDataQualityCheck(String patientId) {
        tvMonitorResults.setText("=== RUNNING DATA QUALITY AUDIT ===\n");
        DatabaseReference dosesRef = FirebaseDatabase.getInstance().getReference("doses").child(patientId);

        dosesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int errorCount = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String ts = ds.child("timestamp").getValue(String.class);
                    String type = ds.child("doseType").getValue(String.class);

                    if (ts == null || ts.isEmpty()) {
                        tvMonitorResults.append("❌ ERROR: Missing timestamp in dose log.\n");
                        errorCount++;
                    }
                    if (type == null || type.isEmpty()) {
                        tvMonitorResults.append("❌ ERROR: Missing doseType (Morning/Evening).\n");
                        errorCount++;
                    }
                }

                for (DataSnapshot ds : cachedAdrs) {
                    String desc = ds.child("description").getValue(String.class);
                    if (desc == null || desc.trim().isEmpty()) {
                        tvMonitorResults.append("❌ ERROR: ADR found with blank description.\n");
                        errorCount++;
                    }
                }

                if (errorCount == 0) {
                    tvMonitorResults.append("✅ PASS: No data anomalies detected.\n");
                } else {
                    tvMonitorResults.append("\nTotal Errors Found: " + errorCount);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
