package com.example.carrental.controller;

import com.example.carrental.dto.BookingRequest;
import com.example.carrental.dto.BookingResponse;
import com.example.carrental.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final com.example.carrental.service.CustodyEscalationService custodyEscalationService;

    public BookingController(BookingService bookingService,
                             com.example.carrental.service.CustodyEscalationService custodyEscalationService) {
        this.bookingService = bookingService;
        this.custodyEscalationService = custodyEscalationService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse created = bookingService.createBooking(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<BookingResponse> getBookingByReference(@PathVariable String reference) {
        return ResponseEntity.ok(bookingService.getBookingByReference(reference));
    }

    @GetMapping("/user")
    public ResponseEntity<List<BookingResponse>> getBookingsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(bookingService.getBookingsByEmail(email));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/car/{carId}/booked-ranges")
    public ResponseEntity<List<com.example.carrental.dto.BookedDateRange>> getCarBookedRanges(@PathVariable Long carId) {
        return ResponseEntity.ok(bookingService.getCarBookedRanges(carId));
    }

    @GetMapping("/car/{carId}/check-availability")
    public ResponseEntity<com.example.carrental.dto.AvailabilityCheckResponse> checkAvailability(
            @PathVariable Long carId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        return ResponseEntity.ok(bookingService.checkCarAvailability(carId, startDate, endDate));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    // ── Step 2A Custody & Escalation Endpoints ──

    @PutMapping("/{id}/status")
    public ResponseEntity<BookingResponse> transitionStatus(
            @PathVariable Long id,
            @RequestParam String targetStatus,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(custodyEscalationService.transitionStatus(id, targetStatus, reason));
    }

    @PostMapping("/{id}/inspections")
    public ResponseEntity<com.example.carrental.dto.VehicleInspectionResponse> recordInspection(
            @PathVariable Long id,
            @Valid @RequestBody com.example.carrental.dto.VehicleInspectionRequest request) {
        return new ResponseEntity<>(custodyEscalationService.recordInspection(id, request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/alerts")
    public ResponseEntity<com.example.carrental.dto.CustodyAlertResponse> raiseAlert(
            @PathVariable Long id,
            @Valid @RequestBody com.example.carrental.dto.CustodyAlertRequest request) {
        return new ResponseEntity<>(custodyEscalationService.raiseCustodyAlert(id, request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/evaluate-overdue")
    public ResponseEntity<BookingResponse> evaluateOverdue(@PathVariable Long id) {
        return ResponseEntity.ok(custodyEscalationService.evaluateOverdue(id));
    }

    @GetMapping("/{id}/custody-dossier")
    public ResponseEntity<com.example.carrental.dto.CustodyDossierResponse> getCustodyDossier(@PathVariable Long id) {
        return ResponseEntity.ok(custodyEscalationService.getCustodyDossier(id));
    }
}
