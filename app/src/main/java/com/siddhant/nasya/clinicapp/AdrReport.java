package com.siddhant.nasya.clinicapp;

public class AdrReport {
    public String patientName;
    public String severity;
    public String description;
    public String timestamp;
    public int intensity;

    // REGULATORY FIELDS (Associated Signs)
    public boolean hasBleeding;
    public boolean hasBreathlessness;

    // INVESTIGATOR ASSESSMENT FIELDS (For Monitor Dashboard)
    public String investigatorRelationship;
    public String investigatorAction;
    public String investigatorOutcome;
    public boolean isSae;

    public AdrReport() {}

    public AdrReport(String patientName, String severity, String description, String timestamp) {
        this.patientName = patientName;
        this.severity = severity;
        this.description = description;
        this.timestamp = timestamp;
        this.investigatorRelationship = "Pending Assessment";
        this.investigatorAction = "Pending";
        this.investigatorOutcome = "Pending";
        this.isSae = false;
    }
}