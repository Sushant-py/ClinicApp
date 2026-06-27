package com.siddhant.nasya.clinicapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SnotActivity extends AppCompatActivity {

    EditText etPatientId;
    Spinner spinVisitInterval;
    LinearLayout container;
    Button btnSave;

    // Explicit 22 target parameters string schema extracted exactly from the documentation
    private final String[] snotSymptoms = {
            "Need to blow nose", "Sneezing", "Runny nose", "Nasal obstruction/blockage",
            "Loss of smell or taste", "Cough", "Post-nasal discharge", "Thick nasal discharge",
            "Ear fullness", "Dizziness", "Ear pain", "Facial pain/pressure",
            "Difficulty falling asleep", "Waking up at night", "Lack of good night's sleep",
            "Waking up tired", "Fatigue", "Reduced productivity", "Reduced concentration",
            "Frustrated/restless/irritable", "Sad", "Embarrassed"
    };

    // Dictionary mapping exactly matching the 0-5 scale requirements
    private final String[] scaleDetails = {
            "No problem", "Very mild", "Mild", "Moderate", "Severe", "Worst possible"
    };

    // Store references to the SeekBars programmatically so we can read them all on save
    private final Map<Integer, SeekBar> boundSliders = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snot_survey);

        etPatientId = findViewById(R.id.etPatientIdSnot);
        spinVisitInterval = findViewById(R.id.spinVisitIntervalSnot);
        container = findViewById(R.id.snotItemsContainer);
        btnSave = findViewById(R.id.btnSaveSnot);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Setup Dropdown for the Assessment Interval
        String[] intervals = {"Baseline", "Week 1", "Week 2", "Week 3", "Week 4", "Week 8", "Follow-up"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, intervals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinVisitInterval.setAdapter(adapter);

        populateSurveyRows();

        btnSave.setOnClickListener(v -> processAndUploadSnotSurvey());
    }

    /**
     * Iterates through the 22 parameters, instantiates a layout template for each,
     * injects the titles, and mounts a listener to update the text when the slider moves.
     */
    private void populateSurveyRows() {
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < snotSymptoms.length; i++) {
            View view = inflater.inflate(R.layout.item_snot_row, container, false);

            TextView tvName = view.findViewById(R.id.tvSnotItemName);
            SeekBar sbScore = view.findViewById(R.id.sbSnotScore);
            TextView tvValueIndicator = view.findViewById(R.id.tvSnotCurrentValue);

            // Set Title (e.g., "1. Need to blow nose")
            tvName.setText((i + 1) + ". " + snotSymptoms[i]);

            // Add Listener to map numbers to the qualitative text dynamically
            sbScore.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    tvValueIndicator.setText("Score: " + progress + " - " + scaleDetails[progress]);
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            // Store the view reference and attach it to the parent UI container
            boundSliders.put(i, sbScore);
            container.addView(view);
        }
    }

    private void processAndUploadSnotSurvey() {
        String trialId = etPatientId.getText().toString().trim().toUpperCase();

        if (trialId.isEmpty()) {
            Toast.makeText(this, "Please enter a Patient ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String visit = spinVisitInterval.getSelectedItem().toString();

        SnotRecord record = new SnotRecord(trialId, timestamp, visit);



        int totalScoreAccumulator = 0;

        // Iterate over all 22 sliders, capture scores, and calculate total /110
        for (int i = 0; i < snotSymptoms.length; i++) {
            int ratingValue = boundSliders.get(i).getProgress();
            totalScoreAccumulator += ratingValue;

            // Format string keys for Firebase compatibility (removes invalid characters)
            String safeKey = snotSymptoms[i].replace("/", "_").replace("'", "");
            record.symptomScores.put(safeKey, ratingValue);
        }

        record.overallSnotTotal = totalScoreAccumulator;

        // Push to Firebase Realtime Database
        FirebaseDatabase.getInstance().getReference("snot_evaluations")
                .child(trialId)
                .child(visit)
                .setValue(record);

        Toast.makeText(this, "SNOT-22 Logged for " + trialId + " (Score: " + totalScoreAccumulator + "/110)", Toast.LENGTH_LONG).show();
        finish();
    }
}