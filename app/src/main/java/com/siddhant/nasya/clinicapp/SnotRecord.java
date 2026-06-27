package com.siddhant.nasya.clinicapp;

import java.util.HashMap;
import java.util.Map;

public class SnotRecord {
    public String trialId;
    public String timestamp;
    public String visitInterval;
    public String dateOfAssessment;
    public String hospitalCenter;
    public String randomizationGroup;

    // Demographics
    public String patientName;
    public String age;
    public String gender;
    public String contactNumber;
    public String address;
    public String occupation;
    public String education;

    // Medical History
    public String durationOfRhinitis;
    public String familyHistory;
    public String allergies;
    public String currentMedications;

    // Follow-up & Compliance
    public String daysCompleted;
    public String missedDoses;
    public String adverseEventsNote;
    public String treatmentResponse;
    public String patientSatisfaction;
    public String sustainedResponse;
    public String relapse;
    public String investigatorName;

    // SNOT-22 Data
    public int overallSnotTotal;
    public Map<String, Integer> symptomScores = new HashMap<>();

    public SnotRecord() {
        // Default constructor required for Firebase
    }

    public SnotRecord(String trialId, String timestamp, String visitInterval) {
        this.trialId = trialId;
        this.timestamp = timestamp;
        this.visitInterval = visitInterval;
    }
}