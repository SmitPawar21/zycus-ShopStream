package com.example.AiInventoryPricing.ai.validator;

import com.example.AiInventoryPricing.ai.dto.CommerceContext;
import com.example.AiInventoryPricing.ai.dto.PricingRecommendation;
import com.example.AiInventoryPricing.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PricingRecommendationValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(PricingRecommendationValidator.class);
    private static final BigDecimal MIN_CONFIDENCE = new BigDecimal("0.50");
    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");
    
    public boolean validate(PricingRecommendation recommendation, CommerceContext context) {
        if (recommendation == null) {
            logger.warn("Pricing recommendation is null");
            return false;
        }
        
        // Validate confidence
        if (recommendation.getConfidence() == null || 
            recommendation.getConfidence().compareTo(MIN_CONFIDENCE) < 0) {
            logger.warn("Pricing recommendation confidence {} is below minimum threshold {}", 
                       recommendation.getConfidence(), MIN_CONFIDENCE);
            return false;
        }
        
        // Validate price
        if (recommendation.getRecommendedPrice() == null || 
            recommendation.getRecommendedPrice().compareTo(MIN_PRICE) < 0) {
            logger.warn("Pricing recommendation price {} is invalid", 
                       recommendation.getRecommendedPrice());
            return false;
        }
        
        // Validate direction
        if (recommendation.getChangeDirection() == null) {
            logger.warn("Pricing recommendation change direction is null");
            return false;
        }
        
        // Validate reasoning
        if (recommendation.getReasoning() == null || recommendation.getReasoning().trim().isEmpty()) {
            logger.warn("Pricing recommendation reasoning is empty");
            return false;
        }
        
        logger.info("Pricing recommendation validation passed");
        return true;
    }
    
    public boolean shouldUseAiRecommendation(PricingRecommendation aiRec, Product product) {
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