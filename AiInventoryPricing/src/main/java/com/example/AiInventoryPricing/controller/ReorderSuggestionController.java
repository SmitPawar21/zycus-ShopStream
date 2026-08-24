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
@RequestMapping("/api/reorder-suggestions")
@CrossOrigin(origins = "*")
public class ReorderSuggestionController {
    
    @Autowired
    private SuggestionService suggestionService;
    
    @GetMapping
    public ResponseEntity<List<ReorderSuggestionDto>> getAllReorderSuggestions(
            @RequestParam(required = false) SuggestionStatus status) {
        
        List<ReorderSuggestionDto> suggestions;
        
        if (status != null) {
            suggestions = suggestionService.getReorderSuggestionsByStatus(status);
        } else {
            suggestions = suggestionService.getAllReorderSuggestions();
        }
        
        return ResponseEntity.ok(suggestions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ReorderSuggestionDto> getReorderSuggestionById(@PathVariable Long id) {
        ReorderSuggestionDto suggestionDto = suggestionService.getReorderSuggestionById(id);
        return ResponseEntity.ok(suggestionDto);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<ReorderSuggestionDto> updateReorderSuggestionStatus(
            @PathVariable Long id, 
            @Valid @RequestBody AcceptSuggestionRequestDto requestDto) {
        ReorderSuggestionDto suggestionDto = suggestionService.updateReorderSuggestionStatus(id, requestDto);
        return ResponseEntity.ok(suggestionDto);
    }
}