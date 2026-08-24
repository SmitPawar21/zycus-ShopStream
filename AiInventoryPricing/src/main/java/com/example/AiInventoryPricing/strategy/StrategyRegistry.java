package com.example.AiInventoryPricing.strategy;

import com.example.AiInventoryPricing.strategy.pricing.PricingStrategy;
import com.example.AiInventoryPricing.strategy.reorder.ReorderStrategy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for managing available strategies
 */
@Component
public class StrategyRegistry {
    
    private final Map<String, PricingStrategy> pricingStrategies = new HashMap<>();
    private final Map<String, ReorderStrategy> reorderStrategies = new HashMap<>();
    private String activePricingStrategyName;
    private String activeReorderStrategyName;
    
    /**
     * Register a pricing strategy
     * @param name Strategy name
     * @param strategy Strategy implementation
     */
    public void registerPricingStrategy(String name, PricingStrategy strategy) {
        pricingStrategies.put(name, strategy);
        if (activePricingStrategyName == null) {
            activePricingStrategyName = name;
        }
    }
    
    /**
     * Register a reorder strategy
     * @param name Strategy name
     * @param strategy Strategy implementation
     */
    public void registerReorderStrategy(String name, ReorderStrategy strategy) {
        reorderStrategies.put(name, strategy);
        if (activeReorderStrategyName == null) {
            activeReorderStrategyName = name;
        }
    }
    
    /**
     * Get the currently active pricing strategy
     * @return Active pricing strategy
     */
    public Optional<PricingStrategy> getActivePricingStrategy() {
        if (activePricingStrategyName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(pricingStrategies.get(activePricingStrategyName));
    }
    
    /**
     * Get the currently active reorder strategy
     * @return Active reorder strategy
     */
    public Optional<ReorderStrategy> getActiveReorderStrategy() {
        if (activeReorderStrategyName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(reorderStrategies.get(activeReorderStrategyName));
    }
    
    /**
     * Set the active pricing strategy
     * @param name Strategy name
     * @return true if strategy exists and was set, false otherwise
     */
    public boolean setActivePricingStrategy(String name) {
        if (pricingStrategies.containsKey(name)) {
            this.activePricingStrategyName = name;
            return true;
        }
        return false;
    }
    
    /**
     * Set the active reorder strategy
     * @param name Strategy name
     * @return true if strategy exists and was set, false otherwise
     */
    public boolean setActiveReorderStrategy(String name) {
        if (reorderStrategies.containsKey(name)) {
            this.activeReorderStrategyName = name;
            return true;
        }
        return false;
    }
    
    /**
     * Get all registered pricing strategy names
     * @return Array of pricing strategy names
     */
    public String[] getPricingStrategyNames() {
        return pricingStrategies.keySet().toArray(new String[0]);
    }
    
    /**
     * Get all registered reorder strategy names
     * @return Array of reorder strategy names
     */
    public String[] getReorderStrategyNames() {
        return reorderStrategies.keySet().toArray(new String[0]);
    }
}