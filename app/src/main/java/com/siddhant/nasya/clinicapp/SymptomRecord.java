package com.siddhant.nasya.clinicapp;

public class SymptomRecord {
    public String trialId;
    public String timestamp;
    public String visitType;

    public int congestion;
    public int rhinorrhea;
    public int sneezing;
    public int itching;
    public int postNasalDrip;

    public int lossOfSmell;
    public int eyeSymptoms;
    public int sleepDisturbance;

    public int totalNasalScore;
    public int totalExtendedScore;

    public SymptomRecord() {}

    public SymptomRecord(String trialId, String timestamp, String visitType) {
        this.trialId = trialId;
        this.timestamp = timestamp;
        this.visitType = visitType;
    }
}