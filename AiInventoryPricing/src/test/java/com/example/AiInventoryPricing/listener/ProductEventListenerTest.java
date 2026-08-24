package com.example.AiInventoryPricing.listener;

import com.example.AiInventoryPricing.AiInventoryPricingApplication;
import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.enums.Category;
import com.example.AiInventoryPricing.enums.LifecycleStatus;
import com.example.AiInventoryPricing.enums.TriggerReason;
import com.example.AiInventoryPricing.event.ProductEvent;
import com.example.AiInventoryPricing.repository.ProductRepository;
import com.example.AiInventoryPricing.service.SuggestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = AiInventoryPricingApplication.class)
public class ProductEventListenerTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private SuggestionService suggestionService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testProductEventHandling() throws InterruptedException {
        // Create a test product with unique SKU
        Product product = new Product();
        product.setSku("TEST-" + UUID.randomUUID().toString().substring(0, 8)); // Unique SKU
        product.setName("Test Product");
        product.setCategory(Category.ELECTRONICS);
        product.setCurrentPrice(new BigDecimal("99.99"));
        product.setStockLevel(5);
        product.setReorderThreshold(10); // This will trigger INVENTORY_LOW
        product.setDemandVelocity(5); // Fix: Use integer value
        product.setLifecycleStatus(LifecycleStatus.ACTIVE);
        product.setCostPrice(new BigDecimal("50.00"));

        Product savedProduct = productRepository.save(product);

        // Publish the event
        eventPublisher.publishEvent(new ProductEvent(savedProduct, TriggerReason.INVENTORY_LOW));

        // Wait a bit for async processing
        Thread.sleep(1000);

        // Verify that the suggestion service methods were called
        verify(suggestionService, times(1)).generatePricingSuggestionAsync(savedProduct.getId(), TriggerReason.INVENTORY_LOW);
        verify(suggestionService, times(1)).generateReorderSuggestionAsync(savedProduct.getId(), TriggerReason.INVENTORY_LOW);
    }
}