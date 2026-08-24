package com.example.AiInventoryPricing.config;

import com.example.AiInventoryPricing.strategy.StrategyRegistry;
import com.example.AiInventoryPricing.strategy.pricing.PricingStrategy;
import com.example.AiInventoryPricing.strategy.pricing.impl.RuleBasedPricingStrategy;
import com.example.AiInventoryPricing.strategy.reorder.ReorderStrategy;
import com.example.AiInventoryPricing.strategy.reorder.impl.RuleBasedReorderStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class StrategyConfig {
    
    @Autowired
    private StrategyRegistry strategyRegistry;
    
    @PostConstruct
    public void initStrategies() {
        // Register pricing strategies
        PricingStrategy ruleBasedPricingStrategy = new RuleBasedPricingStrategy();
        strategyRegistry.registerPricingStrategy(ruleBasedPricingStrategy.getName(), ruleBasedPricingStrategy);
        
        // Register reorder strategies
        ReorderStrategy ruleBasedReorderStrategy = new RuleBasedReorderStrategy();
        strategyRegistry.registerReorderStrategy(ruleBasedReorderStrategy.getName(), ruleBasedReorderStrategy);
    }
}