package com.example.AiInventoryPricing.controller;

import com.example.AiInventoryPricing.dto.*;
import com.example.AiInventoryPricing.enums.SuggestionStatus;
import com.example.AiInventoryPricing.service.SuggestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing-suggestions")
@CrossOrigin(origins = "*")
public class PricingSuggestionController {
    
    @Autowired
    private SuggestionService suggestionService;
    
    @GetMapping
    public ResponseEntity<List<PricingSuggestionDto>> getAllPricingSuggestions(
            @RequestParam(required = false) SuggestionStatus status) {
        
        List<PricingSuggestionDto> suggestions;
        
        if (status != null) {
            suggestions = suggestionService.getPricingSuggestionsByStatus(status);
        } else {
            suggestions = suggestionService.getAllPricingSuggestions();
        }
        
        return ResponseEntity.ok(suggestions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PricingSuggestionDto> getPricingSuggestionById(@PathVariable Long id) {
        PricingSuggestionDto suggestionDto = suggestionService.getPricingSuggestionById(id);
        return ResponseEntity.ok(suggestionDto);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<PricingSuggestionDto> updatePricingSuggestionStatus(
            @PathVariable Long id, 
            @Valid @RequestBody AcceptSuggestionRequestDto requestDto) {
        PricingSuggestionDto suggestionDto = suggestionService.updatePricingSuggestionStatus(id, requestDto);
        return ResponseEntity.ok(suggestionDto);
    }
}