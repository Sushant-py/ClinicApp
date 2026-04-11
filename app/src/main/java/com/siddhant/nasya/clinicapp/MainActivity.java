package com.siddhant.nasya.clinicapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    EditText etTrialId, etPin;
    Spinner spinRole;
    Button btnLogin;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("TrialPrefs", Context.MODE_PRIVATE);

        etTrialId = findViewById(R.id.etTrialId);
        etPin = findViewById(R.id.etPin);
        spinRole = findViewById(R.id.spinRole);
        btnLogin = findViewById(R.id.btnLogin);

        String[] roles = {"Participant", "Monitor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinRole.setAdapter(adapter);

        btnLogin.setOnClickListener(v -> attemptLogin());

        // AUTO-LOGIN: If they have a saved session, route them immediately. No fingerprints.
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
            Toast.makeText(this, "Enter ID and PIN", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(MainActivity.this, "Wrong Role Selected", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
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
            // Checks Eligibility and Consent perfectly.
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