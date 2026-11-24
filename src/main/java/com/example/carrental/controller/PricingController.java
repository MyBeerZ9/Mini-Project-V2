package com.example.carrental.controller;

import com.example.carrental.dto.PricingAdviceResponse;
import com.example.carrental.service.PricingAdvisorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ai")
public class PricingController {

    private final PricingAdvisorService pricingAdvisorService;

    public PricingController(PricingAdvisorService pricingAdvisorService) {
        this.pricingAdvisorService = pricingAdvisorService;
    }

    @GetMapping("/pricing-advice/{carId}")
    public ResponseEntity<PricingAdviceResponse> getPricingAdvice(
            @PathVariable Long carId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(pricingAdvisorService.getPricingAdvice(carId, startDate, endDate));
    }
}
