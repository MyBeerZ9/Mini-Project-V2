package com.example.carrental.dto;

import java.time.LocalDate;

public class BookedDateRange {

    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String bookingReference;

    public BookedDateRange() {
    }

    public BookedDateRange(LocalDate startDate, LocalDate endDate, String status, String bookingReference) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.bookingReference = bookingReference;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }
}
