package com.siddhant.nasya.clinicapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PatientActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    TextView tvWelcome;
    Button btnLogout;
    Button btnMorning, btnEvening, btnReportProblem, btnDailyDiary, btnViewTutorial;
    String trialId;
    SharedPreferences prefs;
    String todayDate;

    private boolean isNavigatingToInternalActivity = false;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.loadLocale();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        tts = new TextToSpeech(this, this);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        btnMorning = findViewById(R.id.btnMorning);
        btnEvening = findViewById(R.id.btnEvening);
        btnReportProblem = findViewById(R.id.btnReportProblem);
        btnDailyDiary = findViewById(R.id.btnDailyDiary);
        btnViewTutorial = findViewById(R.id.btnViewTutorial);

        prefs = getSharedPreferences("TrialPrefs", Context.MODE_PRIVATE);
        trialId = prefs.getString("USER_NAME", "Participant");
        String realName = prefs.getString("FULL_NAME", "Participant");

        String welcomeBase = getString(R.string.welcome);
        tvWelcome.setText(welcomeBase + ", " + realName);

        requestAppPermissions();

        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (prefs.getBoolean(trialId + "_" + todayDate + "_Morning", false)) {
            lockButton(btnMorning, getString(R.string.morning));
        }
        if (prefs.getBoolean(trialId + "_" + todayDate + "_Evening", false)) {
            lockButton(btnEvening, getString(R.string.evening));
        }

        checkDatabaseForToday(todayDate);
        checkIfOverdue();
        scheduleDailyAlarms();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("TRIAL_CHANNEL", "Trial Alerts", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        checkConsecutiveMissedDoses();
        
        btnMorning.setOnClickListener(v -> showDoseConfirmationDialog("Morning Dose", btnMorning));
        btnEvening.setOnClickListener(v -> showDoseConfirmationDialog("Evening Dose", btnEvening));

        btnReportProblem.setOnClickListener(v -> {
            isNavigatingToInternalActivity = true;
            startActivity(new Intent(this, AdrActivity.class));
        });
        
        btnDailyDiary.setOnClickListener(v -> {
            isNavigatingToInternalActivity = true;
            startActivity(new Intent(this, DailyDiaryActivity.class));
        });
        
        btnViewTutorial.setOnClickListener(v -> {
            isNavigatingToInternalActivity = true;
            startActivity(new Intent(this, InstructionalVideoActivity.class));
        });

        btnLogout.setOnClickListener(v -> logoutUser());
    }

    @Override
    protected void onResume() {
        super.onResume();
        isNavigatingToInternalActivity = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Security layer: Auto-logout if user leaves the app
        if (!isChangingConfigurations() && !isNavigatingToInternalActivity) {
            logoutUser();
        }
    }

    private void logoutUser() {
        prefs.edit().clear().apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            String currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags();
            if (currentLang.contains("kn")) tts.setLanguage(new Locale("kn", "IN"));
            else if (currentLang.contains("hi")) tts.setLanguage(new Locale("hi", "IN"));
            else tts.setLanguage(Locale.ENGLISH);
        }
    }

    private void speakText(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void showDoseConfirmationDialog(String doseType, Button clickedButton) {
        String promptMessage = getString(R.string.applied_drops_prompt);
        speakText(promptMessage);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_admin))
                .setMessage(promptMessage)
                .setPositiveButton(getString(R.string.i_applied), (dialog, which) -> {
                    if (tts != null) tts.stop();
                    logDoseToFirebase(doseType, clickedButton);
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                    if (tts != null) tts.stop();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void checkIfOverdue() {
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        boolean morningLogged = prefs.getBoolean(trialId + "_" + todayDate + "_Morning", false);
        boolean eveningLogged = prefs.getBoolean(trialId + "_" + todayDate + "_Evening", false);

        if (currentHour >= 8 && !morningLogged) {
            btnMorning.setText(getString(R.string.morning) + " " + getString(R.string.overdue_suffix));
            btnMorning.setBackgroundColor(android.graphics.Color.parseColor("#FF9800"));
        }
        
        if (currentHour >= 21 && !eveningLogged) {
            btnEvening.setText(getString(R.string.evening) + " " + getString(R.string.overdue_suffix));
            btnEvening.setBackgroundColor(android.graphics.Color.parseColor("#FF9800"));
        }
    }

    private void checkConsecutiveMissedDoses() {
        DatabaseReference dosesRef = FirebaseDatabase.getInstance().getReference("doses").child(trialId);
        dosesRef.orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String lastDoseStr = ds.child("timestamp").getValue(String.class);
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            Date lastDoseDate = sdf.parse(lastDoseStr);
                            long diffInMillis = System.currentTimeMillis() - lastDoseDate.getTime();
                            long daysMissed = diffInMillis / (1000 * 60 * 60 * 24);

                            if (daysMissed >= 3) {
                                new AlertDialog.Builder(PatientActivity.this)
                                        .setTitle("⚠️ Trial Alert")
                                        .setMessage("We noticed 3 missed doses. Please contact the study team.")
                                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                        .setCancelable(false)
                                        .show();
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void requestAppPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.SEND_SMS}, 100);
            }
        }
    }

    private void checkDatabaseForToday(String today) {
        DatabaseReference dosesRef = FirebaseDatabase.getInstance().getReference("doses").child(trialId);
        dosesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String timestamp = ds.child("timestamp").getValue(String.class);
                        String type = ds.child("doseType").getValue(String.class);
                        if (timestamp != null && timestamp.contains(today)) {
                            if ("Morning Dose".equals(type)) {
                                lockButton(btnMorning, getString(R.string.morning));
                            } else if ("Evening Dose".equals(type)) {
                                lockButton(btnEvening, getString(R.string.evening));
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void logDoseToFirebase(String doseType, Button clickedButton) {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        DoseRecord newDose = new DoseRecord(trialId, currentTime, "Applied", doseType);
        FirebaseDatabase.getInstance().getReference("doses").child(trialId).push().setValue(newDose);
        
        String typeLabel = doseType.contains("Morning") ? getString(R.string.morning) : getString(R.string.evening);
        lockButton(clickedButton, typeLabel);
        saveLocalLock(doseType.contains("Morning") ? "Morning" : "Evening", todayDate);
    }

    private void lockButton(Button btn, String typeLabel) {
        btn.setText(typeLabel + " " + getString(R.string.logged_suffix));
        btn.setEnabled(false);
        btn.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
    }

    private void saveLocalLock(String type, String date) {
        prefs.edit().putBoolean(trialId + "_" + date + "_" + type, true).apply();
    }

    private void scheduleDailyAlarms() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentM = new Intent(this, DoseAlarmReceiver.class);
        intentM.putExtra("DOSE_TYPE", "Morning (8 AM)");
        PendingIntent morningIntent = PendingIntent.getBroadcast(this, 1, intentM, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calM = Calendar.getInstance();
        calM.set(Calendar.HOUR_OF_DAY, 8);
        calM.set(Calendar.MINUTE, 0);
        if (calM.getTimeInMillis() <= System.currentTimeMillis()) calM.add(Calendar.DAY_OF_YEAR, 1);
        if (alarmManager != null) alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calM.getTimeInMillis(), AlarmManager.INTERVAL_DAY, morningIntent);
    }
}
