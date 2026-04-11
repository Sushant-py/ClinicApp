package com.siddhant.nasya.clinicapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddPatientActivity extends AppCompatActivity {

    EditText etId, etName, etAge, etPhone, etPin;
    Spinner spinSex;
    Button btnSave;
    ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        etId = findViewById(R.id.etNewPatientId);
        etName = findViewById(R.id.etNewPatientName);
        etAge = findViewById(R.id.etNewPatientAge);
        etPhone = findViewById(R.id.etNewPatientPhone);
        etPin = findViewById(R.id.etNewPatientPin);
        spinSex = findViewById(R.id.spinNewPatientSex);
        btnSave = findViewById(R.id.btnSavePatient);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        String[] sexOptions = {"M", "F", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sexOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSex.setAdapter(adapter);

        btnSave.setOnClickListener(v -> savePatient());
    }

    private void savePatient() {
        String id = etId.getText().toString().trim().toUpperCase();
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String pin = etPin.getText().toString().trim();
        String sex = spinSex.getSelectedItem().toString();

        if (id.isEmpty() || name.isEmpty() || pin.isEmpty()) {
            Toast.makeText(this, "ID, Name, and PIN are required", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(id);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("trialId", id);
        userMap.put("fullName", name);
        userMap.put("age", Integer.parseInt(age.isEmpty() ? "0" : age));
        userMap.put("phoneNumber", phone);
        userMap.put("pin", pin);
        userMap.put("sex", sex);
        userMap.put("role", "Participant");
        userMap.put("hasConsented", false); 

        userRef.setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Patient Registered Successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to register patient", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
