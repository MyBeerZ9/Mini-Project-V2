package com.example.carrental.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityCheckResponse {

    private boolean available;
    private String message;
    private Long carId;
    private LocalDate requestedStartDate;
    private LocalDate requestedEndDate;
    private Integer totalDays;
    private Double estimatedPrice;
    private List<BookedDateRange> conflictingBookings = new ArrayList<>();

    public AvailabilityCheckResponse() {
    }

    public AvailabilityCheckResponse(boolean available, String message) {
        this.available = available;
        this.message = message;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public LocalDate getRequestedStartDate() {
        return requestedStartDate;
    }

    public void setRequestedStartDate(LocalDate requestedStartDate) {
        this.requestedStartDate = requestedStartDate;
    }

    public LocalDate getRequestedEndDate() {
        return requestedEndDate;
    }

    public void setRequestedEndDate(LocalDate requestedEndDate) {
        this.requestedEndDate = requestedEndDate;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public Double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(Double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public List<BookedDateRange> getConflictingBookings() {
        return conflictingBookings;
    }

    public void setConflictingBookings(List<BookedDateRange> conflictingBookings) {
        this.conflictingBookings = conflictingBookings;
    }
}
