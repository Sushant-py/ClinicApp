package com.siddhant.nasya.clinicapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdrActivity extends AppCompatActivity {

    Spinner spinSeverity;
    EditText etDescription;
    SeekBar sbIntensity;
    TextView tvIntensityValue;
    Button btnSubmitAdr, btnEmergencyCall;
    CheckBox cbBleeding, cbBreathlessness, cbSeverePain;
    ImageButton btnBack;

    String patientName;

    private static final String DOCTOR_PHONE = "5556667777"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adr);

        // Init views
        spinSeverity = findViewById(R.id.spinSeverity);
        etDescription = findViewById(R.id.etDescription);
        sbIntensity = findViewById(R.id.sbIntensity);
        tvIntensityValue = findViewById(R.id.tvIntensityValue);
        btnSubmitAdr = findViewById(R.id.btnSubmitAdr);
        btnEmergencyCall = findViewById(R.id.btnEmergencyCall);
        cbBleeding = findViewById(R.id.cbBleeding);
        cbBreathlessness = findViewById(R.id.cbBreathlessness);
        cbSeverePain = findViewById(R.id.cbSeverePain);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("TrialPrefs", Context.MODE_PRIVATE);
        patientName = prefs.getString("USER_NAME", "Participant");

        // Setup Translated Spinner Options
        String[] severities = {
                getString(R.string.mild),
                getString(R.string.moderate),
                getString(R.string.severe)
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, severities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSeverity.setAdapter(adapter);

        // VAS Slider
        sbIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvIntensityValue.setText(getString(R.string.discomfort_level) + ": " + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnEmergencyCall.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            } else {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + DOCTOR_PHONE));
                startActivity(callIntent);
            }
        });

        btnSubmitAdr.setOnClickListener(v -> submitReport());
    }

    private void submitReport() {
        String description = etDescription.getText().toString().trim();
        int severityPos = spinSeverity.getSelectedItemPosition();
        String severityKey = "Mild";
        if (severityPos == 1) severityKey = "Moderate";
        else if (severityPos == 2) severityKey = "Severe";

        int intensity = sbIntensity.getProgress();

        boolean isBleeding = cbBleeding.isChecked();
        boolean isBreathless = cbBreathlessness.isChecked();
        boolean isSeverePain = cbSeverePain.isChecked();

        if (description.isEmpty() && !isBleeding && !isBreathless) {
            Toast.makeText(this, "Please describe the problem or check a symptom.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitAdr.setEnabled(false);
        btnSubmitAdr.setText(getString(R.string.uploading));

        if (severityPos == 2 || isBleeding || isBreathless) {
            sendEmergencySms("RED FLAG WARNING: Bleeding=" + isBleeding + ", Breathless=" + isBreathless + ". Desc: " + description);
        }

        saveDataToDatabase(description, severityKey, intensity, isBleeding, isBreathless, isSeverePain);
    }

    private void saveDataToDatabase(String desc, String sev, int intensity, boolean bleed, boolean breath, boolean pain) {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        AdrReport report = new AdrReport(patientName, sev, desc, currentTime);
        report.intensity = intensity;
        report.hasBleeding = bleed;
        report.hasBreathlessness = breath;

        if (bleed || breath) {
            report.isSae = true;
        }

        DatabaseReference adrRef = FirebaseDatabase.getInstance().getReference("adrs").child(patientName);
        adrRef.push().setValue(report).addOnCompleteListener(task -> {
            Toast.makeText(this, getString(R.string.report_success), Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void sendEmergencySms(String fullDesc) {
        try {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + DOCTOR_PHONE));
            smsIntent.putExtra("sms_body", "EMERGENCY - " + patientName + ": " + fullDesc);
            startActivity(smsIntent);
        } catch (Exception e) {
            Toast.makeText(this, "SMS App Not Found", Toast.LENGTH_SHORT).show();
        }
    }
}
