package com.example.carrental.dto;

import com.example.carrental.entity.Partner;
import java.time.LocalDateTime;

public class PartnerResponse {

    private Long id;
    private String partnerName;
    private String partnerCode;
    private String contactEmail;
    private String contactPhone;
    private String city;
    private Double revenueSharePercent;
    private Boolean verified;
    private Boolean active;
    private LocalDateTime createdAt;

    public PartnerResponse() {
    }

    public static PartnerResponse fromEntity(Partner partner) {
        if (partner == null) return null;
        PartnerResponse resp = new PartnerResponse();
        resp.setId(partner.getId());
        resp.setPartnerName(partner.getPartnerName());
        resp.setPartnerCode(partner.getPartnerCode());
        resp.setContactEmail(partner.getContactEmail());
        resp.setContactPhone(partner.getContactPhone());
        resp.setCity(partner.getCity());
        resp.setRevenueSharePercent(partner.getRevenueSharePercent());
        resp.setVerified(partner.getVerified());
        resp.setActive(partner.getActive());
        resp.setCreatedAt(partner.getCreatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
