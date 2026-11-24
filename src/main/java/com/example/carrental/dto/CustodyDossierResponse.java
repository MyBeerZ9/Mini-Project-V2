package com.example.carrental.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CustodyDossierResponse {

    private Long bookingId;
    private String bookingReference;
    private String bookingStatus;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String renterDlNumber;
    private String kycStatus;
    private Integer renterRiskScore;
    private Double securityDeposit;
    private String depositStatus;
    private Double overdueFee;
    private String escalationReason;
    private LocalDate startDate;
    private LocalDate scheduledEndDate;
    private LocalDateTime actualReturnAt;
    private List<VehicleInspectionResponse> inspections;
    private List<CustodyAlertResponse> alerts;

    public CustodyDossierResponse() {
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getRenterDlNumber() {
        return renterDlNumber;
    }

    public void setRenterDlNumber(String renterDlNumber) {
        this.renterDlNumber = renterDlNumber;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public Integer getRenterRiskScore() {
        return renterRiskScore;
    }

    public void setRenterRiskScore(Integer renterRiskScore) {
        this.renterRiskScore = renterRiskScore;
    }

    public Double getSecurityDeposit() {
        return securityDeposit;
    }

    public void setSecurityDeposit(Double securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    public String getDepositStatus() {
        return depositStatus;
    }

    public void setDepositStatus(String depositStatus) {
        this.depositStatus = depositStatus;
    }

    public Double getOverdueFee() {
        return overdueFee;
    }

    public void setOverdueFee(Double overdueFee) {
        this.overdueFee = overdueFee;
    }

    public String getEscalationReason() {
        return escalationReason;
    }

    public void setEscalationReason(String escalationReason) {
        this.escalationReason = escalationReason;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getScheduledEndDate() {
        return scheduledEndDate;
    }

    public void setScheduledEndDate(LocalDate scheduledEndDate) {
        this.scheduledEndDate = scheduledEndDate;
    }

    public LocalDateTime getActualReturnAt() {
        return actualReturnAt;
    }

    public void setActualReturnAt(LocalDateTime actualReturnAt) {
        this.actualReturnAt = actualReturnAt;
    }

    public List<VehicleInspectionResponse> getInspections() {
        return inspections;
    }

    public void setInspections(List<VehicleInspectionResponse> inspections) {
        this.inspections = inspections;
    }

    public List<CustodyAlertResponse> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<CustodyAlertResponse> alerts) {
        this.alerts = alerts;
    }
}
