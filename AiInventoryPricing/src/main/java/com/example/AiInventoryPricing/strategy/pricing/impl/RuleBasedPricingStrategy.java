package com.example.AiInventoryPricing.strategy.pricing.impl;

import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.entity.PricingSuggestion;
import com.example.AiInventoryPricing.enums.ChangeDirection;
import com.example.AiInventoryPricing.enums.SuggestionStatus;
import com.example.AiInventoryPricing.enums.TriggerReason;
import com.example.AiInventoryPricing.strategy.pricing.PricingStrategy;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Rule-based pricing strategy implementation
 * Adjusts prices based on inventory levels and demand velocity
 */
@Component
public class RuleBasedPricingStrategy implements PricingStrategy {
    
    private static final String STRATEGY_NAME = "RULE_BASED";
    private static final BigDecimal HIGH_CONFIDENCE = new BigDecimal("0.90");
    private static final BigDecimal MEDIUM_CONFIDENCE = new BigDecimal("0.75");
    private static final BigDecimal LOW_CONFIDENCE = new BigDecimal("0.60");
    
    @Override
    public PricingSuggestion generatePricingSuggestion(Product product) {
        PricingSuggestion suggestion = new PricingSuggestion();
        suggestion.setProduct(product);
        suggestion.setCurrentPrice(product.getCurrentPrice());
        suggestion.setStatus(SuggestionStatus.PENDING);
        suggestion.setTriggerReason(TriggerReason.MANUAL); // Using existing enum value
        
        BigDecimal recommendedPrice = calculateRecommendedPrice(product);
        suggestion.setRecommendedPrice(recommendedPrice);
        suggestion.setChangeDirection(determineChangeDirection(product.getCurrentPrice(), recommendedPrice));
        suggestion.setConfidence(determineConfidence(product));
        suggestion.setReasoning(generateReasoning(product, recommendedPrice));
        
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
    
    private BigDecimal calculateRecommendedPrice(Product product) {
        BigDecimal currentPrice = product.getCurrentPrice();
        int stockLevel = product.getStockLevel();
        int reorderThreshold = product.getReorderThreshold();
        int demandVelocity = product.getDemandVelocity();
        
        // If stock is very low, increase price to reduce demand
        if (stockLevel <= reorderThreshold * 0.5) {
            return currentPrice.multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_UP);
        }
        
        // If stock is high and demand is low, decrease price to increase demand
        if (stockLevel >= reorderThreshold * 3 && demandVelocity <= 3) {
            return currentPrice.multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP);
        }
        
        // If demand is high, slightly increase price
        if (demandVelocity >= 10) {
            return currentPrice.multiply(new BigDecimal("1.05")).setScale(2, RoundingMode.HALF_UP);
        }
        
        // If demand is very low, decrease price
        if (demandVelocity <= 2) {
            return currentPrice.multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP);
        }
        
        // Default: no change
        return currentPrice;
    }
    
    private ChangeDirection determineChangeDirection(BigDecimal currentPrice, BigDecimal recommendedPrice) {
        int comparison = currentPrice.compareTo(recommendedPrice);
        if (comparison < 0) {
            return ChangeDirection.INCREASE;
        } else if (comparison > 0) {
            return ChangeDirection.DECREASE;
        } else {
            return ChangeDirection.HOLD;
        }
    }
    
    private BigDecimal determineConfidence(Product product) {
        int stockLevel = product.getStockLevel();
        int reorderThreshold = product.getReorderThreshold();
        int demandVelocity = product.getDemandVelocity();
        
        // High confidence when we have clear signals
        if ((stockLevel <= reorderThreshold * 0.5) || (stockLevel >= reorderThreshold * 3 && demandVelocity <= 3)) {
            return HIGH_CONFIDENCE;
        }
        
        // Medium confidence for strong conditions
        if (demandVelocity >= 10 || demandVelocity <= 2) {
            return MEDIUM_CONFIDENCE;
        }
        
        // Higher confidence when approaching reorder threshold but with normal demand (5)
        if (stockLevel <= reorderThreshold * 1.5 && demandVelocity > 2 && demandVelocity < 10) {
            return new BigDecimal("0.80");
        }
        
        // Lower confidence when conditions are neutral
        return LOW_CONFIDENCE;
    }
    
    private String generateReasoning(Product product, BigDecimal recommendedPrice) {
        int stockLevel = product.getStockLevel();
        int reorderThreshold = product.getReorderThreshold();
        int demandVelocity = product.getDemandVelocity();
        BigDecimal currentPrice = product.getCurrentPrice();
        
        StringBuilder reasoning = new StringBuilder();
        reasoning.append("Rule-based pricing adjustment for product ").append(product.getName()).append(". ");
        
        if (stockLevel <= reorderThreshold * 0.5) {
            reasoning.append("Stock critically low (").append(stockLevel).append("/").append(reorderThreshold)
                    .append("), increasing price by 15% to manage demand.");
        } else if (stockLevel >= reorderThreshold * 3 && demandVelocity <= 3) {
            reasoning.append("Excess stock (").append(stockLevel).append("/").append(reorderThreshold)
                    .append(") with low demand velocity (").append(demandVelocity)
                    .append("), decreasing price by 15% to boost sales.");
        } else if (demandVelocity >= 10) {
            reasoning.append("High demand velocity (").append(demandVelocity)
                    .append("), increasing price by 5% to optimize revenue.");
        } else if (demandVelocity <= 2) {
            reasoning.append("Low demand velocity (").append(demandVelocity)
                    .append("), decreasing price by 5% to stimulate demand.");
        } else {
            reasoning.append("Normal conditions, no price adjustment recommended.");
        }
        
        reasoning.append(" Current price: $").append(currentPrice)
                .append(", Recommended price: $").append(recommendedPrice);
        
        return reasoning.toString();
    }
}