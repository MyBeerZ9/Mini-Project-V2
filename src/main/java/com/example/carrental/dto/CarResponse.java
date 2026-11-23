package com.example.carrental.dto;

import com.example.carrental.entity.Car;
import java.time.LocalDateTime;

public class CarResponse {

    private Long id;
    private String carName;
    private String brand;
    private String model;
    private Integer year;
    private String carType;
    private String fuelType;
    private String transmission;
    private Integer seats;
    private String location;
    private Double pricePerDay;
    private String description;
    private String imageUrl;
    private String ownerName;
    private String ownerEmail;
    private Boolean available;
    private LocalDateTime createdAt;

    public CarResponse() {
    }

    public static CarResponse fromEntity(Car car) {
        if (car == null) return null;
        CarResponse response = new CarResponse();
        response.setId(car.getId());
        response.setCarName(car.getCarName());
        response.setBrand(car.getBrand());
        response.setModel(car.getModel());
        response.setYear(car.getYear());
        response.setCarType(car.getCarType());
        response.setFuelType(car.getFuelType());
        response.setTransmission(car.getTransmission());
        response.setSeats(car.getSeats());
        response.setLocation(car.getLocation());
        response.setPricePerDay(car.getPricePerDay());
        response.setDescription(car.getDescription());
        response.setImageUrl(car.getImageUrl());
        response.setOwnerName(car.getOwnerName());
        response.setOwnerEmail(car.getOwnerEmail());
        response.setAvailable(car.getAvailable());
        response.setCreatedAt(car.getCreatedAt());
        return response;
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
}
