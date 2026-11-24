package com.example.carrental.service;

import com.example.carrental.dto.*;
import com.example.carrental.entity.Booking;
import com.example.carrental.entity.Car;
import com.example.carrental.entity.Partner;
import com.example.carrental.exception.ComplianceException;
import com.example.carrental.exception.InvalidStateTransitionException;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.repository.PartnerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Step2MegaTestSuite {

    @Autowired
    private PartnerFleetService partnerFleetService;

    @Autowired
    private CustodyEscalationService custodyEscalationService;

    @Autowired
    private PricingAdvisorService pricingAdvisorService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Test
    @DisplayName("2B: Partner registration & document compliance blocks booking on expired RC/Insurance")
    void testPartnerComplianceBlocksExpiredCar() {
        Car car = carRepository.findAll().get(0);

        // Intentionally expire RC date
        partnerFleetService.updateComplianceDates(
                car.getId(),
                LocalDate.now().minusDays(10), // expired RC!
                LocalDate.now().plusMonths(6),
                LocalDate.now().plusMonths(6),
                LocalDate.now().plusMonths(6)
        );

        BookingRequest req = new BookingRequest();
        req.setCarId(car.getId());
        req.setCustomerName("Compliance Tester");
        req.setCustomerEmail("compliance@example.com");
        req.setCustomerPhone("+91 9998887776");
        req.setStartDate(LocalDate.now().plusDays(100));
        req.setEndDate(LocalDate.now().plusDays(102));

        ComplianceException ex = assertThrows(ComplianceException.class, () -> bookingService.createBooking(req));
        assertTrue(ex.getMessage().contains("RC expired"));

        // Restore compliance dates
        partnerFleetService.updateComplianceDates(
                car.getId(),
                LocalDate.now().plusYears(1),
                LocalDate.now().plusYears(1),
                LocalDate.now().plusYears(1),
                LocalDate.now().plusYears(1)
        );
    }

    @Test
    @DisplayName("2A: Custody Lifecycle — State Machine Transitions & Pre/Post Trip Inspections")
    void testCustodyLifecycleAndInspections() {
        Car car = carRepository.findAll().get(1);

        BookingRequest req = new BookingRequest();
        req.setCarId(car.getId());
        req.setCustomerName("Trip Driver");
        req.setCustomerEmail("driver@example.com");
        req.setCustomerPhone("+91 9876543210");
        req.setStartDate(LocalDate.now().plusDays(110));
        req.setEndDate(LocalDate.now().plusDays(112));

        BookingResponse created = bookingService.createBooking(req);
        assertEquals("CONFIRMED", created.getStatus());

        // Illegal transition: CONFIRMED cannot jump directly to COMPLETED
        assertThrows(InvalidStateTransitionException.class, () ->
                custodyEscalationService.transitionStatus(created.getId(), "COMPLETED", "Illegal jump")
        );

        // Pre-trip inspection auto-activates trip (CONFIRMED -> ACTIVE_TRIP)
        VehicleInspectionRequest preTrip = new VehicleInspectionRequest();
        preTrip.setInspectionType("PRE_TRIP");
        preTrip.setOdometerKm(15200);
        preTrip.setFuelOrBatteryPercent(95);
        preTrip.setConditionNotes("Scratch on left bumper recorded");
        preTrip.setEvidenceHash("SHA256:abcd1234efgh5678");
        preTrip.setInspectedBy("Inspector Ravi");

        VehicleInspectionResponse inspResp = custodyEscalationService.recordInspection(created.getId(), preTrip);
        assertNotNull(inspResp.getId());
        assertEquals("PRE_TRIP", inspResp.getInspectionType());

        Booking currentBooking = bookingRepository.findById(created.getId()).orElseThrow();
        assertEquals("ACTIVE_TRIP", currentBooking.getStatus());

        // Complete trip
        BookingResponse completed = custodyEscalationService.transitionStatus(created.getId(), "COMPLETED", "Trip concluded safely");
        assertEquals("COMPLETED", completed.getStatus());
        assertEquals("RELEASED", bookingRepository.findById(created.getId()).orElseThrow().getDepositStatus());
    }

    @Test
    @DisplayName("2A: Theft alert triggers immediate ESCALATED_THEFT_HOLD and freezes deposit")
    void testTheftSuspectedEscalation() {
        Car car = carRepository.findAll().get(2);

        BookingRequest req = new BookingRequest();
        req.setCarId(car.getId());
        req.setCustomerName("Suspicious User");
        req.setCustomerEmail("suspect@example.com");
        req.setCustomerPhone("+91 9123456789");
        req.setStartDate(LocalDate.now().plusDays(120));
        req.setEndDate(LocalDate.now().plusDays(122));

        BookingResponse created = bookingService.createBooking(req);
        custodyEscalationService.transitionStatus(created.getId(), "ACTIVE_TRIP", "Trip started");

        CustodyAlertRequest alert = new CustodyAlertRequest();
        alert.setAlertType("THEFT_SUSPECTED");
        alert.setSeverity("CRITICAL");
        alert.setLocationSnapshot("28.6139° N, 77.2090° E (State Border Exit)");
        alert.setDetails("Vehicle tracker tampered and geofence breached");

        CustodyAlertResponse alertResp = custodyEscalationService.raiseCustodyAlert(created.getId(), alert);
        assertNotNull(alertResp.getId());

        Booking escalated = bookingRepository.findById(created.getId()).orElseThrow();
        assertEquals("ESCALATED_THEFT_HOLD", escalated.getStatus());
        assertEquals("FORFEITED_DISPUTE", escalated.getDepositStatus());

        // Generate Custody Dossier
        CustodyDossierResponse dossier = custodyEscalationService.getCustodyDossier(created.getId());
        assertEquals("ESCALATED_THEFT_HOLD", dossier.getBookingStatus());
        assertFalse(dossier.getAlerts().isEmpty());
    }

    @Test
    @DisplayName("2C: AI Dynamic Rate & Yield Advisor — Deterministic Fallback & Cache")
    void testPricingAdvisorFallbackAndCache() {
        Car car = carRepository.findAll().get(0);
        LocalDate start = LocalDate.now().plusDays(15);
        LocalDate end = LocalDate.now().plusDays(18); // 4 days

        PricingAdviceResponse advice1 = pricingAdvisorService.getPricingAdvice(car.getId(), start, end);
        assertNotNull(advice1);
        assertTrue(advice1.getRecommendedDailyRate() >= advice1.getMinRateBand());
        assertTrue(advice1.getRecommendedDailyRate() <= advice1.getMaxRateBand());
        assertEquals(4, advice1.getDurationDays());
        assertTrue(advice1.getEstimatedPartnerYield() > 0);

        // Cache verification
        PricingAdviceResponse advice2 = pricingAdvisorService.getPricingAdvice(car.getId(), start, end);
        assertSame(advice1, advice2, "Subsequent identical request must be returned directly from in-memory cache");
    }
}
