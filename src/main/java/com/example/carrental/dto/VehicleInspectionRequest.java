package com.example.carrental.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VehicleInspectionRequest {

    @NotBlank(message = "Inspection type is required (PRE_TRIP or POST_TRIP)")
    private String inspectionType;

    @NotNull(message = "Odometer reading in km is required")
    @Min(value = 0, message = "Odometer reading must be non-negative")
    private Integer odometerKm;

    @NotNull(message = "Fuel or battery percent is required")
    @Min(value = 0, message = "Percent cannot be less than 0")
    @Max(value = 100, message = "Percent cannot exceed 100")
    private Integer fuelOrBatteryPercent;

    private String conditionNotes;

    @NotBlank(message = "Evidence hash is required")
    private String evidenceHash;

    @NotBlank(message = "Inspected by is required")
    private String inspectedBy;

    public VehicleInspectionRequest() {
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
}
