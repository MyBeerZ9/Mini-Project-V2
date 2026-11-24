package com.example.carrental.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PartnerRequest {

    @NotBlank(message = "Partner name is required")
    private String partnerName;

    @NotBlank(message = "Partner code is required")
    private String partnerCode;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Valid email is required")
    private String contactEmail;

    @NotBlank(message = "Contact phone is required")
    private String contactPhone;

    @NotBlank(message = "City is required")
    private String city;

    private Double revenueSharePercent = 80.0;

    public PartnerRequest() {
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getRevenueSharePercent() {
        return revenueSharePercent;
    }

    public void setRevenueSharePercent(Double revenueSharePercent) {
        this.revenueSharePercent = revenueSharePercent;
    }
}
