package com.example.carrental.service;

import com.example.carrental.dto.CarRequest;
import com.example.carrental.dto.CarResponse;
import com.example.carrental.entity.Car;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.CarRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public CarResponse createCar(CarRequest request) {
        Car car = new Car();
        copyProperties(request, car);
        car.setAvailable(true);
        Car savedCar = carRepository.save(car);
        return CarResponse.fromEntity(savedCar);
    }

    @Transactional(readOnly = true)
    public List<CarResponse> getAllCars() {
        return carRepository.findByAvailableTrue()
                .stream()
                .map(CarResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CarResponse> getFeaturedCars() {
        return carRepository.findTop6ByAvailableTrueOrderByCreatedAtDesc()
                .stream()
                .map(CarResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CarResponse getCarById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));
        return CarResponse.fromEntity(car);
    }

    public CarResponse updateCar(Long id, CarRequest request) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));
        copyProperties(request, car);
        Car updatedCar = carRepository.save(car);
        return CarResponse.fromEntity(updatedCar);
    }

    public void deleteCar(Long id) {
        if (!carRepository.existsById(id)) {
            throw new ResourceNotFoundException("Car not found with id: " + id);
        }
        carRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CarResponse> searchCars(String location,
                                        String carType,
                                        String fuelType,
                                        String transmission,
                                        Double maxPrice,
                                        Integer minSeats,
                                        String preferredBrand) {

        Specification<Car> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only available cars
            predicates.add(cb.isTrue(root.get("available")));

            if (location != null && !location.trim().isEmpty()) {
                String locLower = "%" + location.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("location")), locLower));
            }

            if (carType != null && !carType.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("carType")), carType.trim().toLowerCase()));
            }

            if (fuelType != null && !fuelType.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("fuelType")), fuelType.trim().toLowerCase()));
            }

            if (transmission != null && !transmission.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("transmission")), transmission.trim().toLowerCase()));
            }

            if (maxPrice != null && maxPrice > 0) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerDay"), maxPrice));
            }

            if (minSeats != null && minSeats > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("seats"), minSeats));
            }

            if (preferredBrand != null && !preferredBrand.trim().isEmpty()) {
                String brandPattern = "%" + preferredBrand.trim().toLowerCase() + "%";
                Predicate brandPredicate = cb.like(cb.lower(root.get("brand")), brandPattern);
                Predicate namePredicate = cb.like(cb.lower(root.get("carName")), brandPattern);
                predicates.add(cb.or(brandPredicate, namePredicate));
            }

            query.orderBy(cb.asc(root.get("pricePerDay")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return carRepository.findAll(spec)
                .stream()
                .map(CarResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void copyProperties(CarRequest request, Car car) {
        car.setCarName(request.getCarName().trim());
        car.setBrand(request.getBrand().trim());
        car.setModel(request.getModel().trim());
        car.setYear(request.getYear());
        car.setCarType(request.getCarType().trim());
        car.setFuelType(request.getFuelType().trim());
        car.setTransmission(request.getTransmission().trim());
        car.setSeats(request.getSeats());
        car.setLocation(request.getLocation().trim());
        car.setPricePerDay(request.getPricePerDay());
        car.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        car.setImageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null);
        car.setOwnerName(request.getOwnerName().trim());
        car.setOwnerEmail(request.getOwnerEmail().trim());
    }
}
