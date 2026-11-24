package com.example.carrental.service;

import com.example.carrental.dto.BookingRequest;
import com.example.carrental.dto.BookingResponse;
import com.example.carrental.entity.Car;
import com.example.carrental.exception.BookingConflictException;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.CarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for BookingService covering validation, pricing,
 * overlap rejection, cancellation recovery, and concurrent booking safety.
 *
 * Uses dev profile (H2 in-memory with MODE=PostgreSQL) and Flyway migration.
 * Date ranges are carefully chosen to avoid interference between tests
 * and with seeded sample bookings (plusDays 2-4 and 10-13).
 */
@SpringBootTest
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CarRepository carRepository;

    // ── Helpers ──

    private Car getFirstCar() {
        List<Car> cars = carRepository.findAll();
        assertFalse(cars.isEmpty(), "Database must have at least one seeded car");
        return cars.get(0);
    }

    private BookingRequest buildRequest(Long carId, LocalDate start, LocalDate end) {
        BookingRequest req = new BookingRequest();
        req.setCarId(carId);
        req.setCustomerName("Test User");
        req.setCustomerEmail("test@example.com");
        req.setCustomerPhone("+91 9876543210");
        req.setStartDate(start);
        req.setEndDate(end);
        req.setNotes("Automated test booking");
        return req;
    }

    // ══════════════════════════════════════════════════════════════
    //  Validation Tests
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Reject booking when end date is before start date")
    void rejectEndDateBeforeStartDate() {
        Car car = getFirstCar();
        BookingRequest req = buildRequest(
                car.getId(),
                LocalDate.now().plusDays(40),
                LocalDate.now().plusDays(38) // end before start
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(req));
        assertTrue(ex.getMessage().toLowerCase().contains("before"),
                "Error message should mention 'before'");
    }

    @Test
    @DisplayName("Reject booking when start date is in the past")
    void rejectStartDateInThePast() {
        Car car = getFirstCar();
        BookingRequest req = buildRequest(
                car.getId(),
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(1)
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(req));
        assertTrue(ex.getMessage().toLowerCase().contains("past"),
                "Error message should mention 'past'");
    }

    @Test
    @DisplayName("Reject booking for non-existent car ID")
    void rejectBookingForNonexistentCar() {
        BookingRequest req = buildRequest(
                99999L,
                LocalDate.now().plusDays(40),
                LocalDate.now().plusDays(42)
        );

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(req));
    }

    // ══════════════════════════════════════════════════════════════
    //  Happy Path & Pricing Tests
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Create valid booking with correct CV- reference, days, and price")
    void createValidBooking() {
        Car car = getFirstCar();
        LocalDate start = LocalDate.now().plusDays(50);
        LocalDate end = LocalDate.now().plusDays(52); // 3 days inclusive

        BookingRequest req = buildRequest(car.getId(), start, end);
        req.setCustomerName("Riya Sharma");
        req.setCustomerEmail("riya@example.com");

        BookingResponse response = bookingService.createBooking(req);

        assertAll("Valid booking assertions",
                () -> assertNotNull(response.getId()),
                () -> assertNotNull(response.getBookingReference()),
                () -> assertTrue(response.getBookingReference().startsWith("CV-"),
                        "Reference must start with CV-"),
                () -> assertEquals(8, response.getBookingReference().substring(3).length(),
                        "Reference suffix must be 8 characters"),
                () -> assertEquals("CONFIRMED", response.getStatus()),
                () -> assertEquals(3, response.getTotalDays()),
                () -> assertEquals(car.getPricePerDay() * 3, response.getTotalPrice(), 0.01),
                () -> assertEquals("Riya Sharma", response.getCustomerName())
        );
    }

    @Test
    @DisplayName("Single-day booking (same start and end) calculates as 1 day")
    void singleDayBooking() {
        Car car = getFirstCar();
        LocalDate day = LocalDate.now().plusDays(60);

        BookingRequest req = buildRequest(car.getId(), day, day);

        BookingResponse response = bookingService.createBooking(req);

        assertEquals(1, response.getTotalDays(),
                "Same-day booking should count as 1 day");
        assertEquals(car.getPricePerDay(), response.getTotalPrice(), 0.01);
    }

    // ══════════════════════════════════════════════════════════════
    //  Overlap / Conflict Tests
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Reject overlapping booking for same car with BookingConflictException")
    void rejectOverlappingBooking() {
        Car car = getFirstCar();
        LocalDate start = LocalDate.now().plusDays(70);
        LocalDate end = LocalDate.now().plusDays(73);

        // First booking succeeds
        BookingRequest first = buildRequest(car.getId(), start, end);
        first.setCustomerName("First Booker");
        first.setCustomerEmail("first@example.com");
        BookingResponse firstResponse = bookingService.createBooking(first);
        assertNotNull(firstResponse.getId(), "First booking must succeed");

        // Second overlapping booking must fail with 409
        BookingRequest second = buildRequest(car.getId(),
                start.plusDays(1), end.plusDays(1));
        second.setCustomerName("Second Booker");
        second.setCustomerEmail("second@example.com");

        BookingConflictException ex = assertThrows(BookingConflictException.class,
                () -> bookingService.createBooking(second));
        assertTrue(ex.getMessage().contains("already reserved"),
                "Conflict message should mention 'already reserved'");
    }

    @Test
    @DisplayName("Allow booking after cancellation of conflicting reservation")
    void allowBookingAfterCancellation() {
        Car car = getFirstCar();
        LocalDate start = LocalDate.now().plusDays(80);
        LocalDate end = LocalDate.now().plusDays(82);

        // Create and then cancel
        BookingRequest req = buildRequest(car.getId(), start, end);
        req.setCustomerName("Cancel Test");
        req.setCustomerEmail("cancel@example.com");
        BookingResponse created = bookingService.createBooking(req);
        bookingService.cancelBooking(created.getId());

        // Same dates should now succeed for a different customer
        BookingRequest retry = buildRequest(car.getId(), start, end);
        retry.setCustomerName("Retry Booker");
        retry.setCustomerEmail("retry@example.com");

        BookingResponse retryResponse = bookingService.createBooking(retry);
        assertAll("Post-cancellation booking",
                () -> assertNotNull(retryResponse.getId()),
                () -> assertEquals("CONFIRMED", retryResponse.getStatus()),
                () -> assertNotEquals(created.getBookingReference(),
                        retryResponse.getBookingReference())
        );
    }

    // ══════════════════════════════════════════════════════════════
    //  Concurrency Safety Test
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Concurrent booking attempts — exactly one wins, others get 409")
    void concurrentBookingOnlyOneWins() throws InterruptedException {
        Car car = getFirstCar();
        LocalDate start = LocalDate.now().plusDays(90);
        LocalDate end = LocalDate.now().plusDays(92);

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch gate = new CountDownLatch(1); // all threads wait for this
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                try {
                    gate.await(); // synchronize start
                    BookingRequest req = buildRequest(car.getId(), start, end);
                    req.setCustomerName("Racer-" + idx);
                    req.setCustomerEmail("racer" + idx + "@example.com");
                    bookingService.createBooking(req);
                    successCount.incrementAndGet();
                } catch (BookingConflictException e) {
                    conflictCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        gate.countDown(); // release all threads simultaneously
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS),
                "All threads should complete within 30 seconds");

        assertEquals(1, successCount.get(),
                "Exactly one concurrent booking must succeed");
        assertEquals(threadCount - 1, conflictCount.get(),
                "All other concurrent attempts must receive BookingConflictException");
    }
}
