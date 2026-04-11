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

    private static final String DOCTOR_PHONE = "5556667777"; // Replace with actual number

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

        String[] severities = {"Mild", "Moderate", "Severe"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, severities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSeverity.setAdapter(adapter);

        // VAS Slider
        sbIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvIntensityValue.setText("Discomfort Level: " + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 1. EMERGENCY CALL LOGIC
        btnEmergencyCall.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            } else {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + DOCTOR_PHONE));
                startActivity(callIntent);
            }
        });

        // 2. SUBMIT LOGIC
        btnSubmitAdr.setOnClickListener(v -> submitReport());
    }

    private void submitReport() {
        String description = etDescription.getText().toString().trim();
        String severity = spinSeverity.getSelectedItem().toString();
        int intensity = sbIntensity.getProgress();

        boolean isBleeding = cbBleeding.isChecked();
        boolean isBreathless = cbBreathlessness.isChecked();
        boolean isSeverePain = cbSeverePain.isChecked();

        if (description.isEmpty() && !isBleeding && !isBreathless) {
            Toast.makeText(this, "Please describe the problem or check a symptom.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitAdr.setEnabled(false);
        btnSubmitAdr.setText("Uploading...");

        // RED FLAG ESCALATION: Auto-trigger SMS if critical signs are checked
        if (severity.equals("Severe") || isBleeding || isBreathless) {
            sendEmergencySms("RED FLAG WARNING: Bleeding=" + isBleeding + ", Breathless=" + isBreathless + ". Desc: " + description);
        }

        saveDataToDatabase(description, severity, intensity, isBleeding, isBreathless, isSeverePain);
    }

    private void saveDataToDatabase(String desc, String sev, int intensity, boolean bleed, boolean breath, boolean pain) {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        AdrReport report = new AdrReport(patientName, sev, desc, currentTime);
        report.intensity = intensity;
        report.hasBleeding = bleed;
        report.hasBreathlessness = breath;

        // Auto-flag as SAE (Serious Adverse Event) if critical signs exist
        if (bleed || breath) {
            report.isSae = true;
        }

        DatabaseReference adrRef = FirebaseDatabase.getInstance().getReference("adrs").child(patientName);
        adrRef.push().setValue(report).addOnCompleteListener(task -> {
            Toast.makeText(this, "Report Submitted Successfully", Toast.LENGTH_LONG).show();
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
