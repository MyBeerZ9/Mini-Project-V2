package com.example.carrental.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_inspections")
public class VehicleInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "inspection_type", nullable = false)
    private String inspectionType; // PRE_TRIP, POST_TRIP

    @Column(name = "odometer_km", nullable = false)
    private Integer odometerKm;

    @Column(name = "fuel_or_battery_percent", nullable = false)
    private Integer fuelOrBatteryPercent;

    @Column(name = "condition_notes", length = 1000)
    private String conditionNotes;

    @Column(name = "evidence_hash", nullable = false)
    private String evidenceHash;

    @Column(name = "inspected_by", nullable = false)
    private String inspectedBy;

    @Column(name = "inspected_at", nullable = false)
    private LocalDateTime inspectedAt;

    public VehicleInspection() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.inspectedAt == null) {
            this.inspectedAt = LocalDateTime.now();
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

    public String getInspectionType() {
        return inspectionType;
    }

    public void setInspectionType(String inspectionType) {
        this.inspectionType = inspectionType;
    }

    public Integer getOdometerKm() {
        return odometerKm;
    }

    public void setOdometerKm(Integer odometerKm) {
        this.odometerKm = odometerKm;
    }

    public Integer getFuelOrBatteryPercent() {
        return fuelOrBatteryPercent;
    }

    public void setFuelOrBatteryPercent(Integer fuelOrBatteryPercent) {
        this.fuelOrBatteryPercent = fuelOrBatteryPercent;
    }

    public String getConditionNotes() {
        return conditionNotes;
    }

    public void setConditionNotes(String conditionNotes) {
        this.conditionNotes = conditionNotes;
    }

    public String getEvidenceHash() {
        return evidenceHash;
    }

    public void setEvidenceHash(String evidenceHash) {
        this.evidenceHash = evidenceHash;
    }

    public String getInspectedBy() {
        return inspectedBy;
    }

    public void setInspectedBy(String inspectedBy) {
        this.inspectedBy = inspectedBy;
    }

    public LocalDateTime getInspectedAt() {
        return inspectedAt;
    }

    public void setInspectedAt(LocalDateTime inspectedAt) {
        this.inspectedAt = inspectedAt;
    }
}
