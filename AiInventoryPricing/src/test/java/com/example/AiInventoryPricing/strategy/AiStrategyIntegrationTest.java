package com.example.AiInventoryPricing.strategy;

import com.example.AiInventoryPricing.strategy.pricing.PricingStrategy;
import com.example.AiInventoryPricing.strategy.pricing.impl.AiPricingStrategy;
import com.example.AiInventoryPricing.strategy.reorder.ReorderStrategy;
import com.example.AiInventoryPricing.strategy.reorder.impl.AiReorderStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AiStrategyIntegrationTest {

    @Autowired
    private StrategyRegistry strategyRegistry;

    @Test
    public void testAiPricingStrategyRegistered() {
        PricingStrategy aiStrategy = strategyRegistry.getPricingStrategy("AI");
        assertNotNull(aiStrategy);
        assertTrue(aiStrategy instanceof AiPricingStrategy);
        assertEquals("AI", aiStrategy.getName());
    }

    @Test
    public void testAiReorderStrategyRegistered() {
        ReorderStrategy aiStrategy = strategyRegistry.getReorderStrategy("AI");
        assertNotNull(aiStrategy);
        assertTrue(aiStrategy instanceof AiReorderStrategy);
        assertEquals("AI", aiStrategy.getName());
    }

    @Test
    public void testAvailableStrategies() {
        assertTrue(strategyRegistry.getAvailablePricingStrategies().contains("AI"));
        assertTrue(strategyRegistry.getAvailablePricingStrategies().contains("RULE_BASED"));
        assertTrue(strategyRegistry.getAvailableReorderStrategies().contains("AI"));
        assertTrue(strategyRegistry.getAvailableReorderStrategies().contains("RULE_BASED"));
    }
}