package com.example.AiInventoryPricing.strategy.reorder.impl;

import com.example.AiInventoryPricing.ai.dto.CommerceContext;
import com.example.AiInventoryPricing.ai.dto.ReorderRecommendation;
import com.example.AiInventoryPricing.ai.gateway.LLMGateway;
import com.example.AiInventoryPricing.ai.parser.LLMResponseParser;
import com.example.AiInventoryPricing.ai.validator.ReorderRecommendationValidator;
import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.entity.ReorderSuggestion;
import com.example.AiInventoryPricing.enums.SuggestionStatus;
import com.example.AiInventoryPricing.enums.TriggerReason;
import com.example.AiInventoryPricing.strategy.reorder.ReorderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AiReorderStrategy implements ReorderStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(AiReorderStrategy.class);
    private static final String STRATEGY_NAME = "AI";
    private static final BigDecimal FALLBACK_CONFIDENCE = new BigDecimal("0.30");
    
    private final LLMGateway llmGateway;
    private final LLMResponseParser responseParser;
    private final ReorderRecommendationValidator validator;
    private final RuleBasedReorderStrategy fallbackStrategy;
    
    @Autowired
    public AiReorderStrategy(LLMGateway llmGateway,
                           LLMResponseParser responseParser,
                           ReorderRecommendationValidator validator,
                           RuleBasedReorderStrategy fallbackStrategy) {
        this.llmGateway = llmGateway;
        this.responseParser = responseParser;
        this.validator = validator;
        this.fallbackStrategy = fallbackStrategy;
    }
    
    @Override
    public ReorderSuggestion generateReorderSuggestion(Product product) {
        logger.info("Generating AI reorder suggestion for product: {}", product.getSku());
        
        try {
            // Create commerce context
            CommerceContext context = createCommerceContext(product);
            
            // Determine trigger reason
            TriggerReason triggerReason = determineTriggerReason(product);
            
            // Call LLM for recommendation
            String jsonResponse = llmGateway.generateReorderRecommendation(context, triggerReason);
            
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                logger.warn("Empty response from LLM for product: {}", product.getSku());
                return createFallbackSuggestion(product, "Empty response from LLM");
            }
            
            // Parse LLM response
            ReorderRecommendation recommendation = responseParser.parseReorderRecommendation(jsonResponse);
            
            // Validate recommendation
            if (validator.validate(recommendation, context)) {
                // Valid AI recommendation
                return createReorderSuggestionFromRecommendation(product, recommendation, triggerReason);
            } else {
                logger.warn("Invalid AI recommendation for product: {}. Falling back to rule-based strategy.", product.getSku());
                return createFallbackSuggestion(product, "Invalid AI recommendation");
            }
            
        } catch (Exception e) {
            logger.error("Error generating AI reorder suggestion for product: {}", product.getSku(), e);
            return createFallbackSuggestion(product, "Error occurred: " + e.getMessage());
        }
    }
    
    @Override
    public String getName() {
        return STRATEGY_NAME;
    }
    
    @Override
    public BigDecimal getConfidence() {
        return FALLBACK_CONFIDENCE; // Default confidence, actual confidence comes from LLM response
    }
    
    private CommerceContext createCommerceContext(Product product) {
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
    
    private TriggerReason determineTriggerReason(Product product) {
        if (product.getStockLevel() <= product.getReorderThreshold()) {
            return TriggerReason.INVENTORY_LOW;
        } else if (product.getDemandVelocity() > product.getReorderThreshold()) {
            return TriggerReason.DEMAND_SPIKE;
        }
        return TriggerReason.MANUAL;
    }
    
    private ReorderSuggestion createReorderSuggestionFromRecommendation(
            Product product, ReorderRecommendation recommendation, TriggerReason triggerReason) {
        
        ReorderSuggestion suggestion = new ReorderSuggestion();
        suggestion.setProduct(product);
        suggestion.setCurrentStock(product.getStockLevel());
        suggestion.setRecommendedQuantity(recommendation.getRecommendedQuantity());
        suggestion.setSuggestedLeadTimeDays(recommendation.getSuggestedLeadTimeDays());
        suggestion.setConfidence(recommendation.getConfidence());
        suggestion.setReasoning("AI Recommendation: " + recommendation.getReasoning());
        suggestion.setStatus(SuggestionStatus.PENDING);
        suggestion.setTriggerReason(triggerReason);
        
        logger.info("Created reorder suggestion from AI recommendation: {}", suggestion);
        return suggestion;
    }
    
    private ReorderSuggestion createFallbackSuggestion(Product product, String reason) {
        logger.info("Creating fallback reorder suggestion for product: {} due to: {}", product.getSku(), reason);
        ReorderSuggestion fallbackSuggestion = fallbackStrategy.generateReorderSuggestion(product);
        fallbackSuggestion.setReasoning("AI Fallback (" + reason + "): " + fallbackSuggestion.getReasoning());
        return fallbackSuggestion;
    }
}