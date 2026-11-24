package com.example.carrental.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", nullable = false, unique = true)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "status", nullable = false)
    private String status = "CONFIRMED"; // PENDING_KYC, CONFIRMED, ACTIVE_TRIP, OVERDUE, ESCALATED_THEFT_HOLD, COMPLETED, CANCELLED

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Step 2A: Custody, KYC, Deposit & Escalation Fields
    @Column(name = "kyc_status")
    private String kycStatus = "VERIFIED"; // PENDING_KYC, VERIFIED, REJECTED

    @Column(name = "renter_dl_number")
    private String renterDlNumber;

    @Column(name = "renter_risk_score")
    private Integer renterRiskScore = 15; // 0-100 (lower is safer)

    @Column(name = "security_deposit")
    private Double securityDeposit = 3000.0;

    @Column(name = "deposit_status")
    private String depositStatus = "HELD"; // HELD, RELEASED, FORFEITED_DISPUTE

    @Column(name = "overdue_fee")
    private Double overdueFee = 0.0;

    @Column(name = "actual_return_at")
    private LocalDateTime actualReturnAt;

    @Column(name = "escalation_reason", length = 500)
    private String escalationReason;

    public Booking() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null || this.status.trim().isEmpty()) {
            this.status = "CONFIRMED";
        }
        if (this.kycStatus == null) {
            this.kycStatus = "VERIFIED";
        }
        if (this.renterRiskScore == null) {
            this.renterRiskScore = 15;
        }
        if (this.securityDeposit == null) {
            this.securityDeposit = 3000.0;
        }
        if (this.depositStatus == null) {
            this.depositStatus = "HELD";
        }
        if (this.overdueFee == null) {
            this.overdueFee = 0.0;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public String getRenterDlNumber() {
        return renterDlNumber;
    }

    public void setRenterDlNumber(String renterDlNumber) {
        this.renterDlNumber = renterDlNumber;
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

    public LocalDateTime getActualReturnAt() {
        return actualReturnAt;
    }

    public void setActualReturnAt(LocalDateTime actualReturnAt) {
        this.actualReturnAt = actualReturnAt;
    }

    public String getEscalationReason() {
        return escalationReason;
    }

    public void setEscalationReason(String escalationReason) {
        this.escalationReason = escalationReason;
    }
}
