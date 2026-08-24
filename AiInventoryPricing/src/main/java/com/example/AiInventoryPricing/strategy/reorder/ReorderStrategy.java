package com.example.AiInventoryPricing.strategy.reorder;

import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.entity.ReorderSuggestion;
import java.math.BigDecimal;

/**
 * Interface for reorder strategy implementations
 */
public interface ReorderStrategy {
    
    /**
     * Generate a reorder suggestion for a product
     * @param product The product to generate reorder suggestion for
     * @return ReorderSuggestion with recommended quantity and reasoning
     */
    ReorderSuggestion generateReorderSuggestion(Product product);
    
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