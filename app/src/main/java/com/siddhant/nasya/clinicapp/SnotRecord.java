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

    // Current Medications
    public String antihistamines;
    public String nasalDecongestants;
    public String corticosteroids;
    public String otherMedications;

    // Follow-up, Compliance & Adverse Events
    public String daysCompleted;
    public String missedDoses;
    public String missedDosesReason;
    public String adverseEventSeverity;
    public String adverseEventDetails;
    public String treatmentResponse;
    public String patientSatisfaction;
    public String sustainedResponse;
    public String relapse;

    // Study Completion
    public String dateOfCompletion;
    public String reasonForDiscontinuation;
    public String investigatorName;

    // SNOT-22 Scores
    public int overallSnotTotal;
    public Map<String, Integer> symptomScores = new HashMap<>();

    public SnotRecord() {
        // Default constructor required for Firebase Realtime Database object deserialization
    }

    public SnotRecord(String trialId, String timestamp, String visitInterval) {
        this.trialId = trialId;
        this.timestamp = timestamp;
        this.visitInterval = visitInterval;
    }
}