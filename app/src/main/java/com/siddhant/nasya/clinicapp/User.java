package com.siddhant.nasya.clinicapp;

public class User {
    public String fullName;
    public String trialId;
    public String pin;
    public String role;

    public int age;
    public String sex;
    public String comorbidities;
    public String baselineEntFindings;
    public String phoneNumber;

    // NEW REGULATORY FIELDS
    public String emergencyContact;
    public String randomizationArm; // e.g., "Group A" or "Group B"
    public String deviceId;         // For Device Binding

    public User() {}

    public User(String fullName, String trialId, String pin, String role, int age, String sex, String comorbidities, String baselineEntFindings, String phoneNumber, String emergencyContact, String randomizationArm, String deviceId) {
        this.fullName = fullName;
        this.trialId = trialId;
        this.pin = pin;
        this.role = role;
        this.age = age;
        this.sex = sex;
        this.comorbidities = comorbidities;
        this.baselineEntFindings = baselineEntFindings;
        this.phoneNumber = phoneNumber;
        this.emergencyContact = emergencyContact;
        this.randomizationArm = randomizationArm;
        this.deviceId = deviceId;
    }
}