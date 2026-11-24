package com.example.carrental.controller;

import com.example.carrental.dto.PartnerFleetComplianceReport;
import com.example.carrental.dto.PartnerRequest;
import com.example.carrental.dto.PartnerResponse;
import com.example.carrental.entity.Car;
import com.example.carrental.service.PartnerFleetService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    private final PartnerFleetService partnerFleetService;

    public PartnerController(PartnerFleetService partnerFleetService) {
        this.partnerFleetService = partnerFleetService;
    }

    @PostMapping
    public ResponseEntity<PartnerResponse> createPartner(@Valid @RequestBody PartnerRequest request) {
        PartnerResponse response = partnerFleetService.createPartner(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PartnerResponse>> getAllPartners() {
        return ResponseEntity.ok(partnerFleetService.getAllPartners());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartnerResponse> getPartnerById(@PathVariable Long id) {
        return ResponseEntity.ok(partnerFleetService.getPartnerById(id));
    }

    @PutMapping("/cars/{carId}/assign/{partnerId}")
    public ResponseEntity<Car> assignCarToPartner(@PathVariable Long carId, @PathVariable Long partnerId) {
        return ResponseEntity.ok(partnerFleetService.assignCarToPartner(carId, partnerId));
    }

    @PutMapping("/cars/{carId}/compliance")
    public ResponseEntity<Car> updateComplianceDates(
            @PathVariable Long carId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rcExpiry,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate insuranceExpiry,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pucExpiry,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate permitExpiry) {
        return ResponseEntity.ok(partnerFleetService.updateComplianceDates(carId, rcExpiry, insuranceExpiry, pucExpiry, permitExpiry));
    }

    @GetMapping("/fleet/compliance-report")
    public ResponseEntity<List<PartnerFleetComplianceReport>> getFleetComplianceReport() {
        return ResponseEntity.ok(partnerFleetService.getFleetComplianceReport());
    }
}
