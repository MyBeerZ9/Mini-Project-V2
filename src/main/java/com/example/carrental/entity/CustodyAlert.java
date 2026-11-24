package com.example.carrental.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "custody_alerts")
public class CustodyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "alert_type", nullable = false)
    private String alertType; // OVERDUE_SLA, GEOFENCE_BREACH, TRACKER_OFFLINE, THEFT_SUSPECTED

    @Column(name = "severity", nullable = false)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "location_snapshot")
    private String locationSnapshot;

    @Column(name = "details", length = 1000)
    private String details;

    @Column(name = "resolved", nullable = false)
    private Boolean resolved = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CustodyAlert() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.resolved == null) {
            this.resolved = false;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
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
