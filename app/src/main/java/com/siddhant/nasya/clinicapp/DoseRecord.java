package com.siddhant.nasya.clinicapp;

public class DoseRecord {
    public String patientName;
    public String timestamp;
    public String status;

    // THE NEW VARIABLE
    public String doseType;

    // 1. Firebase REQUIRES an empty constructor
    public DoseRecord() {
    }

    // 2. THE FIX: The constructor that accepts all FOUR strings
    public DoseRecord(String patientName, String timestamp, String status, String doseType) {
        this.patientName = patientName;
        this.timestamp = timestamp;
        this.status = status;
        this.doseType = doseType;
    }
}