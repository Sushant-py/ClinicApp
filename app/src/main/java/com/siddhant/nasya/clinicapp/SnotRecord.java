package com.siddhant.nasya.clinicapp;

import java.util.HashMap;
import java.util.Map;

public class SnotRecord {
    public String trialId;
    public String timestamp;
    public String assessmentPeriod; // <-- Fixed variable name
    public int overallSnotTotal;

    // Maps the specific symptom name to its 0-5 score
    public Map<String, Integer> symptomScores = new HashMap<>();

    public SnotRecord() {}

    public SnotRecord(String trialId, String timestamp, String assessmentPeriod) {
        this.trialId = trialId;
        this.timestamp = timestamp;
        this.assessmentPeriod = assessmentPeriod;
    }
}