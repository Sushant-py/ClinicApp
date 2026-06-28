package com.siddhant.nasya.clinicapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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
    Button btnSearchPatient, btnMarkReviewed, btnRunAudit, btnAddPatient, btnLogout, btnDownloadPdfReport;
    TextView tvMonitorResults, tvAdherenceRate, tvActionQueueHeader;
    RadioGroup rgTimeFilter;

    String currentPatientId = "";
    String currentPatientName = "";
    String currentPatientAge = "";
    List<DataSnapshot> cachedAdrs = new ArrayList<>();

    // Cache lists for PDF generation
    List<DataSnapshot> currentSymptomSnaps = new ArrayList<>();
    List<DataSnapshot> currentSnotSnaps = new ArrayList<>();

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
        btnDownloadPdfReport = findViewById(R.id.btnDownloadPdfReport);
        tvMonitorResults = findViewById(R.id.tvMonitorResults);
        tvAdherenceRate = findViewById(R.id.tvAdherenceRate);
        tvActionQueueHeader = findViewById(R.id.tvActionQueueHeader);
        rgTimeFilter = findViewById(R.id.rgTimeFilter);

        tvMonitorResults.setOnClickListener(v -> {
            String data = tvMonitorResults.getText().toString();
            if (!currentPatientId.isEmpty() && !data.equals("No data loaded.") && !data.startsWith(getString(R.string.loading_data))) {
                Intent intent = new Intent(MonitorActivity.this, PatientDetailsActivity.class);
                intent.putExtra("patientData", data);
                startActivity(intent);
            }
        });

        // Bind Doctor Evaluation Buttons
        Button btnSymptomSheet = findViewById(R.id.btnDoctorSymptomEvaluation);
        Button btnSnotSurvey = findViewById(R.id.btnDoctorSnotSurvey);

        if (btnSymptomSheet != null) {
            btnSymptomSheet.setOnClickListener(v -> {
                isNavigatingToInternalActivity = true;
                Intent intent = new Intent(MonitorActivity.this, SymptomScoreActivity.class);
                intent.putExtra("patientId", currentPatientId);
                intent.putExtra("patientName", currentPatientName);
                startActivity(intent);
            });
        }

        if (btnSnotSurvey != null) {
            btnSnotSurvey.setOnClickListener(v -> {
                isNavigatingToInternalActivity = true;
                Intent intent = new Intent(MonitorActivity.this, SnotActivity.class);
                intent.putExtra("patientId", currentPatientId);
                intent.putExtra("patientName", currentPatientName);
                intent.putExtra("patientAge", currentPatientAge);
                startActivity(intent);
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
            currentSymptomSnaps.clear();
            currentSnotSnaps.clear();
            btnDownloadPdfReport.setVisibility(View.GONE);

            fetchPatientData(currentPatientId);
        });

        // Trigger Download PDF task
        btnDownloadPdfReport.setOnClickListener(v -> {
            if (!currentPatientId.isEmpty()) {
                PdfReportGenerator.generatePatientPdfReport(
                        this,
                        currentPatientId,
                        profileText,
                        dosesText,
                        cachedAdrs,
                        currentSymptomSnaps,
                        currentSnotSnaps
                );
            }
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
                    currentPatientName = name != null ? name : "";
                    String age = String.valueOf(snapshot.child("age").getValue());
                    currentPatientAge = age.equals("null") ? "" : age;
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
                    evaluationsText += "\n\n=== [SECTION: NASAL SYMPTOM SCORES] ===\n";
                    for (DataSnapshot visitSnap : snapshot.getChildren()) {
                        currentSymptomSnaps.add(visitSnap);

                        String visitType = visitSnap.getKey();
                        Integer tns = visitSnap.child("totalNasalScore").getValue(Integer.class);
                        Integer tes = visitSnap.child("totalExtendedScore").getValue(Integer.class);
                        String time = visitSnap.child("timestamp").getValue(String.class);

                        evaluationsText += "\n[Visit: " + visitType.toUpperCase() + "]\n";
                        evaluationsText += "  - Date: " + visitSnap.child("assessmentDate").getValue() + " " + visitSnap.child("assessmentTime").getValue() + "\n";
                        evaluationsText += "  - Logged at: " + time + "\n";
                        evaluationsText += "  [Symptom Ratings]\n";
                        evaluationsText += "    • Congestion: " + visitSnap.child("congestion").getValue() + "\n";
                        evaluationsText += "    • Rhinorrhea: " + visitSnap.child("rhinorrhea").getValue() + "\n";
                        evaluationsText += "    • Sneezing: " + visitSnap.child("sneezing").getValue() + "\n";
                        evaluationsText += "    • Itching: " + visitSnap.child("itching").getValue() + "\n";
                        evaluationsText += "    • Post-Nasal Drip: " + visitSnap.child("postNasalDrip").getValue() + "\n";
                        evaluationsText += "    >> TOTAL NASAL SCORE: " + (tns != null ? tns : 0) + "/15\n";
                        evaluationsText += "    • Loss of Smell: " + visitSnap.child("lossOfSmell").getValue() + "\n";
                        evaluationsText += "    • Eye Symptoms: " + visitSnap.child("eyeSymptoms").getValue() + "\n";
                        evaluationsText += "    • Sleep Disturbance: " + visitSnap.child("sleepDisturbance").getValue() + "\n";
                        evaluationsText += "    >> TOTAL EXTENDED SCORE: " + (tes != null ? tes : 0) + "/24\n";
                        evaluationsText += "------------------------------------------\n";
                    }
                }
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
                    evaluationsText += "\n\n=== [SECTION: SNOT-22 & CLINICAL AUDIT] ===\n";
                    for (DataSnapshot recordSnap : snapshot.getChildren()) {
                        currentSnotSnaps.add(recordSnap);

                        String assessment = recordSnap.child("visitInterval").getValue(String.class);
                        if (assessment == null) assessment = recordSnap.getKey();
                        String time = recordSnap.child("timestamp").getValue(String.class);

                        evaluationsText += "\n[Visit: " + assessment.toUpperCase() + "]\n";
                        evaluationsText += "  - Logged at: " + time + "\n";
                        evaluationsText += "  - SNOT-22 Total Score: " + recordSnap.child("overallSnotTotal").getValue() + "/110\n";
                        
                        evaluationsText += "  [1. Clinical Context]\n";
                        evaluationsText += "    - Randomization: " + recordSnap.child("randomizationGroup").getValue() + "\n";
                        evaluationsText += "    - Center: " + recordSnap.child("hospitalCenter").getValue() + "\n";
                        evaluationsText += "    - History: " + recordSnap.child("durationOfRhinitis").getValue() + " (Family Hist: " + recordSnap.child("familyHistory").getValue() + ")\n";
                        evaluationsText += "    - Allergies: " + recordSnap.child("allergies").getValue() + "\n";
                        
                        evaluationsText += "  [2. Medications]\n";
                        evaluationsText += "    - Antihistamines: " + recordSnap.child("antihistamines").getValue() + "\n";
                        evaluationsText += "    - Decongestants: " + recordSnap.child("nasalDecongestants").getValue() + "\n";
                        evaluationsText += "    - Corticosteroids: " + recordSnap.child("corticosteroids").getValue() + "\n";
                        evaluationsText += "    - Other Meds: " + recordSnap.child("otherMedications").getValue() + "\n";

                        evaluationsText += "  [3. Compliance & Outcomes]\n";
                        evaluationsText += "    - Days Completed: " + recordSnap.child("daysCompleted").getValue() + "\n";
                        evaluationsText += "    - Missed Doses: " + recordSnap.child("missedDoses").getValue() + " (Reason: " + recordSnap.child("missedDosesReason").getValue() + ")\n";
                        evaluationsText += "    - Adverse Event: " + recordSnap.child("adverseEventSeverity").getValue() + " - " + recordSnap.child("adverseEventDetails").getValue() + "\n";
                        evaluationsText += "    - Treatment Response: " + recordSnap.child("treatmentResponse").getValue() + "\n";
                        evaluationsText += "    - Satisfaction: " + recordSnap.child("patientSatisfaction").getValue() + "\n";
                        evaluationsText += "    - Response Sustained: " + recordSnap.child("sustainedResponse").getValue() + " | Relapse: " + recordSnap.child("relapse").getValue() + "\n";

                        if (recordSnap.hasChild("reasonForDiscontinuation")) {
                            String reason = recordSnap.child("reasonForDiscontinuation").getValue(String.class);
                            if (reason != null && !reason.equals("N/A") && !reason.isEmpty()) {
                                evaluationsText += "    - DISCONTINUATION REASON: " + reason + "\n";
                            }
                        }

                        DataSnapshot scoresSnap = recordSnap.child("symptomScores");
                        if (scoresSnap.exists()) {
                            evaluationsText += "  [4. Itemized SNOT-22 Scores]\n";
                            for (DataSnapshot item : scoresSnap.getChildren()) {
                                evaluationsText += "    • " + item.getKey() + ": " + item.getValue() + "\n";
                            }
                        }
                        
                        evaluationsText += "  - Investigator Signature: " + recordSnap.child("investigatorName").getValue() + "\n";
                        evaluationsText += "------------------------------------------\n";
                    }
                }

                if(!profileText.contains("not found")) {
                    btnDownloadPdfReport.setVisibility(View.VISIBLE);
                }
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