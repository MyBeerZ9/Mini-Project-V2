package com.example.carrental;

import com.example.carrental.dto.AiSearchCriteria;
import com.example.carrental.dto.BookingRequest;
import com.example.carrental.dto.BookingResponse;
import com.example.carrental.entity.Car;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.service.AiSearchService;
import com.example.carrental.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CarRentalApplicationTests {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AiSearchService aiSearchService;

    @Test
    @DisplayName("Application context loads and seeds realistic cars successfully")
    void contextLoadsAndCarsSeeded() {
        long count = carRepository.count();
        assertTrue(count >= 14, "Database should be seeded with at least 14 realistic sample cars");
    }

    @Test
    @DisplayName("Local NLP parser accurately extracts search criteria from natural query")
    void testLocalNlpParser() {
        String query = "I need an automatic SUV in Delhi under 3000 rupees per day";
        AiSearchCriteria criteria = aiSearchService.parseLocallyWithNlp(query);

        assertEquals("Delhi", criteria.getLocation());
        assertEquals("SUV", criteria.getCarType());
        assertEquals("Automatic", criteria.getTransmission());
        assertNotNull(criteria.getMaxPricePerDay());
        assertEquals(3000.0, criteria.getMaxPricePerDay());
    }

    @Test
    @DisplayName("End-to-end booking flow creates reservation with dynamic total calculation")
    void testBookingCreation() {
        List<Car> cars = carRepository.findAll();
        assertFalse(cars.isEmpty());
        Car car = cars.get(0);

        BookingRequest req = new BookingRequest();
        req.setCarId(car.getId());
        req.setCustomerName("Aarav Sharma");
        req.setCustomerEmail("aarav.sharma@example.com");
        req.setCustomerPhone("+91 9876543210");
        req.setStartDate(LocalDate.now().plusDays(20));
        req.setEndDate(LocalDate.now().plusDays(22)); // 3 days
        req.setNotes("Arriving at airport terminal 3");

        BookingResponse response = bookingService.createBooking(req);

        assertNotNull(response.getId());
        assertNotNull(response.getBookingReference());
        assertTrue(response.getBookingReference().startsWith("CV-"));
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(3, response.getTotalDays());
        assertEquals(car.getPricePerDay() * 3, response.getTotalPrice());

        // Verify retrieval by email
        List<BookingResponse> byEmail = bookingService.getBookingsByEmail("aarav.sharma@example.com");
        assertFalse(byEmail.isEmpty());
        assertEquals(response.getBookingReference(), byEmail.get(0).getBookingReference());
    }

    @Test
    @DisplayName("Conflict prevention strictly blocks double booking for overlapping date ranges")
    void testBookingConflictPrevention() {
        List<Car> cars = carRepository.findAll();
        assertFalse(cars.isEmpty());
        Car car = cars.get(0);

        // Attempt to book overlapping dates with the pre-seeded booking (which is plusDays(2) to plusDays(4))
        BookingRequest conflictReq = new BookingRequest();
        conflictReq.setCarId(car.getId());
        conflictReq.setCustomerName("Conflict Tester");
        conflictReq.setCustomerEmail("tester@example.com");
        conflictReq.setCustomerPhone("+91 9999988888");
        conflictReq.setStartDate(LocalDate.now().plusDays(3));
        conflictReq.setEndDate(LocalDate.now().plusDays(5));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(conflictReq);
        });

        assertTrue(ex.getMessage().contains("already reserved"));

        // Verify availability check API returns false
        var avail = bookingService.checkCarAvailability(car.getId(), LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        assertFalse(avail.isAvailable());
        assertFalse(avail.getConflictingBookings().isEmpty());
    }
}
