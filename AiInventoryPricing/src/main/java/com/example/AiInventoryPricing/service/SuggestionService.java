package com.example.AiInventoryPricing.service;

import com.example.AiInventoryPricing.dto.*;
import com.example.AiInventoryPricing.enums.SuggestionStatus;
import com.example.AiInventoryPricing.entity.PricingSuggestion;
import com.example.AiInventoryPricing.entity.ReorderSuggestion;

import java.util.List;

public interface SuggestionService {
    List<PricingSuggestionDto> getAllPricingSuggestions();
    
    List<PricingSuggestionDto> getPricingSuggestionsByStatus(SuggestionStatus status);
    
    PricingSuggestionDto getPricingSuggestionById(Long id);
    
    PricingSuggestionDto updatePricingSuggestionStatus(Long id, AcceptSuggestionRequestDto requestDto);
    
    PricingSuggestion createPricingSuggestion(PricingSuggestion pricingSuggestion);
    
    List<ReorderSuggestionDto> getAllReorderSuggestions();
    
    List<ReorderSuggestionDto> getReorderSuggestionsByStatus(SuggestionStatus status);
    
    ReorderSuggestionDto getReorderSuggestionById(Long id);
    
    ReorderSuggestionDto updateReorderSuggestionStatus(Long id, AcceptSuggestionRequestDto requestDto);
    
    ReorderSuggestion createReorderSuggestion(ReorderSuggestion reorderSuggestion);
    
    // Methods for manual suggestion creation (existing)
    PricingSuggestionDto generatePricingSuggestionForProduct(Long productId, CreatePricingSuggestionRequestDto requestDto);
    
    ReorderSuggestionDto generateReorderSuggestionForProduct(Long productId, CreateReorderSuggestionRequestDto requestDto);
    
    // New methods for strategy-based on-demand generation
    PricingSuggestionDto generatePricingSuggestionUsingStrategy(Long productId);
    
    ReorderSuggestionDto generateReorderSuggestionUsingStrategy(Long productId);
    
    // Methods for managing active strategies
    boolean setActivePricingStrategy(String strategyName);
    
    boolean setActiveReorderStrategy(String strategyName);
    
    String[] getAvailablePricingStrategies();
    
    String[] getAvailableReorderStrategies();
    
    String getActivePricingStrategy();
    
    String getActiveReorderStrategy();
}