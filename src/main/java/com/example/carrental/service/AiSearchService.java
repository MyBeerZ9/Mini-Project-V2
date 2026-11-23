package com.example.carrental.service;

import com.example.carrental.config.GeminiConfig;
import com.example.carrental.dto.AiSearchCriteria;
import com.example.carrental.dto.AiSearchRequest;
import com.example.carrental.dto.AiSearchResponse;
import com.example.carrental.dto.CarResponse;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiSearchService {

    private static final Logger logger = LoggerFactory.getLogger(AiSearchService.class);

    private final GeminiConfig geminiConfig;
    private final CarService carService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiSearchService(GeminiConfig geminiConfig,
                           CarService carService,
                           RestTemplate restTemplate,
                           ObjectMapper objectMapper) {
        this.geminiConfig = geminiConfig;
        this.carService = carService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public AiSearchResponse searchWithAi(AiSearchRequest request) {
        String query = request.getQuery();
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        AiSearchCriteria criteria = extractCriteria(query);
        logger.info("Extracted Search Criteria: {}", criteria);

        List<CarResponse> matchingCars = carService.searchCars(
                criteria.getLocation(),
                criteria.getCarType(),
                criteria.getFuelType(),
                criteria.getTransmission(),
                criteria.getMaxPricePerDay(),
                criteria.getMinSeats(),
                criteria.getPreferredBrand()
        );

        return new AiSearchResponse(criteria, matchingCars);
    }

    public AiSearchCriteria extractCriteria(String userQuery) {
        if (geminiConfig.isConfigured()) {
            try {
                return callGeminiApi(userQuery);
            } catch (Exception e) {
                logger.warn("Gemini API call failed or timed out. Falling back to local NLP parser. Reason: {}", e.getMessage());
                return parseLocallyWithNlp(userQuery);
            }
        } else {
            logger.info("GEMINI_API_KEY not configured. Using local smart NLP criteria parser for viva demonstration.");
            return parseLocallyWithNlp(userQuery);
        }
    }

    private AiSearchCriteria callGeminiApi(String userQuery) throws Exception {
        String systemInstruction = """
                You are a car rental search assistant.
                Extract search criteria from the user's request.
                Return ONLY valid JSON.
                
                Fields:
                location (string or null)
                carType (one of: Hatchback, Sedan, SUV, MUV, Luxury, Electric, or null)
                fuelType (one of: Petrol, Diesel, Electric, Hybrid, CNG, or null)
                transmission (one of: Manual, Automatic, or null)
                maxPricePerDay (number or null)
                minSeats (number or null)
                preferredBrand (string or null)
                
                If a field is not mentioned, return null.
                Do not invent values.
                
                Example JSON output format:
                {
                  "location": "Delhi",
                  "carType": "SUV",
                  "fuelType": null,
                  "transmission": "Automatic",
                  "maxPricePerDay": 3000,
                  "minSeats": null,
                  "preferredBrand": null
                }
                """;

        String prompt = systemInstruction + "\n\nUser request: \"" + userQuery + "\"\n\nJSON output:";

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> contentsItem = Map.of("parts", List.of(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(contentsItem));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.1);
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
                return cleanAndParseJson(rawText);
            }
        }

        throw new RuntimeException("Empty or invalid response from Gemini API");
    }

    private AiSearchCriteria cleanAndParseJson(String rawText) {
        try {
            String json = rawText.trim();
            if (json.startsWith("```json")) {
                json = json.substring(7);
            } else if (json.startsWith("```")) {
                json = json.substring(3);
            }
            if (json.endsWith("```")) {
                json = json.substring(0, json.length() - 3);
            }
            json = json.trim();

            return objectMapper.readValue(json, AiSearchCriteria.class);
        } catch (Exception e) {
            logger.error("Failed to parse Gemini JSON output: {}. Raw: {}", e.getMessage(), rawText);
            return parseLocallyWithNlp(rawText);
        }
    }

    /**
     * Resilient smart local NLP extractor.
     * Ensures viva demonstrations and offline presentations work seamlessly
     * even if internet connectivity drops or API key quota is reached.
     */
    public AiSearchCriteria parseLocallyWithNlp(String text) {
        AiSearchCriteria criteria = new AiSearchCriteria();
        String lower = text.toLowerCase();

        // 1. Location extraction (popular Indian cities)
        String[] cities = {"delhi", "noida", "gurgaon", "gurugram", "ghaziabad", "jaipur", "lucknow", "agra", "mumbai", "pune", "bangalore", "bengaluru", "hyderabad", "chennai", "chandigarh"};
        for (String city : cities) {
            if (lower.contains(city)) {
                if (city.equals("gurugram")) city = "Gurgaon";
                else if (city.equals("bengaluru")) city = "Bangalore";
                criteria.setLocation(capitalize(city));
                break;
            }
        }

        // 2. Car Type extraction
        if (lower.contains("suv")) {
            criteria.setCarType("SUV");
        } else if (lower.contains("sedan")) {
            criteria.setCarType("Sedan");
        } else if (lower.contains("hatchback") || lower.contains("hatch")) {
            criteria.setCarType("Hatchback");
        } else if (lower.contains("muv") || lower.contains("mpv") || lower.contains("van")) {
            criteria.setCarType("MUV");
        } else if (lower.contains("luxury")) {
            criteria.setCarType("Luxury");
        } else if (lower.contains("electric") || lower.contains("ev")) {
            criteria.setCarType("Electric");
        }

        // 3. Transmission
        if (lower.contains("automatic") || lower.contains("auto") || lower.contains("amt") || lower.contains("at")) {
            criteria.setTransmission("Automatic");
        } else if (lower.contains("manual")) {
            criteria.setTransmission("Manual");
        }

        // 4. Fuel Type
        if (lower.contains("diesel")) {
            criteria.setFuelType("Diesel");
        } else if (lower.contains("petrol")) {
            criteria.setFuelType("Petrol");
        } else if (lower.contains("cng")) {
            criteria.setFuelType("CNG");
        } else if (lower.contains("hybrid")) {
            criteria.setFuelType("Hybrid");
        } else if (lower.contains("electric") && criteria.getCarType() == null) {
            criteria.setFuelType("Electric");
        }

        // 5. Max Price extraction (e.g. "under 3000", "< 4000", "below 2500", "within 3500", "budget 3000", "under ₹3000")
        Pattern pricePattern = Pattern.compile("(?:under|below|less than|within|max(?:imum)?|upto|up to|budget|₹|rs\\.?|inr)\\s*₹?\\s*(\\d{3,6})");
        Matcher priceMatcher = pricePattern.matcher(lower);
        if (priceMatcher.find()) {
            try {
                criteria.setMaxPricePerDay(Double.parseDouble(priceMatcher.group(1)));
            } catch (NumberFormatException ignored) {}
        } else {
            // General number followed by "per day" or "a day"
            Pattern perDayPattern = Pattern.compile("(\\d{3,6})\\s*(?:(?:rs|inr|rupees)?\\s*(?:/|per|a)\\s*day)");
            Matcher perDayMatcher = perDayPattern.matcher(lower);
            if (perDayMatcher.find()) {
                try {
                    criteria.setMaxPricePerDay(Double.parseDouble(perDayMatcher.group(1)));
                } catch (NumberFormatException ignored) {}
            }
        }

        // 6. Minimum Seats extraction (e.g. "5 seater", "7 seats", "6 seat")
        Pattern seatPattern = Pattern.compile("(\\d+)\\s*(?:-|\\s)?(?:seater|seats|seat|person)");
        Matcher seatMatcher = seatPattern.matcher(lower);
        if (seatMatcher.find()) {
            try {
                criteria.setMinSeats(Integer.parseInt(seatMatcher.group(1)));
            } catch (NumberFormatException ignored) {}
        }

        // 7. Preferred Brand
        String[] brands = {"hyundai", "kia", "tata", "maruti", "suzuki", "honda", "toyota", "mahindra", "mg", "bmw", "mercedes", "audi", "skoda", "volkswagen"};
        for (String brand : brands) {
            if (lower.contains(brand)) {
                criteria.setPreferredBrand(capitalize(brand));
                break;
            }
        }

        return criteria;
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
