package com.example.carrental.service;

import com.example.carrental.dto.BookingRequest;
import com.example.carrental.dto.BookingResponse;
import com.example.carrental.entity.Booking;
import com.example.carrental.entity.Car;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.CarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;

    public BookingService(BookingRepository bookingRepository, CarRepository carRepository) {
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
    }

    public BookingResponse createBooking(BookingRequest request) {
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + request.getCarId()));

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }

        // Conflict check: Ensure no overlapping active bookings exist for this car
        List<Booking> conflicts = bookingRepository.findOverlappingBookings(
                request.getCarId(),
                request.getStartDate(),
                request.getEndDate()
        );
        if (!conflicts.isEmpty()) {
            Booking firstConflict = conflicts.get(0);
            throw new IllegalArgumentException(String.format(
                    "This vehicle is already reserved from %s to %s. Please select different dates.",
                    firstConflict.getStartDate(),
                    firstConflict.getEndDate()
            ));
        }

        // Calculate total days (e.g. Sep 10 to Sep 10 = 1 day, Sep 10 to Sep 12 = 3 days)
        long daysDiff = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        int totalDays = Math.max(1, (int) daysDiff + 1);
        double totalPrice = totalDays * car.getPricePerDay();

        String reference = "CV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Booking booking = new Booking();
        booking.setBookingReference(reference);
        booking.setCar(car);
        booking.setCustomerName(request.getCustomerName().trim());
        booking.setCustomerEmail(request.getCustomerEmail().trim());
        booking.setCustomerPhone(request.getCustomerPhone().trim());
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setTotalDays(totalDays);
        booking.setTotalPrice(totalPrice);
        booking.setStatus("CONFIRMED");
        booking.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        Booking saved = bookingRepository.save(booking);
        return BookingResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<com.example.carrental.dto.BookedDateRange> getCarBookedRanges(Long carId) {
        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car not found with id: " + carId);
        }
        return bookingRepository.findUpcomingActiveBookings(carId, LocalDate.now())
                .stream()
                .map(b -> new com.example.carrental.dto.BookedDateRange(
                        b.getStartDate(),
                        b.getEndDate(),
                        b.getStatus(),
                        b.getBookingReference()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public com.example.carrental.dto.AvailabilityCheckResponse checkCarAvailability(
            Long carId,
            LocalDate startDate,
            LocalDate endDate) {

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + carId));

        com.example.carrental.dto.AvailabilityCheckResponse response = new com.example.carrental.dto.AvailabilityCheckResponse();
        response.setCarId(carId);
        response.setRequestedStartDate(startDate);
        response.setRequestedEndDate(endDate);

        if (startDate == null || endDate == null) {
            response.setAvailable(false);
            response.setMessage("Both pickup and return dates are required.");
            return response;
        }

        if (endDate.isBefore(startDate)) {
            response.setAvailable(false);
            response.setMessage("Return date cannot be earlier than pickup date.");
            return response;
        }

        if (startDate.isBefore(LocalDate.now())) {
            response.setAvailable(false);
            response.setMessage("Pickup date cannot be in the past.");
            return response;
        }

        long daysDiff = ChronoUnit.DAYS.between(startDate, endDate);
        int totalDays = Math.max(1, (int) daysDiff + 1);
        double estimatedPrice = totalDays * car.getPricePerDay();
        response.setTotalDays(totalDays);
        response.setEstimatedPrice(estimatedPrice);

        List<Booking> conflicts = bookingRepository.findOverlappingBookings(carId, startDate, endDate);
        if (!conflicts.isEmpty()) {
            response.setAvailable(false);
            Booking first = conflicts.get(0);
            response.setMessage(String.format("Unavailable: Car is already reserved from %s to %s.",
                    first.getStartDate(), first.getEndDate()));
            response.setConflictingBookings(conflicts.stream()
                    .map(b -> new com.example.carrental.dto.BookedDateRange(
                            b.getStartDate(),
                            b.getEndDate(),
                            b.getStatus(),
                            b.getBookingReference()
                    ))
                    .collect(Collectors.toList()));
        } else {
            response.setAvailable(true);
            response.setMessage(String.format("Available! %d %s • ₹%.2f total estimated",
                    totalDays, totalDays == 1 ? "day" : "days", estimatedPrice));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return BookingResponse.fromEntity(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String reference) {
        Booking booking = bookingRepository.findByBookingReference(reference.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + reference));
        return BookingResponse.fromEntity(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByEmail(String email) {
        return bookingRepository.findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(email.trim())
                .stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public BookingResponse cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        booking.setStatus("CANCELLED");
        Booking updated = bookingRepository.save(booking);
        return BookingResponse.fromEntity(updated);
    }
}
