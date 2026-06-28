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
    private static String mPatientId = "";
    private static final int LEFT_MARGIN = 40;
    private static final int RIGHT_MARGIN = 555;

    public static void generatePatientPdfReport(Context context, String patientId,
                                                String profileText, String dosesText,
                                                List<DataSnapshot> adrSnapshots,
                                                List<DataSnapshot> symptomSnapshots,
                                                List<DataSnapshot> snotSnapshots) {

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        pageNumber = 1;
        mPatientId = patientId;

        // Start Page 1
        currentPage = startNewPage(pdfDocument);
        currentCanvas = currentPage.getCanvas();

        // 1. Branding Header
        paint.setColor(Color.parseColor("#3F51B5"));
        currentCanvas.drawRect(0, 0, 595, 80, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(22f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        currentCanvas.drawText("CLINICAL TRIAL: COMPREHENSIVE PATIENT RECORD", 30, 45, paint);

        paint.setTextSize(10f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        String currentTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        currentCanvas.drawText("Official Report | Generated: " + currentTimestamp, 30, 65, paint);

        currentY = 110;

        // 2. Demographic Profile Section
        drawSectionHeader(pdfDocument, paint, "1. PARTICIPANT DEMOGRAPHICS");
        drawWrappedText(pdfDocument, paint, profileText);

        // 3. Adherence Overview
        drawSectionHeader(pdfDocument, paint, "2. DOSE ADHERENCE STATUS");
        drawWrappedText(pdfDocument, paint, dosesText);
        currentY += 10;

        // 4. Detailed Nasal Symptom Evaluations
        drawSectionHeader(pdfDocument, paint, "3. NASAL SYMPTOM EVALUATION LOGS");
        if (symptomSnapshots.isEmpty()) {
            drawWrappedText(pdfDocument, paint, "No clinical symptom records found.");
        } else {
            for (DataSnapshot snap : symptomSnapshots) {
                checkPageOverflow(pdfDocument, 100);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                drawWrappedText(pdfDocument, paint, "Visit: " + snap.getKey() + " (" + snap.child("timestamp").getValue() + ")");
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                
                String scores = "Congestion: " + snap.child("congestion").getValue() + ", " +
                                "Rhinorrhea: " + snap.child("rhinorrhea").getValue() + ", " +
                                "Sneezing: " + snap.child("sneezing").getValue() + ", " +
                                "Itching: " + snap.child("itching").getValue() + ", " +
                                "Post-Nasal: " + snap.child("postNasalDrip").getValue();
                drawWrappedText(pdfDocument, paint, "  • Core (0-3): " + scores);
                
                String extended = "Loss of Smell: " + snap.child("lossOfSmell").getValue() + ", " +
                                  "Eye: " + snap.child("eyeSymptoms").getValue() + ", " +
                                  "Sleep: " + snap.child("sleepDisturbance").getValue();
                drawWrappedText(pdfDocument, paint, "  • Ext (0-3): " + extended);
                
                paint.setColor(Color.parseColor("#D32F2F"));
                drawWrappedText(pdfDocument, paint, "  >> Total Nasal Score: " + snap.child("totalNasalScore").getValue() + "/15 | Extended: " + snap.child("totalExtendedScore").getValue() + "/24");
                paint.setColor(Color.BLACK);
                currentY += 8;
            }
        }

        // 5. SNOT-22 & Detailed Audit History
        drawSectionHeader(pdfDocument, paint, "4. SNOT-22 & CLINICAL AUDIT HISTORY");
        if (snotSnapshots.isEmpty()) {
            drawWrappedText(pdfDocument, paint, "No SNOT-22 survey data recorded.");
        } else {
            for (DataSnapshot snap : snotSnapshots) {
                checkPageOverflow(pdfDocument, 150);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                drawWrappedText(pdfDocument, paint, "Assessment Stage: " + snap.child("visitInterval").getValue() + " (" + snap.child("timestamp").getValue() + ")");
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

                drawWrappedText(pdfDocument, paint, "  • SNOT-22 TOTAL: " + snap.child("overallSnotTotal").getValue() + " / 110");
                drawWrappedText(pdfDocument, paint, "  • Medical History: Duration: " + snap.child("durationOfRhinitis").getValue() + " | Family History: " + snap.child("familyHistory").getValue());
                drawWrappedText(pdfDocument, paint, "  • Allergies: " + snap.child("allergies").getValue());
                
                String meds = "Anti-H: " + snap.child("antihistamines").getValue() + 
                              " | Decong: " + snap.child("nasalDecongestants").getValue() + 
                              " | Steroids: " + snap.child("corticosteroids").getValue();
                drawWrappedText(pdfDocument, paint, "  • Medications: " + meds);
                
                drawWrappedText(pdfDocument, paint, "  • Adherence: " + snap.child("daysCompleted").getValue() + " days completed. Missed: " + snap.child("missedDoses").getValue() + " (" + snap.child("missedDosesReason").getValue() + ")");
                drawWrappedText(pdfDocument, paint, "  • Adverse Events: Severity: " + snap.child("adverseEventSeverity").getValue() + " | Details: " + snap.child("adverseEventDetails").getValue());
                drawWrappedText(pdfDocument, paint, "  • Outcome: Response: " + snap.child("treatmentResponse").getValue() + " | Satisfaction: " + snap.child("patientSatisfaction").getValue());
                
                if (snap.hasChild("reasonForDiscontinuation")) {
                     drawWrappedText(pdfDocument, paint, "  • DISCONTINUATION: " + snap.child("reasonForDiscontinuation").getValue());
                }
                drawWrappedText(pdfDocument, paint, "  • Investigator Signature: " + snap.child("investigatorName").getValue());
                
                currentY += 12;
            }
        }

        // 6. ADR Section
        drawSectionHeader(pdfDocument, paint, "5. REPORTED ADVERSE DRUG REACTIONS (ADRs)");
        if (adrSnapshots.isEmpty()) {
            drawWrappedText(pdfDocument, paint, "No ADRs reported for this participant.");
        } else {
            for (DataSnapshot snap : adrSnapshots) {
                checkPageOverflow(pdfDocument, 60);
                String desc = snap.child("description").getValue(String.class);
                String sev = snap.child("severity").getValue(String.class);
                String date = snap.child("timestamp").getValue(String.class);
                drawWrappedText(pdfDocument, paint, "• [" + date + "] SEVERITY: " + sev);
                drawWrappedText(pdfDocument, paint, "  Description: " + desc);
                currentY += 5;
            }
        }

        drawFooter();
        pdfDocument.finishPage(currentPage);

        // Save File
        File pdfFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File pdfFile = new File(pdfFolder, "Trial_Report_" + patientId + "_" + System.currentTimeMillis() + ".pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Toast.makeText(context, "Report Generated: " + pdfFile.getName(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }

    private static void drawSectionHeader(PdfDocument doc, Paint paint, String title) {
        checkPageOverflow(doc, 45);
        currentY += 15;
        paint.setTextSize(14f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(Color.parseColor("#3F51B5"));
        currentCanvas.drawText(title, LEFT_MARGIN, currentY, paint);
        currentY += 5;
        paint.setColor(Color.parseColor("#CCCCCC"));
        currentCanvas.drawLine(LEFT_MARGIN, currentY, RIGHT_MARGIN, currentY, paint);
        currentY += 15;
        paint.setColor(Color.BLACK);
        paint.setTextSize(11f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
    }

    private static void drawWrappedText(PdfDocument doc, Paint paint, String text) {
        if (text == null) return;
        
        String[] lines = text.split("\n");
        for (String lineText : lines) {
            if (lineText.trim().isEmpty() || lineText.contains("==")) continue;
            
            int maxWidth = RIGHT_MARGIN - LEFT_MARGIN;
            String[] words = lineText.split(" ");
            StringBuilder currentLine = new StringBuilder();
            
            for (String word : words) {
                String testLine = currentLine.length() == 0 ? word : currentLine.toString() + " " + word;
                float width = paint.measureText(testLine);
                
                if (width > maxWidth) {
                    checkPageOverflow(doc, 20);
                    currentCanvas.drawText(currentLine.toString(), LEFT_MARGIN, currentY, paint);
                    currentY += 16;
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }
            
            if (currentLine.length() > 0) {
                checkPageOverflow(doc, 20);
                currentCanvas.drawText(currentLine.toString(), LEFT_MARGIN, currentY, paint);
                currentY += 16;
            }
        }
    }

    private static PdfDocument.Page startNewPage(PdfDocument doc) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
        PdfDocument.Page page = doc.startPage(pageInfo);
        pageNumber++;
        return page;
    }

    private static void checkPageOverflow(PdfDocument doc, int requiredSpace) {
        if (currentY + requiredSpace > 790) {
            drawFooter();
            if (currentPage != null) {
                doc.finishPage(currentPage);
            }
            currentPage = startNewPage(doc);
            currentCanvas = currentPage.getCanvas();
            currentY = 60;
        }
    }

    private static void drawFooter() {
        Paint p = new Paint();
        p.setTextSize(8f);
        p.setColor(Color.GRAY);
        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        currentCanvas.drawText("Confidential Clinical Record | ID: " + mPatientId + " | Page " + (pageNumber - 1), LEFT_MARGIN, 820, p);
    }
}