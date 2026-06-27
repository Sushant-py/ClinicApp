package com.siddhant.nasya.clinicapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddPatientActivity extends AppCompatActivity {

    EditText etId, etName, etAge, etPhone, etPin;
    Spinner spinSex, spinEducation;
    Button btnSave;
    ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Respect language preference
        LanguageHelper.loadLocale();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        // Core Fields
        etId = findViewById(R.id.etNewPatientId);
        etName = findViewById(R.id.etNewPatientName);
        etAge = findViewById(R.id.etNewPatientAge);
        etPhone = findViewById(R.id.etNewPatientPhone);
        etPin = findViewById(R.id.etNewPatientPin);
        spinSex = findViewById(R.id.spinNewPatientSex);
        spinEducation = findViewById(R.id.spinEducation);
        btnSave = findViewById(R.id.btnSavePatient);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Visibility Toggles for Detailed Fields
        setupVisibilityToggle(R.id.cbFood, R.id.etFoodDetails);
        setupVisibilityToggle(R.id.cbOtherAllergy, R.id.etOtherAllergyDetails);
        setupVisibilityToggle(R.id.cbAntihistamines, R.id.etAntiDetails);
        setupVisibilityToggle(R.id.cbDecongestants, R.id.etDecongestantsDetails);
        setupVisibilityToggle(R.id.cbCorticosteroids, R.id.etCorticosteroidsDetails);
        setupVisibilityToggle(R.id.cbOtherMeds, R.id.etOtherMedsDetails);

        // Setup Spinners
        String[] sexOptions = {"M", "F", "Other"};
        ArrayAdapter<String> sexAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sexOptions);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSex.setAdapter(sexAdapter);

        String[] eduOptions = {"Primary", "Secondary", "Graduate", "Post Graduate", "None"};
        ArrayAdapter<String> eduAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eduOptions);
        eduAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinEducation.setAdapter(eduAdapter);

        btnSave.setOnClickListener(v -> savePatient());
    }

    private void setupVisibilityToggle(int checkBoxId, int editTextId) {
        CheckBox cb = findViewById(checkBoxId);
        EditText et = findViewById(editTextId);
        if (cb != null && et != null) {
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                et.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void savePatient() {
        String id = etId.getText().toString().trim().toUpperCase();
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String pin = etPin.getText().toString().trim();

        if (id.isEmpty() || name.isEmpty() || pin.isEmpty()) {
            Toast.makeText(this, "ID, Name, and PIN are required", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(id);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("trialId", id);
        userMap.put("fullName", name);
        userMap.put("age", ageStr.isEmpty() ? 0 : Integer.parseInt(ageStr));
        userMap.put("phoneNumber", phone);
        userMap.put("pin", pin);
        userMap.put("sex", spinSex.getSelectedItem().toString());
        userMap.put("role", "Participant");
        userMap.put("hasConsented", false);

        // Detailed Clinical Data
        userMap.put("hospitalCenter", ((EditText) findViewById(R.id.etHospitalCenter)).getText().toString().trim());

        RadioGroup rgRand = findViewById(R.id.rgRandomization);
        userMap.put("randomizationGroup", rgRand.getCheckedRadioButtonId() == R.id.rbIntervention ? "Intervention" : "Control");

        userMap.put("address", ((EditText) findViewById(R.id.etAddress)).getText().toString().trim());
        userMap.put("occupation", ((EditText) findViewById(R.id.etOccupation)).getText().toString().trim());
        userMap.put("education", spinEducation.getSelectedItem().toString());

        userMap.put("arDurationYears", ((EditText) findViewById(R.id.etDurationYears)).getText().toString().trim());
        userMap.put("arDurationMonths", ((EditText) findViewById(R.id.etDurationMonths)).getText().toString().trim());

        RadioGroup rgFam = findViewById(R.id.rgFamilyHistory);
        userMap.put("familyHistoryAR", rgFam.getCheckedRadioButtonId() == R.id.rbFamYes ? "Yes" : "No");

        // Allergies
        userMap.put("allergyDust", ((CheckBox) findViewById(R.id.cbDust)).isChecked());
        userMap.put("allergyPollen", ((CheckBox) findViewById(R.id.cbPollen)).isChecked());
        userMap.put("allergyPets", ((CheckBox) findViewById(R.id.cbPets)).isChecked());
        userMap.put("allergyFood", ((CheckBox) findViewById(R.id.cbFood)).isChecked());
        userMap.put("allergyFoodDetails", ((EditText) findViewById(R.id.etFoodDetails)).getText().toString().trim());
        userMap.put("allergyOther", ((CheckBox) findViewById(R.id.cbOtherAllergy)).isChecked());
        userMap.put("allergyOtherDetails", ((EditText) findViewById(R.id.etOtherAllergyDetails)).getText().toString().trim());

        // Medications
        userMap.put("medAntihistamines", ((CheckBox) findViewById(R.id.cbAntihistamines)).isChecked());
        userMap.put("medAntihistaminesDetails", ((EditText) findViewById(R.id.etAntiDetails)).getText().toString().trim());
        userMap.put("medDecongestants", ((CheckBox) findViewById(R.id.cbDecongestants)).isChecked());
        userMap.put("medDecongestantsDetails", ((EditText) findViewById(R.id.etDecongestantsDetails)).getText().toString().trim());
        userMap.put("medCorticosteroids", ((CheckBox) findViewById(R.id.cbCorticosteroids)).isChecked());
        userMap.put("medCorticosteroidsDetails", ((EditText) findViewById(R.id.etCorticosteroidsDetails)).getText().toString().trim());
        userMap.put("medOthers", ((CheckBox) findViewById(R.id.cbOtherMeds)).isChecked());
        userMap.put("medOthersDetails", ((EditText) findViewById(R.id.etOtherMedsDetails)).getText().toString().trim());

        userRef.setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Patient Registered Successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to register: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}