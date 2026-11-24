package com.example.carrental.dto;

import jakarta.validation.constraints.NotBlank;

public class CustodyAlertRequest {

    @NotBlank(message = "Alert type is required (e.g. OVERDUE_SLA, GEOFENCE_BREACH, TRACKER_OFFLINE, THEFT_SUSPECTED)")
    private String alertType;

    @NotBlank(message = "Severity is required (LOW, MEDIUM, HIGH, CRITICAL)")
    private String severity;

    private String locationSnapshot;
    private String details;

    public CustodyAlertRequest() {
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getLocationSnapshot() {
        return locationSnapshot;
    }

    public void setLocationSnapshot(String locationSnapshot) {
        this.locationSnapshot = locationSnapshot;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
