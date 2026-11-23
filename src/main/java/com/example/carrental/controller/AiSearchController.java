package com.example.carrental.controller;

import com.example.carrental.dto.AiSearchRequest;
import com.example.carrental.dto.AiSearchResponse;
import com.example.carrental.service.AiSearchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiSearchController {

    private final AiSearchService aiSearchService;

    public AiSearchController(AiSearchService aiSearchService) {
        this.aiSearchService = aiSearchService;
    }

    @PostMapping("/search")
    public ResponseEntity<AiSearchResponse> searchWithAi(@Valid @RequestBody AiSearchRequest request) {
        AiSearchResponse response = aiSearchService.searchWithAi(request);
        return ResponseEntity.ok(response);
    }
}
