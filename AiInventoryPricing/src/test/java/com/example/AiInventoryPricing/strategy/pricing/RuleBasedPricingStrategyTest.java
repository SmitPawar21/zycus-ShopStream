package com.example.AiInventoryPricing.strategy.pricing;

import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.entity.PricingSuggestion;
import com.example.AiInventoryPricing.enums.Category;
import com.example.AiInventoryPricing.enums.LifecycleStatus;
import com.example.AiInventoryPricing.strategy.pricing.impl.RuleBasedPricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RuleBasedPricingStrategyTest {

    private RuleBasedPricingStrategy strategy;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        strategy = new RuleBasedPricingStrategy();
        
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setSku("TEST-001");
        testProduct.setName("Test Product");
        testProduct.setCategory(Category.ELECTRONICS);
        testProduct.setCurrentPrice(new BigDecimal("100.00"));
        testProduct.setStockLevel(50);
        testProduct.setReorderThreshold(10);
        testProduct.setDemandVelocity(5);
        testProduct.setLifecycleStatus(LifecycleStatus.ACTIVE);
    }

    @Test
    void testGetName() {
        assertEquals("RULE_BASED", strategy.getName());
    }

    @Test
    void testGetConfidence() {
        assertEquals(new BigDecimal("0.75"), strategy.getConfidence());
    }

    @Test
    void testGeneratePricingSuggestion_NormalConditions() {
        PricingSuggestion suggestion = strategy.generatePricingSuggestion(testProduct);
        
        assertNotNull(suggestion);
        assertEquals(testProduct, suggestion.getProduct());
        assertEquals(testProduct.getCurrentPrice(), suggestion.getCurrentPrice());
        assertEquals(testProduct.getCurrentPrice(), suggestion.getRecommendedPrice()); // No change expected
        assertNotNull(suggestion.getReasoning());
        assertTrue(suggestion.getReasoning().contains("Normal conditions"));
    }

    @Test
    void testGeneratePricingSuggestion_LowStock_IncreasesPrice() {
        testProduct.setStockLevel(3); // Very low stock (below 50% of reorder threshold)
        
        PricingSuggestion suggestion = strategy.generatePricingSuggestion(testProduct);
        
        assertNotNull(suggestion);
        assertEquals(testProduct.getCurrentPrice(), suggestion.getCurrentPrice());
        assertTrue(suggestion.getRecommendedPrice().compareTo(testProduct.getCurrentPrice()) > 0); // Price should increase
        assertEquals(new BigDecimal("115.00"), suggestion.getRecommendedPrice()); // 15% increase
        assertEquals(new BigDecimal("0.90"), suggestion.getConfidence()); // High confidence
        assertTrue(suggestion.getReasoning().contains("Stock critically low"));
    }

    @Test
    void testGeneratePricingSuggestion_ExcessStock_DecreasesPrice() {
        testProduct.setStockLevel(40); // Excess stock (more than 3x reorder threshold)
        testProduct.setDemandVelocity(2); // Low demand
        
        PricingSuggestion suggestion = strategy.generatePricingSuggestion(testProduct);
        
        assertNotNull(suggestion);
        assertEquals(testProduct.getCurrentPrice(), suggestion.getCurrentPrice());
        assertTrue(suggestion.getRecommendedPrice().compareTo(testProduct.getCurrentPrice()) < 0); // Price should decrease
        assertEquals(new BigDecimal("85.00"), suggestion.getRecommendedPrice()); // 15% decrease
        assertEquals(new BigDecimal("0.90"), suggestion.getConfidence()); // High confidence
        assertTrue(suggestion.getReasoning().contains("Excess stock"));
    }

    @Test
    void testGeneratePricingSuggestion_HighDemand_IncreasesPrice() {
        testProduct.setDemandVelocity(12); // High demand
        
        PricingSuggestion suggestion = strategy.generatePricingSuggestion(testProduct);
        
        assertNotNull(suggestion);
        assertEquals(testProduct.getCurrentPrice(), suggestion.getCurrentPrice());
        assertTrue(suggestion.getRecommendedPrice().compareTo(testProduct.getCurrentPrice()) > 0); // Price should increase
        assertEquals(new BigDecimal("105.00"), suggestion.getRecommendedPrice()); // 5% increase
        assertEquals(new BigDecimal("0.75"), suggestion.getConfidence()); // Medium confidence
        assertTrue(suggestion.getReasoning().contains("High demand velocity"));
    }

    @Test
    void testGeneratePricingSuggestion_LowDemand_DecreasesPrice() {
        testProduct.setStockLevel(15); // Moderate stock, not excessive
        testProduct.setDemandVelocity(1); // Very low demand
        
        PricingSuggestion suggestion = strategy.generatePricingSuggestion(testProduct);
        
        assertNotNull(suggestion);
        assertEquals(testProduct.getCurrentPrice(), suggestion.getCurrentPrice());
        assertTrue(suggestion.getRecommendedPrice().compareTo(testProduct.getCurrentPrice()) < 0); // Price should decrease
        assertEquals(new BigDecimal("95.00"), suggestion.getRecommendedPrice()); // 5% decrease
        assertEquals(new BigDecimal("0.75"), suggestion.getConfidence()); // Medium confidence
        assertTrue(suggestion.getReasoning().contains("Low demand velocity"));
    }
}