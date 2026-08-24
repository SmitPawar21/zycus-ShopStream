package com.example.AiInventoryPricing.controller;

import com.example.AiInventoryPricing.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/strategies")
@CrossOrigin(origins = "*")
public class StrategyController {
    
    @Autowired
    private SuggestionService suggestionService;
    
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableStrategies() {
        Map<String, Object> response = new HashMap<>();
        response.put("pricingStrategies", suggestionService.getAvailablePricingStrategies());
        response.put("reorderStrategies", suggestionService.getAvailableReorderStrategies());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveStrategies() {
        Map<String, Object> response = new HashMap<>();
        response.put("pricingStrategy", suggestionService.getActivePricingStrategy());
        response.put("reorderStrategy", suggestionService.getActiveReorderStrategy());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/activate/pricing/{strategyName}")
    public ResponseEntity<Map<String, Object>> activatePricingStrategy(@PathVariable String strategyName) {
        boolean success = suggestionService.setActivePricingStrategy(strategyName);
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("message", "Pricing strategy activated successfully");
            response.put("activeStrategy", strategyName);
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Strategy not found: " + strategyName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @PostMapping("/activate/reorder/{strategyName}")
    public ResponseEntity<Map<String, Object>> activateReorderStrategy(@PathVariable String strategyName) {
        boolean success = suggestionService.setActiveReorderStrategy(strategyName);
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("message", "Reorder strategy activated successfully");
            response.put("activeStrategy", strategyName);
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Strategy not found: " + strategyName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}