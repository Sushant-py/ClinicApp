package com.siddhant.nasya.clinicapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SnotActivity extends AppCompatActivity {

    // Global layout view instances
    EditText etPatientId, etHospital, etAssessmentDate, etDemoName, etDemoAge, etDemoGender, etDemoAddress;
    EditText etDurationYears, etDurationMonths, etAdverseDetails;
    EditText etFoodAllergy, etOtherAllergy, etDaysCompleted, etMissedReason, etInvestigatorName;
    EditText etAntihistaminesSpecify, etDecongestantsSpecify, etCorticosteroidsSpecify, etOtherMeds;
    EditText etCompletionDate, etDiscontOtherSpecify;

    Spinner spinVisitInterval, spinResponse, spinSatisfaction;
    RadioGroup rgRandomization, rgFamilyHistory, rgMissedDoses, rgSustained, rgRelapse, rgAdverseSeverity, rgDiscontinuation;
    CheckBox cbDust, cbPollen, cbPet, cbFood, cbOtherAlg, cbAntihistamines, cbDecongestants, cbCorticosteroids;

    LinearLayout container;
    androidx.cardview.widget.CardView cardStudyCompletion;
    Button btnSave;

    private final String[] snotSymptoms = {
            "Need to blow nose", "Sneezing", "Runny nose", "Nasal obstruction/blockage",
            "Loss of smell or taste", "Cough", "Post-nasal discharge", "Thick nasal discharge",
            "Ear fullness", "Dizziness", "Ear pain", "Facial pain/pressure",
            "Difficulty falling asleep", "Waking up at night", "Lack of good night's sleep",
            "Waking up tired", "Fatigue", "Reduced productivity", "Reduced concentration",
            "Frustrated/restless/irritable", "Sad", "Embarrassed"
    };

    private final String[] scaleDetails = {
            "No problem", "Very mild", "Mild", "Moderate", "Severe", "Worst possible"
    };

    private final Map<Integer, SeekBar> boundSliders = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snot_survey);

        initViews();
        setupSpinners();
        setupDatePickers();
        populateSurveyRows();

        // Pre-fill Patient ID if passed
        String prefillId = getIntent().getStringExtra("patientId");
        if (prefillId != null && !prefillId.isEmpty()) {
            etPatientId.setText(prefillId);
            loadPatientProfile(prefillId);
        }

        etPatientId.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                loadPatientProfile(s.toString().trim().toUpperCase());
            }
        });

        btnSave.setOnClickListener(v -> processAndUploadSnotSurvey());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        etPatientId = findViewById(R.id.etPatientIdSnot);
        etHospital = findViewById(R.id.etHospital);
        etAssessmentDate = findViewById(R.id.etAssessmentDate);
        spinVisitInterval = findViewById(R.id.spinVisitIntervalSnot);
        rgRandomization = findViewById(R.id.rgRandomization);

        etDemoName = findViewById(R.id.etDemoName);
        etDemoAge = findViewById(R.id.etDemoAge);
        etDemoGender = findViewById(R.id.etDemoGender);
        etDemoAddress = findViewById(R.id.etDemoAddress);

        etDurationYears = findViewById(R.id.etDurationYears);
        etDurationMonths = findViewById(R.id.etDurationMonths);
        rgFamilyHistory = findViewById(R.id.rgFamilyHistory);
        cbDust = findViewById(R.id.cbDust);
        cbPollen = findViewById(R.id.cbPollen);
        cbPet = findViewById(R.id.cbPet);
        cbFood = findViewById(R.id.cbFood);
        cbOtherAlg = findViewById(R.id.cbOtherAlg);
        etFoodAllergy = findViewById(R.id.etFoodAllergy);
        etOtherAllergy = findViewById(R.id.etOtherAllergy);

        cbAntihistamines = findViewById(R.id.cbAntihistamines);
        etAntihistaminesSpecify = findViewById(R.id.etAntihistaminesSpecify);
        cbDecongestants = findViewById(R.id.cbDecongestants);
        etDecongestantsSpecify = findViewById(R.id.etDecongestantsSpecify);
        cbCorticosteroids = findViewById(R.id.cbCorticosteroids);
        etCorticosteroidsSpecify = findViewById(R.id.etCorticosteroidsSpecify);
        etOtherMeds = findViewById(R.id.etOtherMeds);

        container = findViewById(R.id.snotItemsContainer);

        etDaysCompleted = findViewById(R.id.etDaysCompleted);
        rgMissedDoses = findViewById(R.id.rgMissedDoses);
        etMissedReason = findViewById(R.id.etMissedReason);
        rgAdverseSeverity = findViewById(R.id.rgAdverseSeverity);
        etAdverseDetails = findViewById(R.id.etAdverseDetails);
        spinResponse = findViewById(R.id.spinResponse);
        spinSatisfaction = findViewById(R.id.spinSatisfaction);
        rgSustained = findViewById(R.id.rgSustained);
        rgRelapse = findViewById(R.id.rgRelapse);

        etCompletionDate = findViewById(R.id.etCompletionDate);
        rgDiscontinuation = findViewById(R.id.rgDiscontinuation);
        etDiscontOtherSpecify = findViewById(R.id.etDiscontOtherSpecify);
        etInvestigatorName = findViewById(R.id.etInvestigatorName);

        cardStudyCompletion = findViewById(R.id.cardStudyCompletion);
        btnSave = findViewById(R.id.btnSaveSnot);
    }

    private void setupSpinners() {
        String[] intervals = {"Baseline", "Week 1", "Week 2", "Week 3", "Week 4", "Week 8", "Follow-up"};
        ArrayAdapter<String> adapterInt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, intervals);
        spinVisitInterval.setAdapter(adapterInt);

        spinVisitInterval.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = intervals[position];
                if (selected.equals("Week 8") || selected.equals("Follow-up")) {
                    cardStudyCompletion.setVisibility(View.VISIBLE);
                } else {
                    cardStudyCompletion.setVisibility(View.GONE);
                }
                loadExistingVisitData(selected);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        String[] responseArray = {"Select", "Excellent (>75% improvement)", "Good (51-75% improvement)", "Fair (26-50% improvement)", "Poor (<25% improvement)"};
        ArrayAdapter<String> adapterRes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, responseArray);
        spinResponse.setAdapter(adapterRes);

        String[] satArray = {"Select", "Very Satisfied", "Satisfied", "Neutral", "Dissatisfied", "Very Dissatisfied"};
        ArrayAdapter<String> adapterSat = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, satArray);
        spinSatisfaction.setAdapter(adapterSat);
    }

    private void setupDatePickers() {
        etAssessmentDate.setOnClickListener(v -> showDatePicker(etAssessmentDate));
        etCompletionDate.setOnClickListener(v -> showDatePicker(etCompletionDate));
    }

    private void showDatePicker(EditText targetEditText) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
                targetEditText.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadPatientProfile(String patientId) {
        if (patientId.isEmpty()) return;
        FirebaseDatabase.getInstance().getReference("users").child(patientId)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            etDemoName.setText(snapshot.child("fullName").getValue(String.class));
                            etDemoAge.setText(String.valueOf(snapshot.child("age").getValue()));
                            etDemoGender.setText(snapshot.child("sex").getValue(String.class));
                            etDemoAddress.setText(snapshot.child("address").getValue(String.class));
                            etHospital.setText(snapshot.child("hospitalCenter").getValue(String.class));
                            
                            String group = snapshot.child("randomizationGroup").getValue(String.class);
                            if ("Intervention".equalsIgnoreCase(group)) {
                                ((RadioButton) rgRandomization.getChildAt(0)).setChecked(true);
                            } else if ("Control".equalsIgnoreCase(group)) {
                                ((RadioButton) rgRandomization.getChildAt(1)).setChecked(true);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
                });
    }

    private void loadExistingVisitData(String visit) {
        String patientId = etPatientId.getText().toString().trim().toUpperCase();
        if (patientId.isEmpty()) return;

        FirebaseDatabase.getInstance().getReference("snot_evaluations")
                .child(patientId).child(visit).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            SnotRecord record = snapshot.getValue(SnotRecord.class);
                            if (record != null) {
                                etAssessmentDate.setText(record.dateOfAssessment);
                                etHospital.setText(record.hospitalCenter);
                                setRadioByText(rgRandomization, record.randomizationGroup);

                                if (record.durationOfRhinitis != null && record.durationOfRhinitis.contains("Years")) {
                                    String[] parts = record.durationOfRhinitis.split(",");
                                    etDurationYears.setText(parts[0].replace(" Years", "").trim());
                                    if (parts.length > 1) {
                                        etDurationMonths.setText(parts[1].replace(" Months", "").trim());
                                    }
                                }

                                setRadioByText(rgFamilyHistory, record.familyHistory);

                                // Allergies
                                cbDust.setChecked(record.allergies != null && record.allergies.contains("Dust mites"));
                                cbPollen.setChecked(record.allergies != null && record.allergies.contains("Pollen"));
                                cbPet.setChecked(record.allergies != null && record.allergies.contains("Pet dander"));
                                cbFood.setChecked(record.allergies != null && record.allergies.contains("Food:"));
                                if (cbFood.isChecked()) {
                                    int start = record.allergies.indexOf("Food: ") + 6;
                                    int end = record.allergies.indexOf(",", start);
                                    if (end == -1) end = record.allergies.length();
                                    etFoodAllergy.setText(record.allergies.substring(start, end));
                                }
                                cbOtherAlg.setChecked(record.allergies != null && record.allergies.contains("Other:"));
                                if (cbOtherAlg.isChecked()) {
                                    int start = record.allergies.indexOf("Other: ") + 7;
                                    etOtherAllergy.setText(record.allergies.substring(start));
                                }

                                // Medications
                                parseMedication(record.antihistamines, cbAntihistamines, etAntihistaminesSpecify);
                                parseMedication(record.nasalDecongestants, cbDecongestants, etDecongestantsSpecify);
                                parseMedication(record.corticosteroids, cbCorticosteroids, etCorticosteroidsSpecify);
                                etOtherMeds.setText(record.otherMedications);

                                etDaysCompleted.setText(record.daysCompleted);
                                setRadioByText(rgMissedDoses, record.missedDoses);
                                etMissedReason.setText(record.missedDosesReason);
                                setRadioByText(rgAdverseSeverity, record.adverseEventSeverity);
                                etAdverseDetails.setText(record.adverseEventDetails);

                                setSpinnerValue(spinResponse, record.treatmentResponse);
                                setSpinnerValue(spinSatisfaction, record.patientSatisfaction);

                                setRadioByText(rgSustained, record.sustainedResponse);
                                setRadioByText(rgRelapse, record.relapse);

                                etCompletionDate.setText(record.dateOfCompletion);
                                String discReason = record.reasonForDiscontinuation;
                                if (discReason != null && discReason.contains(":")) {
                                    setRadioByText(rgDiscontinuation, "Other");
                                    etDiscontOtherSpecify.setText(discReason.substring(discReason.indexOf(":") + 1).trim());
                                } else {
                                    setRadioByText(rgDiscontinuation, discReason);
                                }

                                etInvestigatorName.setText(record.investigatorName);

                                // Map SNOT-22 Scores back to sliders
                                for (int i = 0; i < snotSymptoms.length; i++) {
                                    String safeKey = snotSymptoms[i].replace("/", "_").replace("'", "");
                                    if (record.symptomScores.containsKey(safeKey)) {
                                        boundSliders.get(i).setProgress(record.symptomScores.get(safeKey));
                                    }
                                }
                                Toast.makeText(SnotActivity.this, "Existing SNOT-22 data loaded", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            clearFields();
                        }
                    }
                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
                });
    }

    private void parseMedication(String data, CheckBox cb, EditText et) {
        if (data != null && data.startsWith("Yes")) {
            cb.setChecked(true);
            if (data.contains("(") && data.contains(")")) {
                et.setText(data.substring(data.indexOf("(") + 1, data.indexOf(")")));
            }
        } else {
            cb.setChecked(false);
            et.setText("");
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void clearFields() {
        etAssessmentDate.setText("");
        etDurationYears.setText("");
        etDurationMonths.setText("");
        rgFamilyHistory.clearCheck();
        cbDust.setChecked(false);
        cbPollen.setChecked(false);
        cbPet.setChecked(false);
        cbFood.setChecked(false);
        etFoodAllergy.setText("");
        cbOtherAlg.setChecked(false);
        etOtherAllergy.setText("");
        cbAntihistamines.setChecked(false);
        etAntihistaminesSpecify.setText("");
        cbDecongestants.setChecked(false);
        etDecongestantsSpecify.setText("");
        cbCorticosteroids.setChecked(false);
        etCorticosteroidsSpecify.setText("");
        etOtherMeds.setText("");
        etDaysCompleted.setText("");
        rgMissedDoses.clearCheck();
        etMissedReason.setText("");
        rgAdverseSeverity.clearCheck();
        etAdverseDetails.setText("");
        spinResponse.setSelection(0);
        spinSatisfaction.setSelection(0);
        rgSustained.clearCheck();
        rgRelapse.clearCheck();
        etCompletionDate.setText("");
        rgDiscontinuation.clearCheck();
        etDiscontOtherSpecify.setText("");
        etInvestigatorName.setText("");
        for (SeekBar sb : boundSliders.values()) {
            sb.setProgress(0);
        }
    }

    private void setRadioByText(RadioGroup rg, String text) {
        if (text == null || text.isEmpty() || text.equals("N/A")) {
            rg.clearCheck();
            return;
        }
        for (int i = 0; i < rg.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rg.getChildAt(i);
            if (rb.getText().toString().equalsIgnoreCase(text) || (text.startsWith(rb.getText().toString()) && text.contains(":"))) {
                rb.setChecked(true);
                return;
            }
        }
    }

    private void populateSurveyRows() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < snotSymptoms.length; i++) {
            View view = inflater.inflate(R.layout.item_snot_row, container, false);
            TextView tvName = view.findViewById(R.id.tvSnotItemName);
            SeekBar sbScore = view.findViewById(R.id.sbSnotScore);
            TextView tvValueIndicator = view.findViewById(R.id.tvSnotCurrentValue);

            tvName.setText((i + 1) + ". " + snotSymptoms[i]);
            sbScore.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    tvValueIndicator.setText("Score: " + progress + " - " + scaleDetails[progress]);
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            boundSliders.put(i, sbScore);
            container.addView(view);
        }
    }

    private String getRadioText(RadioGroup rg) {
        int id = rg.getCheckedRadioButtonId();
        if (id != -1) {
            return ((RadioButton) findViewById(id)).getText().toString();
        }
        return "N/A";
    }

    private void processAndUploadSnotSurvey() {
        String trialId = etPatientId.getText().toString().trim().toUpperCase();
        if (trialId.isEmpty()) {
            Toast.makeText(this, "Please enter a Patient ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String investigator = etInvestigatorName.getText().toString().trim();
        if (investigator.isEmpty()) {
            Toast.makeText(this, "Investigator signature/name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cardStudyCompletion.getVisibility() == View.VISIBLE) {
            if (etCompletionDate.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Completion Date is required for this visit", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String visit = spinVisitInterval.getSelectedItem().toString();

        SnotRecord record = new SnotRecord(trialId, timestamp, visit);

        // Map Parent Identification Section
        record.hospitalCenter = etHospital.getText().toString().trim();
        record.dateOfAssessment = etAssessmentDate.getText().toString().trim();
        record.randomizationGroup = getRadioText(rgRandomization);

        // Map Medical History
        record.durationOfRhinitis = etDurationYears.getText().toString().trim() + " Years, " + etDurationMonths.getText().toString().trim() + " Months";
        record.familyHistory = getRadioText(rgFamilyHistory);

        StringBuilder allergyProfileBuilder = new StringBuilder();
        if (cbDust.isChecked()) allergyProfileBuilder.append("Dust mites, ");
        if (cbPollen.isChecked()) allergyProfileBuilder.append("Pollen, ");
        if (cbPet.isChecked()) allergyProfileBuilder.append("Pet dander, ");
        if (cbFood.isChecked()) allergyProfileBuilder.append("Food: ").append(etFoodAllergy.getText().toString().trim()).append(", ");
        if (cbOtherAlg.isChecked()) allergyProfileBuilder.append("Other: ").append(etOtherAllergy.getText().toString().trim());
        record.allergies = allergyProfileBuilder.toString();

        // Map Current Medications
        record.antihistamines = cbAntihistamines.isChecked() ? "Yes (" + etAntihistaminesSpecify.getText().toString().trim() + ")" : "No";
        record.nasalDecongestants = cbDecongestants.isChecked() ? "Yes (" + etDecongestantsSpecify.getText().toString().trim() + ")" : "No";
        record.corticosteroids = cbCorticosteroids.isChecked() ? "Yes (" + etCorticosteroidsSpecify.getText().toString().trim() + ")" : "No";
        record.otherMedications = etOtherMeds.getText().toString().trim();

        // Map Treatment Adherence & Follow Up Metrics
        record.daysCompleted = etDaysCompleted.getText().toString().trim();
        record.missedDoses = getRadioText(rgMissedDoses);
        record.missedDosesReason = etMissedReason.getText().toString().trim();
        record.adverseEventSeverity = getRadioText(rgAdverseSeverity);
        record.adverseEventDetails = etAdverseDetails.getText().toString().trim();
        record.treatmentResponse = spinResponse.getSelectedItem().toString();
        record.patientSatisfaction = spinSatisfaction.getSelectedItem().toString();
        record.sustainedResponse = getRadioText(rgSustained);
        record.relapse = getRadioText(rgRelapse);

        // Map Study Completion Metrics
        record.dateOfCompletion = etCompletionDate.getText().toString().trim();
        record.reasonForDiscontinuation = getRadioText(rgDiscontinuation);
        if (record.reasonForDiscontinuation.equals("Other")) {
            record.reasonForDiscontinuation += ": " + etDiscontOtherSpecify.getText().toString().trim();
        }
        record.investigatorName = etInvestigatorName.getText().toString().trim();

        // Map SNOT-22 Item Array & Accumulate Total Score
        int totalScoreAccumulator = 0;
        for (int i = 0; i < snotSymptoms.length; i++) {
            int ratingValue = boundSliders.get(i).getProgress();
            totalScoreAccumulator += ratingValue;
            String safeKey = snotSymptoms[i].replace("/", "_").replace("'", "");
            record.symptomScores.put(safeKey, ratingValue);
        }
        record.overallSnotTotal = totalScoreAccumulator;

        // Commit to targeted interval subnode in Firebase Realtime Database
        FirebaseDatabase.getInstance().getReference("snot_evaluations")
                .child(trialId)
                .child(visit)
                .setValue(record);

        Toast.makeText(this, "Complete Audit Data Logged for " + visit + "!", Toast.LENGTH_LONG).show();
        finish();
    }
}