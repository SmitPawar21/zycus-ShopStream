package com.example.AiInventoryPricing.ai.validator;

import com.example.AiInventoryPricing.ai.dto.CommerceContext;
import com.example.AiInventoryPricing.ai.dto.ReorderRecommendation;
import com.example.AiInventoryPricing.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ReorderRecommendationValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ReorderRecommendationValidator.class);
    private static final BigDecimal MIN_CONFIDENCE = new BigDecimal("0.50");
    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 10000;
    private static final int MIN_LEAD_TIME = 1;
    private static final int MAX_LEAD_TIME = 365;
    
    public boolean validate(ReorderRecommendation recommendation, CommerceContext context) {
        if (recommendation == null) {
            logger.warn("Reorder recommendation is null");
            return false;
        }
        
        // Validate confidence
        if (recommendation.getConfidence() == null || 
            recommendation.getConfidence().compareTo(MIN_CONFIDENCE) < 0) {
            logger.warn("Reorder recommendation confidence {} is below minimum threshold {}", 
                       recommendation.getConfidence(), MIN_CONFIDENCE);
            return false;
        }
        
        // Validate quantity
        if (recommendation.getRecommendedQuantity() == null || 
            recommendation.getRecommendedQuantity() < MIN_QUANTITY ||
            recommendation.getRecommendedQuantity() > MAX_QUANTITY) {
            logger.warn("Reorder recommendation quantity {} is outside valid range [{}, {}]", 
                       recommendation.getRecommendedQuantity(), MIN_QUANTITY, MAX_QUANTITY);
            return false;
        }
        
        // Validate lead time
        if (recommendation.getSuggestedLeadTimeDays() == null || 
            recommendation.getSuggestedLeadTimeDays() < MIN_LEAD_TIME ||
            recommendation.getSuggestedLeadTimeDays() > MAX_LEAD_TIME) {
            logger.warn("Reorder recommendation lead time {} is outside valid range [{}, {}]", 
                       recommendation.getSuggestedLeadTimeDays(), MIN_LEAD_TIME, MAX_LEAD_TIME);
            return false;
        }
        
        // Validate reasoning
        if (recommendation.getReasoning() == null || recommendation.getReasoning().trim().isEmpty()) {
            logger.warn("Reorder recommendation reasoning is empty");
            return false;
        }
        
        logger.info("Reorder recommendation validation passed");
        return true;
    }
    
    public boolean shouldUseAiRecommendation(ReorderRecommendation aiRec, Product product) {
        // If AI recommendation is valid, use it
        CommerceContext context = createContextFromProduct(product);
        if (validate(aiRec, context)) {
            return true;
        }
        
        // Otherwise fall back to rule-based
        return false;
    }
    
    private CommerceContext createContextFromProduct(Product product) {
        return new CommerceContext(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getCategory(),
            product.getCurrentPrice(),
            product.getStockLevel(),
            product.getReorderThreshold(),
            product.getDemandVelocity(),
            product.getLifecycleStatus(),
            product.getCostPrice()
        );
    }
}