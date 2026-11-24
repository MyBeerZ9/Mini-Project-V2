package com.example.carrental.service;

import com.example.carrental.config.GeminiConfig;
import com.example.carrental.dto.PricingAdviceResponse;
import com.example.carrental.entity.Booking;
import com.example.carrental.entity.Car;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.CarRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PricingAdvisorService {

    private static final Logger logger = LoggerFactory.getLogger(PricingAdvisorService.class);

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Fast in-memory cache for pricing advice (keyed by carId_startDate_endDate)
    private final Map<String, PricingAdviceResponse> cache = new ConcurrentHashMap<>();

    public PricingAdvisorService(CarRepository carRepository,
                                 BookingRepository bookingRepository,
                                 GeminiConfig geminiConfig,
                                 RestTemplate restTemplate,
                                 ObjectMapper objectMapper) {
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
        this.geminiConfig = geminiConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public PricingAdviceResponse getPricingAdvice(Long carId, LocalDate startDate, LocalDate endDate) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + carId));

        LocalDate start = (startDate != null) ? startDate : LocalDate.now().plusDays(1);
        LocalDate end = (endDate != null && !endDate.isBefore(start)) ? endDate : start.plusDays(2);

        String cacheKey = carId + "_" + start + "_" + end;
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        PricingAdviceResponse response;
        if (geminiConfig.isConfigured()) {
            try {
                response = callGeminiPricing(car, start, end);
            } catch (Exception e) {
                logger.warn("Gemini pricing API call failed: {}. Falling back to deterministic yield engine.", e.getMessage());
                response = calculateDeterministicPricing(car, start, end);
            }
        } else {
            response = calculateDeterministicPricing(car, start, end);
        }

        cache.put(cacheKey, response);
        return response;
    }

    public PricingAdviceResponse calculateDeterministicPricing(Car car, LocalDate start, LocalDate end) {
        long days = Math.max(1, ChronoUnit.DAYS.between(start, end) + 1);
        double base = car.getPricePerDay();

        // 1. Weekend multiplier
        double weekendFactor = 1.0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                weekendFactor += 0.08;
            }
        }
        weekendFactor = Math.min(1.25, weekendFactor);

        // 2. Occupancy / fleet demand factor
        List<Booking> upcoming = bookingRepository.findUpcomingActiveBookings(car.getId(), LocalDate.now());
        double demandMultiplier = upcoming.size() >= 2 ? 1.20 : (upcoming.size() == 1 ? 1.10 : 1.0);

        // 3. Segment factor
        double segmentMultiplier = "Luxury".equalsIgnoreCase(car.getCarType()) || "SUV".equalsIgnoreCase(car.getCarType()) ? 1.15 : 1.0;

        double combinedMultiplier = weekendFactor * demandMultiplier * segmentMultiplier;
        double recommended = Math.round(base * combinedMultiplier * 100.0) / 100.0;

        // Apply car min/max bands
        double minBand = car.getMinRateFloor() != null ? car.getMinRateFloor() : Math.round(base * 0.75 * 100.0) / 100.0;
        double maxBand = car.getMaxRateCeiling() != null ? car.getMaxRateCeiling() : Math.round(base * 1.60 * 100.0) / 100.0;
        recommended = Math.max(minBand, Math.min(maxBand, recommended));

        double totalRevenue = Math.round(recommended * days * 100.0) / 100.0;
        double revShare = (car.getPartner() != null) ? car.getPartner().getRevenueSharePercent() : 80.0;
        double partnerYield = Math.round(totalRevenue * (revShare / 100.0) * 100.0) / 100.0;

        PricingAdviceResponse resp = new PricingAdviceResponse();
        resp.setCarId(car.getId());
        resp.setCarName(car.getCarName());
        resp.setLocation(car.getLocation());
        resp.setBasePrice(base);
        resp.setRecommendedDailyRate(recommended);
        resp.setMinRateBand(minBand);
        resp.setMaxRateBand(maxBand);
        resp.setDurationDays((int) days);
        resp.setEstimatedTotalRevenue(totalRevenue);
        resp.setEstimatedPartnerYield(partnerYield);
        resp.setRevenueSharePercent(revShare);
        resp.setDemandLevel(combinedMultiplier >= 1.20 ? "HIGH" : (combinedMultiplier >= 1.08 ? "MEDIUM" : "NORMAL"));
        resp.setRationale(String.format("Deterministic Engine: Base rate ₹%.2f adjusted by weekend factor (%.2fx), occupancy factor (%.2fx), and segment factor (%.2fx).",
                base, weekendFactor, demandMultiplier, segmentMultiplier));
        resp.setEngineSource("DETERMINISTIC_RULE_FALLBACK");

        return resp;
    }

    private PricingAdviceResponse callGeminiPricing(Car car, LocalDate start, LocalDate end) throws Exception {
        long days = Math.max(1, ChronoUnit.DAYS.between(start, end) + 1);
        String prompt = String.format("""
            You are CARVIA's AI Dynamic Rate & Yield Optimization Engine.
            Recommend optimal dynamic pricing in JSON only.
            
            Vehicle: %s (%s, %s, Seats: %d)
            Location: %s
            Base Daily Price: %.2f
            Trip Window: %s to %s (%d days)
            
            Return JSON with these exact keys:
            {
              "recommendedDailyRate": number,
              "minRateBand": number,
              "maxRateBand": number,
              "demandLevel": "HIGH" | "MEDIUM" | "NORMAL",
              "rationale": "short explanation"
            }
            """, car.getCarName(), car.getCarType(), car.getFuelType(), car.getSeats(),
                car.getLocation(), car.getPricePerDay(), start, end, days);

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> contentsItem = Map.of("parts", List.of(textPart));
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(contentsItem));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("responseMimeType", "application/json");
        requestBody.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = geminiConfig.getApiUrl() + "?key=" + geminiConfig.getApiKey();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                String rawText = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
                String json = rawText.trim();
                if (json.startsWith("```json")) json = json.substring(7);
                if (json.startsWith("```")) json = json.substring(3);
                if (json.endsWith("```")) json = json.substring(0, json.length() - 3);

                JsonNode parsed = objectMapper.readTree(json.trim());
                double rec = parsed.path("recommendedDailyRate").asDouble(car.getPricePerDay());
                double minB = parsed.path("minRateBand").asDouble(car.getPricePerDay() * 0.75);
                double maxB = parsed.path("maxRateBand").asDouble(car.getPricePerDay() * 1.6);
                String demand = parsed.path("demandLevel").asText("NORMAL");
                String rat = parsed.path("rationale").asText("AI dynamic rate based on demand parameters.");

                double totalRev = Math.round(rec * days * 100.0) / 100.0;
                double revShare = (car.getPartner() != null) ? car.getPartner().getRevenueSharePercent() : 80.0;
                double partnerYield = Math.round(totalRev * (revShare / 100.0) * 100.0) / 100.0;

                PricingAdviceResponse advice = new PricingAdviceResponse();
                advice.setCarId(car.getId());
                advice.setCarName(car.getCarName());
                advice.setLocation(car.getLocation());
                advice.setBasePrice(car.getPricePerDay());
                advice.setRecommendedDailyRate(rec);
                advice.setMinRateBand(minB);
                advice.setMaxRateBand(maxB);
                advice.setDurationDays((int) days);
                advice.setEstimatedTotalRevenue(totalRev);
                advice.setEstimatedPartnerYield(partnerYield);
                advice.setRevenueSharePercent(revShare);
                advice.setDemandLevel(demand);
                advice.setRationale(rat);
                advice.setEngineSource("GEMINI_AI");
                return advice;
            }
        }
        throw new RuntimeException("Invalid response from Gemini pricing API");
    }
}
