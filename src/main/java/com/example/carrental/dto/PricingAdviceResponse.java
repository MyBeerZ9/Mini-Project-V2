package com.example.carrental.dto;

import java.time.LocalDate;

public class PricingAdviceResponse {

    private Long carId;
    private String carName;
    private String location;
    private Double basePrice;
    private Double recommendedDailyRate;
    private Double minRateBand;
    private Double maxRateBand;
    private Integer durationDays;
    private Double estimatedTotalRevenue;
    private Double estimatedPartnerYield;
    private Double revenueSharePercent;
    private String demandLevel; // HIGH, MEDIUM, NORMAL
    private String rationale;
    private String engineSource; // GEMINI_AI, DETERMINISTIC_RULE_FALLBACK

    public PricingAdviceResponse() {
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Double getRecommendedDailyRate() {
        return recommendedDailyRate;
    }

    public void setRecommendedDailyRate(Double recommendedDailyRate) {
        this.recommendedDailyRate = recommendedDailyRate;
    }

    public Double getMinRateBand() {
        return minRateBand;
    }

    public void setMinRateBand(Double minRateBand) {
        this.minRateBand = minRateBand;
    }

    public Double getMaxRateBand() {
        return maxRateBand;
    }

    public void setMaxRateBand(Double maxRateBand) {
        this.maxRateBand = maxRateBand;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Double getEstimatedTotalRevenue() {
        return estimatedTotalRevenue;
    }

    public void setEstimatedTotalRevenue(Double estimatedTotalRevenue) {
        this.estimatedTotalRevenue = estimatedTotalRevenue;
    }

    public Double getEstimatedPartnerYield() {
        return estimatedPartnerYield;
    }

    public void setEstimatedPartnerYield(Double estimatedPartnerYield) {
        this.estimatedPartnerYield = estimatedPartnerYield;
    }

    public Double getRevenueSharePercent() {
        return revenueSharePercent;
    }

    public void setRevenueSharePercent(Double revenueSharePercent) {
        this.revenueSharePercent = revenueSharePercent;
    }

    public String getDemandLevel() {
        return demandLevel;
    }

    public void setDemandLevel(String demandLevel) {
        this.demandLevel = demandLevel;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public String getEngineSource() {
        return engineSource;
    }

    public void setEngineSource(String engineSource) {
        this.engineSource = engineSource;
    }
}
