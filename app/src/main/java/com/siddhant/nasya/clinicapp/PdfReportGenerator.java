package com.siddhant.nasya.clinicapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfReportGenerator {

    private static int currentY = 110;
    private static PdfDocument.Page currentPage = null;
    private static Canvas currentCanvas = null;
    private static int pageNumber = 1;

    public static void generatePatientPdfReport(Context context, String patientId,
                                                String profileText, String dosesText,
                                                List<DataSnapshot> adrSnapshots,
                                                List<DataSnapshot> symptomSnapshots,
                                                List<DataSnapshot> snotSnapshots) {

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        pageNumber = 1;

        // Start Page 1
        currentPage = startNewPage(pdfDocument);
        currentCanvas = currentPage.getCanvas();

        // 1. Draw PDF Branding Header Bar
        paint.setColor(Color.parseColor("#3F51B5"));
        currentCanvas.drawRect(0, 0, 595, 80, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(20f); // Fixed: Replaced '20sp' with standard float literal
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        currentCanvas.drawText("CLINICAL TRIAL PATIENT REPORT", 30, 45, paint);

        paint.setTextSize(10f); // Fixed: Replaced '10sp' with float literal
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        String currentTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        currentCanvas.drawText("Generated on: " + currentTimestamp, 30, 65, paint);

        currentY = 110;

        // 2. Section: Demographic Profile
        checkPageOverflow(pdfDocument, 40);
        paint.setTextSize(14f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(Color.parseColor("#3F51B5"));
        currentCanvas.drawText("1. Participant Demographics & Trial Context", 30, currentY, paint);
        currentY += 6;
        paint.setColor(Color.parseColor("#CCCCCC"));
        currentCanvas.drawLine(30, currentY, 565, currentY, paint);
        currentY += 18;

        paint.setColor(Color.BLACK);
        paint.setTextSize(11f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        String[] profileLines = profileText.split("\n");
        for (String line : profileLines) {
            if (!line.trim().isEmpty() && !line.contains("==")) {
                checkPageOverflow(pdfDocument, 20);
                currentCanvas.drawText(line, 35, currentY, paint);
                currentY += 16;
            }
        }

        // 3. Section: Adherence Overview
        checkPageOverflow(pdfDocument, 40);
        currentY += 10;
        paint.setTextSize(14f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(Color.parseColor("#3F51B5"));
        currentCanvas.drawText("2. Dose Adherence Audit", 30, currentY, paint);
        currentY += 6;
        paint.setColor(Color.parseColor("#CCCCCC"));
        currentCanvas.drawLine(30, currentY, 565, currentY, paint);
        currentY += 18;

        paint.setColor(Color.BLACK);
        paint.setTextSize(11f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        checkPageOverflow(pdfDocument, 20);
        currentCanvas.drawText(dosesText.replace("\n", ""), 35, currentY, paint);
        currentY += 24;

        // 4. Section: Symptom Scores Timeline Table
        checkPageOverflow(pdfDocument, 50);
        paint.setTextSize(14f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(Color.parseColor("#3F51B5"));
        currentCanvas.drawText("3. Nasal Symptom Evaluation Sheets Tracker", 30, currentY, paint);
        currentY += 6;
        paint.setColor(Color.parseColor("#CCCCCC"));
        currentCanvas.drawLine(30, currentY, 565, currentY, paint);
        currentY += 18;

        // Draw Table Header
        paint.setColor(Color.parseColor("#E0E0E0"));
        currentCanvas.drawRect(30, currentY, 565, currentY + 20, paint);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        currentCanvas.drawText("Assessment Visit", 35, currentY + 14, paint);
        currentCanvas.drawText("Total Nasal Score (/15)", 200, currentY + 14, paint);
        currentCanvas.drawText("Total Extended Score (/24)", 380, currentY + 14, paint);
        currentY += 20;

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        if (symptomSnapshots.isEmpty()) {
            checkPageOverflow(pdfDocument, 20);
            currentCanvas.drawText("No evaluation sheets logged for this participant.", 35, currentY + 15, paint);
            currentY += 25;
        } else {
            for (DataSnapshot snap : symptomSnapshots) {
                checkPageOverflow(pdfDocument, 20);
                currentCanvas.drawText(String.valueOf(snap.getKey()), 35, currentY + 15, paint);
                currentCanvas.drawText(String.valueOf(snap.child("totalNasalScore").getValue()), 200, currentY + 15, paint);
                currentCanvas.drawText(String.valueOf(snap.child("totalExtendedScore").getValue()), 380, currentY + 15, paint);
                currentY += 20;
            }
        }

        // 5. Section: SNOT-22 Records
        checkPageOverflow(pdfDocument, 50);
        currentY += 10;
        paint.setTextSize(14f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(Color.parseColor("#3F51B5"));
        currentCanvas.drawText("4. Sino-Nasal Outcome Test (SNOT-22) History", 30, currentY, paint);
        currentY += 6;
        paint.setColor(Color.parseColor("#CCCCCC"));
        currentCanvas.drawLine(30, currentY, 565, currentY, paint);
        currentY += 18;

        // Draw Table Header
        paint.setColor(Color.parseColor("#E0E0E0"));
        currentCanvas.drawRect(30, currentY, 565, currentY + 20, paint);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        currentCanvas.drawText("Assessment Interval", 35, currentY + 14, paint);
        currentCanvas.drawText("Aggregated SNOT Total Score Matrix", 250, currentY + 14, paint);
        currentY += 20;

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        if (snotSnapshots.isEmpty()) {
            checkPageOverflow(pdfDocument, 20);
            currentCanvas.drawText("No SNOT-22 surveys logged for this participant.", 35, currentY + 15, paint);
            currentY += 25;
        } else {
            for (DataSnapshot snap : snotSnapshots) {
                checkPageOverflow(pdfDocument, 20);
                String label = snap.child("assessmentPeriod").getValue(String.class);
                if (label == null) label = "Routine Check";
                currentCanvas.drawText(label, 35, currentY + 15, paint);
                currentCanvas.drawText(snap.child("overallSnotTotal").getValue() + " / 110 Points", 250, currentY + 15, paint);
                currentY += 20;
            }
        }

        pdfDocument.finishPage(currentPage);

        // File Writing Logic
        File pdfFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File pdfFile = new File(pdfFolder, "Patient_" + patientId + "_Clinical_Summary.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Toast.makeText(context, "PDF saved to Downloads folder!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error printing PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }

    private static PdfDocument.Page startNewPage(PdfDocument doc) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
        PdfDocument.Page page = doc.startPage(pageInfo);
        pageNumber++;
        return page;
    }

    private static void checkPageOverflow(PdfDocument doc, int requiredSpace) {
        // If content height reaches near the bottom of standard A4 canvas boundaries (842 points)
        if (currentY + requiredSpace > 790) {
            if (currentPage != null) {
                doc.finishPage(currentPage);
            }
            currentPage = startNewPage(doc);
            currentCanvas = currentPage.getCanvas();
            currentY = 50; // Reset top y margin layout anchor on next sheet page
        }
    }
}