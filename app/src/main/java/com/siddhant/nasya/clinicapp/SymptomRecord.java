package com.siddhant.nasya.clinicapp;

public class SymptomRecord {
    public String patientId; // Renamed from trialId for clarity
    public String assessmentDate;
    public String assessmentTime;
    public String timestamp;
    public String visitType;

    // Core Symptoms (Items 1-5)
    public int congestion;
    public int rhinorrhea;
    public int sneezing;
    public int itching;
    public int postNasalDrip;

    // Additional Symptoms (Items 6-8)
    public int lossOfSmell;
    public int eyeSymptoms;
    public int sleepDisturbance;

    // Calculated Totals
    public int totalNasalScore; // Out of 15
    public int totalExtendedScore; // Out of 24

    public SymptomRecord() {}

    public SymptomRecord(String patientId, String timestamp, String visitType) {
        this.patientId = patientId;
        this.timestamp = timestamp;
        this.visitType = visitType;
    }
}