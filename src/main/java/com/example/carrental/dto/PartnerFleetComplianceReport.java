package com.example.carrental.dto;

import java.time.LocalDate;

public class PartnerFleetComplianceReport {

    private Long carId;
    private String carName;
    private String partnerName;
    private boolean compliant;
    private LocalDate rcExpiryDate;
    private LocalDate insuranceExpiryDate;
    private LocalDate pucExpiryDate;
    private LocalDate permitExpiryDate;
    private String complianceIssue;

    public PartnerFleetComplianceReport() {
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public boolean isCompliant() {
        return compliant;
    }

    public void setCompliant(boolean compliant) {
        this.compliant = compliant;
    }

    public LocalDate getRcExpiryDate() {
        return rcExpiryDate;
    }

    public void setRcExpiryDate(LocalDate rcExpiryDate) {
        this.rcExpiryDate = rcExpiryDate;
    }

    public LocalDate getInsuranceExpiryDate() {
        return insuranceExpiryDate;
    }

    public void setInsuranceExpiryDate(LocalDate insuranceExpiryDate) {
        this.insuranceExpiryDate = insuranceExpiryDate;
    }

    public LocalDate getPucExpiryDate() {
        return pucExpiryDate;
    }

    public void setPucExpiryDate(LocalDate pucExpiryDate) {
        this.pucExpiryDate = pucExpiryDate;
    }

    public LocalDate getPermitExpiryDate() {
        return permitExpiryDate;
    }

    public void setPermitExpiryDate(LocalDate permitExpiryDate) {
        this.permitExpiryDate = permitExpiryDate;
    }

    public String getComplianceIssue() {
        return complianceIssue;
    }

    public void setComplianceIssue(String complianceIssue) {
        this.complianceIssue = complianceIssue;
    }
}
