package com.example.carrental.dto;

import com.example.carrental.entity.VehicleInspection;
import java.time.LocalDateTime;

public class VehicleInspectionResponse {

    private Long id;
    private Long bookingId;
    private String inspectionType;
    private Integer odometerKm;
    private Integer fuelOrBatteryPercent;
    private String conditionNotes;
    private String evidenceHash;
    private String inspectedBy;
    private LocalDateTime inspectedAt;

    public VehicleInspectionResponse() {
    }

    public static VehicleInspectionResponse fromEntity(VehicleInspection insp) {
        if (insp == null) return null;
        VehicleInspectionResponse resp = new VehicleInspectionResponse();
        resp.setId(insp.getId());
        if (insp.getBooking() != null) {
            resp.setBookingId(insp.getBooking().getId());
        }
        resp.setInspectionType(insp.getInspectionType());
        resp.setOdometerKm(insp.getOdometerKm());
        resp.setFuelOrBatteryPercent(insp.getFuelOrBatteryPercent());
        resp.setConditionNotes(insp.getConditionNotes());
        resp.setEvidenceHash(insp.getEvidenceHash());
        resp.setInspectedBy(insp.getInspectedBy());
        resp.setInspectedAt(insp.getInspectedAt());
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
