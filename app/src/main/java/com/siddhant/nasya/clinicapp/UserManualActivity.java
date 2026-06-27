package com.siddhant.nasya.clinicapp;

import android.os.Bundle;
import android.text.Html;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class UserManualActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manual);

        ImageButton btnBack = findViewById(R.id.btnBackManual);
        btnBack.setOnClickListener(v -> finish());

        TextView tvManualContent = findViewById(R.id.tvManualContent);

        // Fetch the HTML text directly from strings.xml
        String manualText = getString(R.string.manual_content);

        // Render HTML securely into the TextView
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvManualContent.setText(Html.fromHtml(manualText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvManualContent.setText(Html.fromHtml(manualText));
        }
    }
}