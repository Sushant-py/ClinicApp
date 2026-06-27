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

        // Comprehensive HTML Formatted User Manual
        String manualText = "<h2>Welcome to the Clinical Trial Application</h2>" +
                "<p>This application is designed to monitor the preventive efficacy of Pratimarsha Nasya with Anu Taila in adults with Allergic Rhinitis. Below is the complete guide for both Participants (Patients) and Investigators (Doctors).</p>" +

                "<hr><br>" +

                "<h3>🟢 PART 1: PARTICIPANT (PATIENT) GUIDE</h3>" +
                "<b>1. Logging In</b><br>" +
                "Use the unique Trial ID provided by your investigator to log into the application.<br><br>" +

                "<b>2. Daily Dose Tracking</b><br>" +
                "• <b>Morning Dose:</b> Apply your nasal drops at 8:00 AM. Click the 'Morning Dose' button to log compliance.<br>" +
                "• <b>Evening Dose:</b> Apply your nasal drops at 9:00 PM. Click the 'Evening Dose' button to log compliance.<br>" +
                "<i>Note: Buttons will turn orange if a dose is overdue, and green once successfully logged.</i><br><br>" +

                "<b>3. Daily Diary</b><br>" +
                "Use the 'Daily Diary' feature to record any daily notes, mild changes in symptoms, or general wellness tracking for your investigator to review.<br><br>" +

                "<b>4. Reporting a Problem (ADR)</b><br>" +
                "If you experience any Adverse Drug Reactions (e.g., severe burning, headache, nosebleeds), click 'Report a Problem' immediately. Select the severity and describe the issue. This will instantly alert your investigator.<br><br>" +

                "<b>5. Instructional Video</b><br>" +
                "If you forget how to administer the drops, click 'View Tutorial' to watch the standardized administration procedure.<br><br>" +

                "<hr><br>" +

                "<h3>🔵 PART 2: INVESTIGATOR (DOCTOR) GUIDE</h3>" +
                "<b>1. The Monitor Dashboard</b><br>" +
                "The investigator dashboard provides complete oversight of patient adherence and clinical evaluations.<br><br>" +

                "<b>2. Registering a New Patient</b><br>" +
                "Click 'Register New Participant' to add a patient to the trial database. You must record their demographics, medical history, and trial arm allocation.<br><br>" +

                "<b>3. Clinical Evaluations</b><br>" +
                "During Patient Visits (Baseline, Week 1, 2, 4, 8), you are required to submit two forms:<br>" +
                "• <b>Nasal Symptom Evaluation:</b> A 0-3 grading scale tracking 5 core symptoms (out of 15) and 3 extended symptoms (out of 24).<br>" +
                "• <b>SNOT-22 Health Survey:</b> A comprehensive 22-parameter survey utilizing a 0-5 severity scale (Total out of 110).<br>" +
                "<i>You must input the Patient's Trial ID manually at the top of these forms to link the data correctly.</i><br><br>" +

                "<b>4. Pulling Patient Records</b><br>" +
                "Enter a patient's Trial ID into the search bar and click 'Pull Record'. This will retrieve:<br>" +
                "• Patient Demographics.<br>" +
                "• Overall Dose Adherence Percentage.<br>" +
                "• Any Pending Adverse Events (ADRs).<br>" +
                "• Chronological timeline of all Nasal Symptom Scores and SNOT-22 totals.<br><br>" +

                "<b>5. Managing Adverse Events (ADRs)</b><br>" +
                "If an ADR is marked as [ACTION REQUIRED], investigate the issue clinically. Once resolved, click 'Review ADRs' on the dashboard to clear the pending queue.<br><br>" +

                "<b>6. Data Auditing & PDF Exports</b><br>" +
                "• <b>Run Audit:</b> Click this to automatically scan a patient's record for missing timestamps, skipped doses, or blank ADR descriptions.<br>" +
                "• <b>Download PDF:</b> Once a patient record is pulled, an orange 'Download PDF' button will appear. Click this to generate and save a formal clinical summary document to your device's Downloads folder.<br><br>" +

                "<hr><br>" +
                "<p><i>For technical support regarding the application structure or database sync issues, contact the system administrator.</i></p>";

        // Render HTML securely into the TextView
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvManualContent.setText(Html.fromHtml(manualText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvManualContent.setText(Html.fromHtml(manualText));
        }
    }
}