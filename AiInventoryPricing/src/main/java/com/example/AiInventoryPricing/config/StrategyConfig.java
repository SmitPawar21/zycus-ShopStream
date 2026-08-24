package com.example.AiInventoryPricing.config;

import com.example.AiInventoryPricing.strategy.StrategyRegistry;
import com.example.AiInventoryPricing.strategy.pricing.PricingStrategy;
import com.example.AiInventoryPricing.strategy.pricing.impl.AiPricingStrategy;
import com.example.AiInventoryPricing.strategy.pricing.impl.RuleBasedPricingStrategy;
import com.example.AiInventoryPricing.strategy.reorder.ReorderStrategy;
import com.example.AiInventoryPricing.strategy.reorder.impl.AiReorderStrategy;
import com.example.AiInventoryPricing.strategy.reorder.impl.RuleBasedReorderStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class StrategyConfig {
    
    @Autowired
    private StrategyRegistry strategyRegistry;
    
    @Autowired
    private AiPricingStrategy aiPricingStrategy;
    
    @Autowired
    private RuleBasedPricingStrategy ruleBasedPricingStrategy;
    
    @Autowired
    private AiReorderStrategy aiReorderStrategy;
    
    @Autowired
    private RuleBasedReorderStrategy ruleBasedReorderStrategy;
    
    @PostConstruct
    public void initStrategies() {
        // Register pricing strategies
        strategyRegistry.registerPricingStrategy(ruleBasedPricingStrategy.getName(), ruleBasedPricingStrategy);
        strategyRegistry.registerPricingStrategy(aiPricingStrategy.getName(), aiPricingStrategy);
        
        // Register reorder strategies
        strategyRegistry.registerReorderStrategy(ruleBasedReorderStrategy.getName(), ruleBasedReorderStrategy);
        strategyRegistry.registerReorderStrategy(aiReorderStrategy.getName(), aiReorderStrategy);
        
        // Set default strategies to rule-based for safety
        strategyRegistry.setActivePricingStrategy(ruleBasedPricingStrategy.getName());
        strategyRegistry.setActiveReorderStrategy(ruleBasedReorderStrategy.getName());
    }
}