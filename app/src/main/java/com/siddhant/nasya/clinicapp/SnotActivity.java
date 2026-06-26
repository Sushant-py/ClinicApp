package com.siddhant.nasya.clinicapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
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

    LinearLayout container;
    Button btnSave;

    private final String[] snotSymptoms = {
            "Need to blow nose", "Sneezing", "Runny nose", "Nasal obstruction/blockage",
            "Loss of smell or taste", "Cough", "Post-nasal discharge", "Thick nasal discharge",
            "Ear fullness", "Dizziness", "Ear pain", "Facial pain/pressure",
            "Difficulty falling asleep", "Waking up at night", "Lack of good night's sleep",
            "Waking up tired", "Fatigue", "Reduced productivity", "Reduced concentration",
            "Frustrated/restless/irritable", "Sad", "Embarrassed"
    };

    private final Map<Integer, SeekBar> boundSliders = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snot_survey);

        container = findViewById(R.id.snotItemsContainer);
        btnSave = findViewById(R.id.btnSaveSnot);

        populateSurveyRows();
        btnSave.setOnClickListener(v -> processAndUploadSnotSurvey());
    }

    private void populateSurveyRows() {
        LayoutInflater inflater = LayoutInflater.from(this);
        String[] details = {"No problem", "Very mild", "Mild", "Moderate", "Severe", "Worst possible"};

        for (int i = 0; i < snotSymptoms.length; i++) {
            View view = inflater.inflate(R.layout.item_snot_row, container, false);
            TextView name = view.findViewById(R.id.tvSnotItemName);
            SeekBar bar = view.findViewById(R.id.sbSnotScore);
            TextView valueIndicator = view.findViewById(R.id.tvSnotCurrentValue);

            name.setText((i + 1) + ". " + snotSymptoms[i]);

            final int index = i;
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    valueIndicator.setText("Score: " + progress + " - " + details[progress]);
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            boundSliders.put(index, bar);
            container.addView(view);
        }
    }

    private void processAndUploadSnotSurvey() {
        String trialId = getSharedPreferences("TrialPrefs", Context.MODE_PRIVATE).getString("USER_NAME", "Unknown");
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        SnotRecord record = new SnotRecord(trialId, timestamp, "Routine Check");
        int totalScoreAccumulator = 0;

        for (int i = 0; i < snotSymptoms.length; i++) {
            int ratingValue = boundSliders.get(i).getProgress();
            totalScoreAccumulator += ratingValue;
            record.symptomScores.put(snotSymptoms[i].replace("/", "_"), ratingValue);
        }
        record.overallSnotTotal = totalScoreAccumulator;

        FirebaseDatabase.getInstance().getReference("snot_evaluations")
                .child(trialId)
                .child(String.valueOf(System.currentTimeMillis()))
                .setValue(record)
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, "SNOT-22 Record Uploaded", Toast.LENGTH_LONG).show();
                    finish();
                });
    }
}