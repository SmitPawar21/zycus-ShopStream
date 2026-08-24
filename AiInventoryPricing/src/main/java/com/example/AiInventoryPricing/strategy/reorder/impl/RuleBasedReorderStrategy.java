package com.example.AiInventoryPricing.strategy.reorder.impl;

import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.entity.ReorderSuggestion;
import com.example.AiInventoryPricing.enums.SuggestionStatus;
import com.example.AiInventoryPricing.enums.TriggerReason;
import com.example.AiInventoryPricing.strategy.reorder.ReorderStrategy;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Rule-based reorder strategy implementation
 * Calculates reorder quantities based on demand velocity and lead time
 */
@Component
public class RuleBasedReorderStrategy implements ReorderStrategy {
    
    private static final String STRATEGY_NAME = "RULE_BASED";
    private static final BigDecimal HIGH_CONFIDENCE = new BigDecimal("0.90");
    private static final BigDecimal MEDIUM_CONFIDENCE = new BigDecimal("0.75");
    private static final BigDecimal LOW_CONFIDENCE = new BigDecimal("0.60");
    private static final int DEFAULT_LEAD_TIME_DAYS = 7;
    
    @Override
    public ReorderSuggestion generateReorderSuggestion(Product product) {
        ReorderSuggestion suggestion = new ReorderSuggestion();
        suggestion.setProduct(product);
        suggestion.setCurrentStock(product.getStockLevel());
        suggestion.setStatus(SuggestionStatus.PENDING);
        suggestion.setTriggerReason(TriggerReason.MANUAL); // Using existing enum value
        
        int recommendedQuantity = calculateRecommendedQuantity(product);
        suggestion.setRecommendedQuantity(recommendedQuantity);
        suggestion.setSuggestedLeadTimeDays(determineLeadTime(product));
        suggestion.setConfidence(determineConfidence(product));
        suggestion.setReasoning(generateReasoning(product, recommendedQuantity));
        
        return suggestion;
    }
    
    @Override
    public String getName() {
        return STRATEGY_NAME;
    }
    
    @Override
    public BigDecimal getConfidence() {
        return MEDIUM_CONFIDENCE;
    }
    
    private int calculateRecommendedQuantity(Product product) {
        int currentStock = product.getStockLevel();
        int reorderThreshold = product.getReorderThreshold();
        int demandVelocity = product.getDemandVelocity();
        
        // If we're already below threshold, calculate based on demand
        if (currentStock <= reorderThreshold) {
            // Calculate 2 weeks of demand plus safety stock
            int twoWeeksDemand = demandVelocity * 2;
            int safetyStock = Math.max(5, reorderThreshold / 2); // Minimum 5 or half of reorder threshold
            int needed = reorderThreshold + twoWeeksDemand + safetyStock - currentStock;
            return Math.max(needed, reorderThreshold); // At least reorder threshold
        }
        
        // If we're above threshold but getting close, suggest smaller order
        if (currentStock <= reorderThreshold * 1.5) {
            int oneWeekDemand = demandVelocity;
            int safetyStock = reorderThreshold / 3;
            int needed = reorderThreshold + oneWeekDemand + safetyStock - currentStock;
            return Math.max(needed, reorderThreshold / 2);
        }
        
        // We're well stocked, suggest minimal reorder (user can reject)
        return 1;
    }
    
    private int determineLeadTime(Product product) {
        // In a real system, this might come from supplier data
        // For now, we'll use a simple rule based on category
        switch (product.getCategory()) {
            case ELECTRONICS:
                return 14; // Electronics typically have longer lead times
            case APPAREL:
                return 10; // Apparel medium lead time
            case HOME:
                return 7;  // Home goods shorter lead time
            default:
                return DEFAULT_LEAD_TIME_DAYS;
        }
    }
    
    private BigDecimal determineConfidence(Product product) {
        int currentStock = product.getStockLevel();
        int reorderThreshold = product.getReorderThreshold();
        
        // High confidence when stock is critically low
        if (currentStock <= reorderThreshold * 0.3) {
            return HIGH_CONFIDENCE;
        }
        
        // Medium confidence when approaching or below threshold
        if (currentStock <= reorderThreshold * 1.5) {
            return MEDIUM_CONFIDENCE;
        }
        
        // Low confidence when well-stocked
        return LOW_CONFIDENCE;
    }
    
    private String generateReasoning(Product product, int recommendedQuantity) {
        int currentStock = product.getStockLevel();
        int reorderThreshold = product.getReorderThreshold();
        int demandVelocity = product.getDemandVelocity();
        
        StringBuilder reasoning = new StringBuilder();
        reasoning.append("Rule-based reorder calculation for product ").append(product.getName()).append(". ");
        
        if (currentStock <= reorderThreshold) {
            reasoning.append("Stock below threshold (").append(currentStock).append("/").append(reorderThreshold)
                    .append(") with demand velocity of ").append(demandVelocity)
                    .append(". Recommending reorder of ").append(recommendedQuantity)
                    .append(" units to cover 2 weeks demand plus safety stock.");
        } else if (currentStock <= reorderThreshold * 1.5) {
            reasoning.append("Approaching reorder threshold (").append(currentStock).append("/").append(reorderThreshold)
                    .append("). Recommending smaller order of ").append(recommendedQuantity).append(" units.");
        } else {
            reasoning.append("Sufficient inventory (").append(currentStock).append("/").append(reorderThreshold)
                    .append("). No reorder recommended at this time.");
        }
        
        return reasoning.toString();
    }
}