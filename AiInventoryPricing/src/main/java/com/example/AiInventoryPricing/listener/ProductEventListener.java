package com.example.AiInventoryPricing.listener;

import com.example.AiInventoryPricing.event.ProductEvent;
import com.example.AiInventoryPricing.service.SuggestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ProductEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductEventListener.class);
    
    @Autowired
    private SuggestionService suggestionService;
    
    @Async
    @EventListener
    public void handleProductEvent(ProductEvent event) {
        try {
            logger.info("Processing async suggestion generation for product ID: {} with trigger: {}", 
                       event.getProduct().getId(), event.getTriggerReason());
            
            // Generate both pricing and reorder suggestions asynchronously
            // These methods include duplicate prevention logic
            suggestionService.generatePricingSuggestionAsync(event.getProduct().getId(), event.getTriggerReason());
            suggestionService.generateReorderSuggestionAsync(event.getProduct().getId(), event.getTriggerReason());
            
            logger.info("Completed async suggestion generation for product ID: {}", event.getProduct().getId());
        } catch (Exception e) {
            logger.error("Error during async suggestion generation for product ID: {}", 
                        event.getProduct().getId(), e);
            // The error is logged but doesn't affect the main API response
        }
    }
}