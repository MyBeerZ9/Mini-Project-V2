package com.example.carrental.dto;

import jakarta.validation.constraints.*;

public class CarRequest {

    @NotBlank(message = "Car name is required")
    @Size(max = 100, message = "Car name must be under 100 characters")
    private String carName;

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand must be under 50 characters")
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(max = 50, message = "Model must be under 50 characters")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1990, message = "Year must be 1990 or later")
    @Max(value = 2030, message = "Year cannot be in the distant future")
    private Integer year;

    @NotBlank(message = "Car type is required")
    private String carType; // Hatchback, Sedan, SUV, MUV, Luxury, Electric

    @NotBlank(message = "Fuel type is required")
    private String fuelType; // Petrol, Diesel, Electric, Hybrid, CNG

    @NotBlank(message = "Transmission is required")
    private String transmission; // Manual, Automatic

    @NotNull(message = "Seats is required")
    @Min(value = 2, message = "Seats must be at least 2")
    @Max(value = 15, message = "Seats cannot exceed 15")
    private Integer seats;

    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location must be under 100 characters")
    private String location;

    @NotNull(message = "Price per day is required")
    @Positive(message = "Price per day must be positive")
    private Double pricePerDay;

    @Size(max = 2000, message = "Description must be under 2000 characters")
    private String description;

    @Size(max = 1000, message = "Image URL must be under 1000 characters")
    private String imageUrl;

    @NotBlank(message = "Owner name is required")
    @Size(max = 100, message = "Owner name must be under 100 characters")
    private String ownerName;

    @NotBlank(message = "Owner email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Owner email must be under 100 characters")
    private String ownerEmail;

    public CarRequest() {
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
}
