package com.siddhant.nasya.clinicapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    EditText etTrialId, etPin;
    Spinner spinRole, spinLanguage;
    Button btnLogin;
    SharedPreferences prefs;
    
    private boolean isFirstSelection = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("TrialPrefs", Context.MODE_PRIVATE);

        etTrialId = findViewById(R.id.etTrialId);
        etPin = findViewById(R.id.etPin);
        spinRole = findViewById(R.id.spinRole);
        spinLanguage = findViewById(R.id.spinLanguage);
        btnLogin = findViewById(R.id.btnLogin);

        // 1. Setup Roles Spinner
        String[] roles = {"Participant", "Monitor"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinRole.setAdapter(roleAdapter);

        // 2. Setup Language Spinner
        String[] languages = {"English", "ಕನ್ನಡ (Kannada)", "हिन्दी (Hindi)"};
        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinLanguage.setAdapter(langAdapter);

        // Set spinner selection based on current saved language
        String currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        if (currentLang.contains("kn")) spinLanguage.setSelection(1);
        else if (currentLang.contains("hi")) spinLanguage.setSelection(2);
        else spinLanguage.setSelection(0);

        spinLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirstSelection) {
                    isFirstSelection = false;
                    return;
                }
                
                String selectedLang = "en";
                if (position == 1) selectedLang = "kn";
                else if (position == 2) selectedLang = "hi";

                if (!currentLang.contains(selectedLang)) {
                    // Pass MainActivity.this to satisfy the new setLocale(Activity, String) signature
                    LanguageHelper.setLocale(MainActivity.this, selectedLang);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnLogin.setOnClickListener(v -> attemptLogin());

        // AUTO-LOGIN: If they have a saved session, route them immediately.
        String savedId = prefs.getString("USER_NAME", "");
        if (!savedId.isEmpty()) {
            routeUser();
        }
    }

    private void attemptLogin() {
        String trialId = etTrialId.getText().toString().trim().toUpperCase();
        String enteredPin = etPin.getText().toString().trim();
        String selectedRole = spinRole.getSelectedItem().toString();

        if (trialId.isEmpty() || enteredPin.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_id_pin), Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(trialId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                btnLogin.setEnabled(true);
                if (snapshot.exists()) {
                    String dbPin = snapshot.child("pin").getValue(String.class);
                    String dbRole = snapshot.child("role").getValue(String.class);
                    String dbName = snapshot.child("fullName").getValue(String.class);
                    String dbPhone = snapshot.child("phoneNumber").getValue(String.class);

                    if (dbPin != null && dbPin.equals(enteredPin)) {
                        if (dbRole != null && !dbRole.equals(selectedRole)) {
                            Toast.makeText(MainActivity.this, getString(R.string.wrong_role), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("USER_NAME", trialId);
                        editor.putString("FULL_NAME", dbName);
                        editor.putString("USER_ROLE", selectedRole);
                        editor.putString("PHONE_NUMBER", dbPhone != null ? dbPhone : "");
                        editor.apply();

                        routeUser();
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnLogin.setEnabled(true);
            }
        });
    }

    private void routeUser() {
        String role = prefs.getString("USER_ROLE", "");
        Intent intent;

        if (role.equals("Monitor")) {
            intent = new Intent(MainActivity.this, MonitorActivity.class);
        } else {
            if (!prefs.getBoolean("IS_ELIGIBLE", false)) {
                intent = new Intent(MainActivity.this, EligibilityActivity.class);
            } else if (!prefs.getBoolean("HAS_CONSENTED", false)) {
                intent = new Intent(MainActivity.this, ConsentActivity.class);
            } else {
                intent = new Intent(MainActivity.this, PatientActivity.class);
            }
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
