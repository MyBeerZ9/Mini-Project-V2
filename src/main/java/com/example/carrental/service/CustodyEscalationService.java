package com.example.carrental.service;

import com.example.carrental.dto.*;
import com.example.carrental.entity.Booking;
import com.example.carrental.entity.CustodyAlert;
import com.example.carrental.entity.VehicleInspection;
import com.example.carrental.exception.InvalidStateTransitionException;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.CustodyAlertRepository;
import com.example.carrental.repository.VehicleInspectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustodyEscalationService {

    private final BookingRepository bookingRepository;
    private final VehicleInspectionRepository inspectionRepository;
    private final CustodyAlertRepository alertRepository;

    public CustodyEscalationService(BookingRepository bookingRepository,
                                    VehicleInspectionRepository inspectionRepository,
                                    CustodyAlertRepository alertRepository) {
        this.bookingRepository = bookingRepository;
        this.inspectionRepository = inspectionRepository;
        this.alertRepository = alertRepository;
    }

    public BookingResponse transitionStatus(Long bookingId, String targetStatus, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        String current = booking.getStatus();
        validateTransition(current, targetStatus);

        booking.setStatus(targetStatus);
        if (reason != null && !reason.trim().isEmpty()) {
            booking.setEscalationReason(reason.trim());
        }

        if ("COMPLETED".equalsIgnoreCase(targetStatus) && booking.getActualReturnAt() == null) {
            booking.setActualReturnAt(LocalDateTime.now());
            // Release deposit if no dispute/overdue
            if (booking.getOverdueFee() <= 0 && "HELD".equalsIgnoreCase(booking.getDepositStatus())) {
                booking.setDepositStatus("RELEASED");
            }
        }

        if ("ESCALATED_THEFT_HOLD".equalsIgnoreCase(targetStatus)) {
            booking.setDepositStatus("FORFEITED_DISPUTE");
        }

        Booking updated = bookingRepository.save(booking);
        return BookingResponse.fromEntity(updated);
    }

    private void validateTransition(String current, String target) {
        if (current.equalsIgnoreCase(target)) return;

        // Legal state machine rules
        // PENDING_KYC -> CONFIRMED, CANCELLED
        // CONFIRMED -> ACTIVE_TRIP, CANCELLED
        // ACTIVE_TRIP -> OVERDUE, COMPLETED, ESCALATED_THEFT_HOLD
        // OVERDUE -> COMPLETED, ESCALATED_THEFT_HOLD
        // ESCALATED_THEFT_HOLD -> COMPLETED
        switch (current.toUpperCase()) {
            case "PENDING_KYC":
                if (!Set.of("CONFIRMED", "CANCELLED").contains(target.toUpperCase())) {
                    throw new InvalidStateTransitionException("Cannot transition booking from " + current + " to " + target);
                }
                break;
            case "CONFIRMED":
                if (!Set.of("ACTIVE_TRIP", "CANCELLED").contains(target.toUpperCase())) {
                    throw new InvalidStateTransitionException("Cannot transition booking from " + current + " to " + target);
                }
                break;
            case "ACTIVE_TRIP":
                if (!Set.of("OVERDUE", "COMPLETED", "ESCALATED_THEFT_HOLD").contains(target.toUpperCase())) {
                    throw new InvalidStateTransitionException("Cannot transition booking from " + current + " to " + target);
                }
                break;
            case "OVERDUE":
                if (!Set.of("COMPLETED", "ESCALATED_THEFT_HOLD").contains(target.toUpperCase())) {
                    throw new InvalidStateTransitionException("Cannot transition booking from " + current + " to " + target);
                }
                break;
            case "ESCALATED_THEFT_HOLD":
                if (!Set.of("COMPLETED").contains(target.toUpperCase())) {
                    throw new InvalidStateTransitionException("Cannot transition booking from " + current + " to " + target);
                }
                break;
            default:
                throw new InvalidStateTransitionException("Terminal or invalid state: " + current);
        }
    }

    public VehicleInspectionResponse recordInspection(Long bookingId, VehicleInspectionRequest req) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        VehicleInspection inspection = new VehicleInspection();
        inspection.setBooking(booking);
        inspection.setInspectionType(req.getInspectionType().trim().toUpperCase());
        inspection.setOdometerKm(req.getOdometerKm());
        inspection.setFuelOrBatteryPercent(req.getFuelOrBatteryPercent());
        inspection.setConditionNotes(req.getConditionNotes() != null ? req.getConditionNotes().trim() : null);
        inspection.setEvidenceHash(req.getEvidenceHash().trim());
        inspection.setInspectedBy(req.getInspectedBy().trim());
        inspection.setInspectedAt(LocalDateTime.now());

        // Auto transition on pre-trip / post-trip
        if ("PRE_TRIP".equalsIgnoreCase(inspection.getInspectionType()) && "CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            booking.setStatus("ACTIVE_TRIP");
            bookingRepository.save(booking);
        }

        VehicleInspection saved = inspectionRepository.save(inspection);
        return VehicleInspectionResponse.fromEntity(saved);
    }

    public CustodyAlertResponse raiseCustodyAlert(Long bookingId, CustodyAlertRequest req) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        CustodyAlert alert = new CustodyAlert();
        alert.setBooking(booking);
        alert.setAlertType(req.getAlertType().trim().toUpperCase());
        alert.setSeverity(req.getSeverity().trim().toUpperCase());
        alert.setLocationSnapshot(req.getLocationSnapshot() != null ? req.getLocationSnapshot().trim() : null);
        alert.setDetails(req.getDetails() != null ? req.getDetails().trim() : null);
        alert.setResolved(false);
        alert.setCreatedAt(LocalDateTime.now());

        // Automatic escalation: THEFT_SUSPECTED or CRITICAL severity triggers ESCALATED_THEFT_HOLD
        if ("THEFT_SUSPECTED".equalsIgnoreCase(alert.getAlertType()) || "CRITICAL".equalsIgnoreCase(alert.getSeverity())) {
            booking.setStatus("ESCALATED_THEFT_HOLD");
            booking.setDepositStatus("FORFEITED_DISPUTE");
            booking.setEscalationReason("Automated Theft Hold Triggered: " + alert.getAlertType() + " - " + alert.getDetails());
            bookingRepository.save(booking);
        }

        CustodyAlert saved = alertRepository.save(alert);
        return CustodyAlertResponse.fromEntity(saved);
    }

    public BookingResponse evaluateOverdue(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        LocalDate now = LocalDate.now();
        if (now.isAfter(booking.getEndDate()) && "ACTIVE_TRIP".equalsIgnoreCase(booking.getStatus())) {
            long daysLate = ChronoUnit.DAYS.between(booking.getEndDate(), now);
            if (daysLate > 0) {
                // Fine: 1.5x daily car price per late day
                double finePerDay = (booking.getCar() != null ? booking.getCar().getPricePerDay() : 2000.0) * 1.5;
                double totalOverdueFee = Math.round(daysLate * finePerDay * 100.0) / 100.0;

                booking.setStatus("OVERDUE");
                booking.setOverdueFee(totalOverdueFee);
                booking.setDepositStatus("FORFEITED_DISPUTE");
                booking.setEscalationReason("Overdue by " + daysLate + " days past scheduled end date " + booking.getEndDate());

                // Auto-create alert
                CustodyAlert alert = new CustodyAlert();
                alert.setBooking(booking);
                alert.setAlertType("OVERDUE_SLA");
                alert.setSeverity(daysLate > 2 ? "HIGH" : "MEDIUM");
                alert.setDetails("Vehicle overdue by " + daysLate + " days. Accumulated fine: ₹" + totalOverdueFee);
                alert.setResolved(false);
                alertRepository.save(alert);

                booking = bookingRepository.save(booking);
            }
        }
        return BookingResponse.fromEntity(booking);
    }

    @Transactional(readOnly = true)
    public CustodyDossierResponse getCustodyDossier(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        List<VehicleInspectionResponse> inspections = inspectionRepository.findByBookingIdOrderByInspectedAtAsc(bookingId)
                .stream().map(VehicleInspectionResponse::fromEntity).collect(Collectors.toList());

        List<CustodyAlertResponse> alerts = alertRepository.findByBookingIdOrderByCreatedAtDesc(bookingId)
                .stream().map(CustodyAlertResponse::fromEntity).collect(Collectors.toList());

        CustodyDossierResponse dossier = new CustodyDossierResponse();
        dossier.setBookingId(booking.getId());
        dossier.setBookingReference(booking.getBookingReference());
        dossier.setBookingStatus(booking.getStatus());
        dossier.setCustomerName(booking.getCustomerName());
        dossier.setCustomerEmail(booking.getCustomerEmail());
        dossier.setCustomerPhone(booking.getCustomerPhone());
        dossier.setRenterDlNumber(booking.getRenterDlNumber());
        dossier.setKycStatus(booking.getKycStatus());
        dossier.setRenterRiskScore(booking.getRenterRiskScore());
        dossier.setSecurityDeposit(booking.getSecurityDeposit());
        dossier.setDepositStatus(booking.getDepositStatus());
        dossier.setOverdueFee(booking.getOverdueFee());
        dossier.setEscalationReason(booking.getEscalationReason());
        dossier.setStartDate(booking.getStartDate());
        dossier.setScheduledEndDate(booking.getEndDate());
        dossier.setActualReturnAt(booking.getActualReturnAt());
        dossier.setInspections(inspections);
        dossier.setAlerts(alerts);

        return dossier;
    }
}
