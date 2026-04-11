package com.siddhant.nasya.clinicapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class IneligibleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ineligible);

        Button btnExit = findViewById(R.id.btnExit);
        TextView tvPiContact = findViewById(R.id.tvPiContact);

        // Allow user to call the PI directly
        tvPiContact.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:9876543210"));
            startActivity(intent);
        });

        btnExit.setOnClickListener(v -> finishAffinity());
    }
}