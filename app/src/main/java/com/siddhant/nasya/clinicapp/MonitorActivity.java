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

    // Display Caches (Ensures UI doesn't endlessly duplicate when filters change)
    private String profileText = "";
    private String dosesText = "";
    private String evaluationsText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // Bind Doctor Evaluation Buttons
        Button btnSymptomSheet = findViewById(R.id.btnDoctorSymptomEvaluation);
        Button btnSnotSurvey = findViewById(R.id.btnDoctorSnotSurvey);

        if (btnSymptomSheet != null) {
            btnSymptomSheet.setOnClickListener(v -> {
                isNavigatingToInternalActivity = true;
                startActivity(new Intent(MonitorActivity.this, SymptomScoreActivity.class));
            });
        }

        if (btnSnotSurvey != null) {
            btnSnotSurvey.setOnClickListener(v -> {
                isNavigatingToInternalActivity = true;
                startActivity(new Intent(MonitorActivity.this, SnotActivity.class));
            });
        }

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

            // Clear caches before fetching new patient
            profileText = "";
            dosesText = "";
            evaluationsText = "";
            cachedAdrs.clear();

            fetchPatientData(currentPatientId);
        });

        rgTimeFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (!cachedAdrs.isEmpty()) {
                applyTimeFilterAndRefreshDisplay();
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

                    profileText = getString(R.string.patient_profile_header) + "\n" +
                            getString(R.string.patient_name) + ": " + (name != null ? name : "N/A") + "\n" +
                            getString(R.string.patient_age) + ": " + (age.equals("null") ? "N/A" : age) + " | " + getString(R.string.patient_sex) + ": " + (sex != null ? sex : "N/A") + "\n" +
                            getString(R.string.trial_arm) + ": " + (arm != null ? arm : "N/A") + "\n" +
                            getString(R.string.comorbidities) + ": " + (comorbidities != null ? comorbidities : "None") + "\n" +
                            getString(R.string.drug_batch) + ": " + (batch != null ? batch : "N/A") + "\n" +
                            "========================\n\n";
                } else {
                    profileText = getString(R.string.profile_not_found) + "\n\n";
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
                    dosesText = getString(R.string.severe_non_adherence) + "\n";
                } else {
                    dosesText = getString(R.string.total_doses_logged) + ": " + totalDosesLogged + "\n";
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
                // Finally, fetch the clinical evaluations before rendering everything
                fetchClinicalEvaluations(currentPatientId);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchClinicalEvaluations(String patientId) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // 1. Fetch Nasal Symptoms
        rootRef.child("clinical_symptoms").child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    evaluationsText += "\n\n📊 NASAL SYMPTOM SCORE TIMELINE\n";
                    for (DataSnapshot visitSnap : snapshot.getChildren()) {
                        String visitType = visitSnap.getKey();
                        Integer tns = visitSnap.child("totalNasalScore").getValue(Integer.class);
                        Integer tes = visitSnap.child("totalExtendedScore").getValue(Integer.class);
                        String time = visitSnap.child("timestamp").getValue(String.class);

                        evaluationsText += "Visit: " + visitType + " (" + time + ")\n";
                        evaluationsText += "• Total Nasal Score: " + (tns != null ? tns : 0) + "/15\n";
                        evaluationsText += "• Total Extended Score: " + (tes != null ? tes : 0) + "/24\n";
                        evaluationsText += "--------------------\n";
                    }
                }

                // 2. Chain SNOT-22 fetch to guarantee synchronous display order
                fetchSnotEvaluations(patientId, rootRef);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fetchSnotEvaluations(patientId, rootRef);
            }
        });
    }

    private void fetchSnotEvaluations(String patientId, DatabaseReference rootRef) {
        rootRef.child("snot_evaluations").child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    evaluationsText += "\n\n📋 SNOT-22 OUTCOME TRACKING HISTORIES\n";
                    for (DataSnapshot recordSnap : snapshot.getChildren()) {
                        String assessment = recordSnap.child("assessmentPeriod").getValue(String.class);
                        if (assessment == null) assessment = "Routine Check";
                        Integer overallTotal = recordSnap.child("overallSnotTotal").getValue(Integer.class);
                        String time = recordSnap.child("timestamp").getValue(String.class);

                        evaluationsText += "Stage: " + assessment + " (" + time + ")\n";
                        evaluationsText += "• SNOT-22 Sum Total: " + (overallTotal != null ? overallTotal : 0) + "/110\n";
                        evaluationsText += "--------------------\n";
                    }
                }
                // Once everything is fetched, compile the view
                applyTimeFilterAndRefreshDisplay();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                applyTimeFilterAndRefreshDisplay();
            }
        });
    }

    private void applyTimeFilterAndRefreshDisplay() {
        StringBuilder adrBuilder = new StringBuilder();
        adrBuilder.append("\n\n").append(getString(R.string.reported_adrs_header)).append("\n");

        if (cachedAdrs.isEmpty()) {
            adrBuilder.append(getString(R.string.no_adrs_reported));
            tvActionQueueHeader.setText(getString(R.string.pending_action_queue) + " (0)");
        } else {
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
                            resultsBuilder.append(getString(R.string.action_required)).append("\n");
                        }
                        if (isSae) {
                            resultsBuilder.append(getString(R.string.sae_warning)).append("\n");
                        }
                        resultsBuilder.append(getString(R.string.date_label)).append(": ").append(timestampStr).append("\n");
                        resultsBuilder.append(getString(R.string.severity_label_simple)).append(": ").append(sev).append("\n");
                        resultsBuilder.append(getString(R.string.desc_label)).append(": ").append(desc).append("\n");
                        resultsBuilder.append(getString(R.string.status_label)).append(": ").append(action).append("\n");
                        resultsBuilder.append("--------------------\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            tvActionQueueHeader.setText(getString(R.string.pending_action_queue) + " (" + pendingCount + " " + getString(R.string.action_required) + ")");
            if (resultsBuilder.length() == 0) {
                adrBuilder.append("No ADRs found for this time period.");
            } else {
                adrBuilder.append(resultsBuilder.toString());
            }
        }

        // Assemble the final screen layout: Profile + Doses + ADRs + Evaluation Reports
        tvMonitorResults.setText(profileText + dosesText + adrBuilder.toString() + evaluationsText);
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