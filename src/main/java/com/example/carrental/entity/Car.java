package com.example.carrental.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "car_name", nullable = false)
    private String carName;

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "model_year", nullable = false)
    private Integer year;

    @Column(name = "car_type", nullable = false)
    private String carType; // Hatchback, Sedan, SUV, MUV, Luxury, Electric

    @Column(name = "fuel_type", nullable = false)
    private String fuelType; // Petrol, Diesel, Electric, Hybrid, CNG

    @Column(name = "transmission", nullable = false)
    private String transmission; // Manual, Automatic

    @Column(name = "seats", nullable = false)
    private Integer seats;

    @Column(name = "location", nullable = false)
    private String location; // Delhi, Noida, Gurgaon, etc.

    @Column(name = "price_per_day", nullable = false)
    private Double pricePerDay;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    @Column(name = "available", nullable = false)
    private Boolean available = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Step 2B: Partner & Compliance Fields
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @Column(name = "rc_expiry_date")
    private LocalDate rcExpiryDate;

    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;

    @Column(name = "puc_expiry_date")
    private LocalDate pucExpiryDate;

    @Column(name = "permit_expiry_date")
    private LocalDate permitExpiryDate;

    // Step 2C: Pricing Floor / Ceiling Bands
    @Column(name = "min_rate_floor")
    private Double minRateFloor;

    @Column(name = "max_rate_ceiling")
    private Double maxRateCeiling;

    public Car() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.available == null) {
            this.available = true;
        }
        if (this.minRateFloor == null && this.pricePerDay != null) {
            this.minRateFloor = Math.round(this.pricePerDay * 0.75 * 100.0) / 100.0;
        }
        if (this.maxRateCeiling == null && this.pricePerDay != null) {
            this.maxRateCeiling = Math.round(this.pricePerDay * 1.60 * 100.0) / 100.0;
        }
    }

    public boolean isComplianceValid(LocalDate referenceDate) {
        LocalDate checkDate = (referenceDate != null) ? referenceDate : LocalDate.now();
        if (rcExpiryDate != null && rcExpiryDate.isBefore(checkDate)) return false;
        if (insuranceExpiryDate != null && insuranceExpiryDate.isBefore(checkDate)) return false;
        if (pucExpiryDate != null && pucExpiryDate.isBefore(checkDate)) return false;
        if (permitExpiryDate != null && permitExpiryDate.isBefore(checkDate)) return false;
        return true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(Double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
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

    public Double getMinRateFloor() {
        return minRateFloor;
    }

    public void setMinRateFloor(Double minRateFloor) {
        this.minRateFloor = minRateFloor;
    }

    public Double getMaxRateCeiling() {
        return maxRateCeiling;
    }

    public void setMaxRateCeiling(Double maxRateCeiling) {
        this.maxRateCeiling = maxRateCeiling;
    }
}
