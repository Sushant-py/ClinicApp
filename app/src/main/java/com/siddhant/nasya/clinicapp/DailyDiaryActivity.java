package com.siddhant.nasya.clinicapp;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DailyDiaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // LOAD LOCALE FIRST
        LanguageHelper.loadLocale(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_diary);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        SeekBar sbDiscomfort = findViewById(R.id.sbNasalDiscomfort);
        TextView tvScore = findViewById(R.id.tvDiscomfortScore);
        EditText etConcurrentMeds = findViewById(R.id.etConcurrentMeds);
        Button btnSave = findViewById(R.id.btnSaveDiary);

        sbDiscomfort.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Use translated Score string
                String scoreBase = getString(R.string.score);
                tvScore.setText(scoreBase + ": " + progress + " / 10");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSave.setOnClickListener(v -> {
            String trialId = getSharedPreferences("TrialPrefs", Context.MODE_PRIVATE).getString("USER_NAME", "Unknown");
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            HashMap<String, Object> diaryEntry = new HashMap<>();
            diaryEntry.put("nasalDiscomfortVAS", sbDiscomfort.getProgress());
            diaryEntry.put("concurrentMeds", etConcurrentMeds.getText().toString().trim());
            diaryEntry.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            FirebaseDatabase.getInstance().getReference("diaries").child(trialId).child(date).setValue(diaryEntry);

            Toast.makeText(this, "Daily Diary Saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
