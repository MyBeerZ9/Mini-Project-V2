package com.example.carrental.service;

import com.example.carrental.dto.PartnerFleetComplianceReport;
import com.example.carrental.dto.PartnerRequest;
import com.example.carrental.dto.PartnerResponse;
import com.example.carrental.entity.Car;
import com.example.carrental.entity.Partner;
import com.example.carrental.exception.ComplianceException;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.repository.PartnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PartnerFleetService {

    private final PartnerRepository partnerRepository;
    private final CarRepository carRepository;

    public PartnerFleetService(PartnerRepository partnerRepository, CarRepository carRepository) {
        this.partnerRepository = partnerRepository;
        this.carRepository = carRepository;
    }

    public PartnerResponse createPartner(PartnerRequest request) {
        if (partnerRepository.existsByPartnerCodeIgnoreCase(request.getPartnerCode())) {
            throw new IllegalArgumentException("Partner code already exists: " + request.getPartnerCode());
        }

        Partner partner = new Partner();
        partner.setPartnerName(request.getPartnerName().trim());
        partner.setPartnerCode(request.getPartnerCode().trim().toUpperCase());
        partner.setContactEmail(request.getContactEmail().trim());
        partner.setContactPhone(request.getContactPhone().trim());
        partner.setCity(request.getCity().trim());
        if (request.getRevenueSharePercent() != null) {
            partner.setRevenueSharePercent(request.getRevenueSharePercent());
        }
        partner.setVerified(true);
        partner.setActive(true);

        Partner saved = partnerRepository.save(partner);
        return PartnerResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PartnerResponse> getAllPartners() {
        return partnerRepository.findAll()
                .stream()
                .map(PartnerResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PartnerResponse getPartnerById(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + id));
        return PartnerResponse.fromEntity(partner);
    }

    public Car assignCarToPartner(Long carId, Long partnerId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + carId));
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + partnerId));

        car.setPartner(partner);
        return carRepository.save(car);
    }

    public Car updateComplianceDates(Long carId, LocalDate rcExpiry, LocalDate insuranceExpiry, LocalDate pucExpiry, LocalDate permitExpiry) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + carId));

        car.setRcExpiryDate(rcExpiry);
        car.setInsuranceExpiryDate(insuranceExpiry);
        car.setPucExpiryDate(pucExpiry);
        car.setPermitExpiryDate(permitExpiry);

        return carRepository.save(car);
    }

    public void validateCarBookingCompliance(Car car, LocalDate startDate) {
        if (!car.isComplianceValid(startDate)) {
            List<String> issues = new ArrayList<>();
            LocalDate checkDate = (startDate != null) ? startDate : LocalDate.now();
            if (car.getRcExpiryDate() != null && car.getRcExpiryDate().isBefore(checkDate)) {
                issues.add("RC expired on " + car.getRcExpiryDate());
            }
            if (car.getInsuranceExpiryDate() != null && car.getInsuranceExpiryDate().isBefore(checkDate)) {
                issues.add("Insurance expired on " + car.getInsuranceExpiryDate());
            }
            if (car.getPucExpiryDate() != null && car.getPucExpiryDate().isBefore(checkDate)) {
                issues.add("PUC expired on " + car.getPucExpiryDate());
            }
            if (car.getPermitExpiryDate() != null && car.getPermitExpiryDate().isBefore(checkDate)) {
                issues.add("Permit expired on " + car.getPermitExpiryDate());
            }
            throw new ComplianceException("Car compliance violation: " + String.join(", ", issues));
        }
    }

    @Transactional(readOnly = true)
    public List<PartnerFleetComplianceReport> getFleetComplianceReport() {
        return carRepository.findAll().stream().map(car -> {
            PartnerFleetComplianceReport report = new PartnerFleetComplianceReport();
            report.setCarId(car.getId());
            report.setCarName(car.getCarName());
            report.setPartnerName(car.getPartner() != null ? car.getPartner().getPartnerName() : "Independent Fleet");
            report.setRcExpiryDate(car.getRcExpiryDate());
            report.setInsuranceExpiryDate(car.getInsuranceExpiryDate());
            report.setPucExpiryDate(car.getPucExpiryDate());
            report.setPermitExpiryDate(car.getPermitExpiryDate());

            boolean valid = car.isComplianceValid(LocalDate.now());
            report.setCompliant(valid);
            if (!valid) {
                List<String> issues = new ArrayList<>();
                if (car.getRcExpiryDate() != null && car.getRcExpiryDate().isBefore(LocalDate.now())) issues.add("RC Expired");
                if (car.getInsuranceExpiryDate() != null && car.getInsuranceExpiryDate().isBefore(LocalDate.now())) issues.add("Insurance Expired");
                if (car.getPucExpiryDate() != null && car.getPucExpiryDate().isBefore(LocalDate.now())) issues.add("PUC Expired");
                if (car.getPermitExpiryDate() != null && car.getPermitExpiryDate().isBefore(LocalDate.now())) issues.add("Permit Expired");
                report.setComplianceIssue(String.join("; ", issues));
            } else {
                report.setComplianceIssue("ALL_DOCUMENTS_VALID");
            }
            return report;
        }).collect(Collectors.toList());
    }
}
