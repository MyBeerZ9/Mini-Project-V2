package com.example.carrental.dto;

import com.example.carrental.entity.CustodyAlert;
import java.time.LocalDateTime;

public class CustodyAlertResponse {

    private Long id;
    private Long bookingId;
    private String alertType;
    private String severity;
    private String locationSnapshot;
    private String details;
    private Boolean resolved;
    private LocalDateTime createdAt;

    public CustodyAlertResponse() {
    }

    public static CustodyAlertResponse fromEntity(CustodyAlert alert) {
        if (alert == null) return null;
        CustodyAlertResponse resp = new CustodyAlertResponse();
        resp.setId(alert.getId());
        if (alert.getBooking() != null) {
            resp.setBookingId(alert.getBooking().getId());
        }
        resp.setAlertType(alert.getAlertType());
        resp.setSeverity(alert.getSeverity());
        resp.setLocationSnapshot(alert.getLocationSnapshot());
        resp.setDetails(alert.getDetails());
        resp.setResolved(alert.getResolved());
        resp.setCreatedAt(alert.getCreatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
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

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
