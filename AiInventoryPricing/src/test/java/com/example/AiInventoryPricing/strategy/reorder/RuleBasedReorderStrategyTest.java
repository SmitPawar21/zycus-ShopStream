package com.example.AiInventoryPricing.strategy.reorder;

import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.entity.ReorderSuggestion;
import com.example.AiInventoryPricing.enums.Category;
import com.example.AiInventoryPricing.enums.LifecycleStatus;
import com.example.AiInventoryPricing.strategy.reorder.impl.RuleBasedReorderStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RuleBasedReorderStrategyTest {

    private RuleBasedReorderStrategy strategy;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        strategy = new RuleBasedReorderStrategy();
        
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
    void testGenerateReorderSuggestion_NormalStock_NoReorder() {
        ReorderSuggestion suggestion = strategy.generateReorderSuggestion(testProduct);
        
        assertNotNull(suggestion);
        assertEquals(testProduct, suggestion.getProduct());
        assertEquals(testProduct.getStockLevel(), suggestion.getCurrentStock());
        assertEquals(0, suggestion.getRecommendedQuantity()); // No reorder needed
        assertNotNull(suggestion.getReasoning());
        assertTrue(suggestion.getReasoning().contains("Sufficient inventory"));
    }

    @Test
    void testGenerateReorderSuggestion_BelowThreshold_Reorder() {
        testProduct.setStockLevel(5); // Below threshold
        
        ReorderSuggestion suggestion = strategy.generateReorderSuggestion(testProduct);
        
        assertNotNull(suggestion);
        assertEquals(testProduct, suggestion.getProduct());
        assertEquals(testProduct.getStockLevel(), suggestion.getCurrentStock());
        assertTrue(suggestion.getRecommendedQuantity() > 0); // Should recommend reorder
        assertEquals(14, suggestion.getSuggestedLeadTimeDays()); // Electronics lead time
        assertEquals(new BigDecimal("0.75"), suggestion.getConfidence()); // Medium confidence
        assertTrue(suggestion.getReasoning().contains("Stock below threshold"));
    }

    @Test
    void testGenerateReorderSuggestion_NearThreshold_SmallReorder() {
        testProduct.setStockLevel(12); // Near threshold (10)
        
        ReorderSuggestion suggestion = strategy.generateReorderSuggestion(testProduct);
        
        assertNotNull(suggestion);
        assertEquals(testProduct, suggestion.getProduct());
        assertEquals(testProduct.getStockLevel(), suggestion.getCurrentStock());
        assertTrue(suggestion.getRecommendedQuantity() > 0); // Should recommend small reorder
        assertEquals(14, suggestion.getSuggestedLeadTimeDays()); // Electronics lead time
        assertEquals(new BigDecimal("0.75"), suggestion.getConfidence()); // Medium confidence
        assertTrue(suggestion.getReasoning().contains("Approaching reorder threshold"));
    }

    @Test
    void testGenerateReorderSuggestion_CriticalLow_HighConfidence() {
        testProduct.setStockLevel(2); // Critically low (below 30% of threshold)
        
        ReorderSuggestion suggestion = strategy.generateReorderSuggestion(testProduct);
        
        assertNotNull(suggestion);
        assertEquals(testProduct, suggestion.getProduct());
        assertEquals(testProduct.getStockLevel(), suggestion.getCurrentStock());
        assertTrue(suggestion.getRecommendedQuantity() > 0); // Should recommend reorder
        assertEquals(new BigDecimal("0.90"), suggestion.getConfidence()); // High confidence
        assertTrue(suggestion.getReasoning().contains("Stock below threshold"));
    }

    @Test
    void testDifferentCategories_HaveDifferentLeadTimes() {
        // Test electronics
        testProduct.setCategory(Category.ELECTRONICS);
        testProduct.setStockLevel(5);
        ReorderSuggestion electronicsSuggestion = strategy.generateReorderSuggestion(testProduct);
        assertEquals(14, electronicsSuggestion.getSuggestedLeadTimeDays());
        
        // Test apparel
        testProduct.setCategory(Category.APPAREL);
        ReorderSuggestion apparelSuggestion = strategy.generateReorderSuggestion(testProduct);
        assertEquals(10, apparelSuggestion.getSuggestedLeadTimeDays());
        
        // Test home
        testProduct.setCategory(Category.HOME);
        ReorderSuggestion homeSuggestion = strategy.generateReorderSuggestion(testProduct);
        assertEquals(7, homeSuggestion.getSuggestedLeadTimeDays());
    }
}