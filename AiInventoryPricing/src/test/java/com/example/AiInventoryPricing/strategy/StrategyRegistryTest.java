package com.example.AiInventoryPricing.strategy;

import com.example.AiInventoryPricing.strategy.pricing.PricingStrategy;
import com.example.AiInventoryPricing.strategy.pricing.impl.RuleBasedPricingStrategy;
import com.example.AiInventoryPricing.strategy.reorder.ReorderStrategy;
import com.example.AiInventoryPricing.strategy.reorder.impl.RuleBasedReorderStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StrategyRegistryTest {

    private StrategyRegistry registry;
    private PricingStrategy pricingStrategy;
    private ReorderStrategy reorderStrategy;

    @BeforeEach
    void setUp() {
        registry = new StrategyRegistry();
        pricingStrategy = new RuleBasedPricingStrategy();
        reorderStrategy = new RuleBasedReorderStrategy();
    }

    @Test
    void testRegisterAndGetPricingStrategy() {
        registry.registerPricingStrategy("test_pricing", pricingStrategy);
        
        String[] names = registry.getPricingStrategyNames();
        assertEquals(1, names.length);
        assertEquals("test_pricing", names[0]);
        
        Optional<PricingStrategy> retrieved = registry.getActivePricingStrategy();
        assertTrue(retrieved.isPresent());
        assertEquals(pricingStrategy, retrieved.get());
    }

    @Test
    void testRegisterAndGetReorderStrategy() {
        registry.registerReorderStrategy("test_reorder", reorderStrategy);
        
        String[] names = registry.getReorderStrategyNames();
        assertEquals(1, names.length);
        assertEquals("test_reorder", names[0]);
        
        Optional<ReorderStrategy> retrieved = registry.getActiveReorderStrategy();
        assertTrue(retrieved.isPresent());
        assertEquals(reorderStrategy, retrieved.get());
    }

    @Test
    void testSetActiveStrategy_Success() {
        registry.registerPricingStrategy("strategy1", pricingStrategy);
        
        // Register another strategy
        PricingStrategy anotherStrategy = new RuleBasedPricingStrategy();
        registry.registerPricingStrategy("strategy2", anotherStrategy);
        
        // Set active strategy
        boolean result = registry.setActivePricingStrategy("strategy2");
        assertTrue(result);
        
        Optional<PricingStrategy> active = registry.getActivePricingStrategy();
        assertTrue(active.isPresent());
        assertEquals(anotherStrategy, active.get());
    }

    @Test
    void testSetActiveStrategy_NotFound() {
        registry.registerPricingStrategy("strategy1", pricingStrategy);
        
        // Try to set non-existent strategy
        boolean result = registry.setActivePricingStrategy("nonexistent");
        assertFalse(result);
        
        // Active strategy should remain the default
        Optional<PricingStrategy> active = registry.getActivePricingStrategy();
        assertTrue(active.isPresent());
        assertEquals(pricingStrategy, active.get());
    }

    @Test
    void testGetActiveStrategy_NoneRegistered() {
        Optional<PricingStrategy> active = registry.getActivePricingStrategy();
        assertFalse(active.isPresent());
    }
}