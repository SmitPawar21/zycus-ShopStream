package com.example.AiInventoryPricing.strategy.pricing;

import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.entity.PricingSuggestion;
import java.math.BigDecimal;

/**
 * Interface for pricing strategy implementations
 */
public interface PricingStrategy {
    
    /**
     * Generate a pricing suggestion for a product
     * @param product The product to generate pricing suggestion for
     * @return PricingSuggestion with recommended price and reasoning
     */
    PricingSuggestion generatePricingSuggestion(Product product);
    
    /**
     * Get the name of this strategy
     * @return Strategy name
     */
    String getName();
    
    /**
     * Get the confidence level of this strategy (0.00 - 1.00)
     * @return Confidence level
     */
    BigDecimal getConfidence();
}