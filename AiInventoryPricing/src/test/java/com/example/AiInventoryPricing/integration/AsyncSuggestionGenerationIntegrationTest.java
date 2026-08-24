package com.example.AiInventoryPricing.integration;

import com.example.AiInventoryPricing.AiInventoryPricingApplication;
import com.example.AiInventoryPricing.dto.CreateProductRequestDto;
import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.enums.Category;
import com.example.AiInventoryPricing.enums.SuggestionStatus;
import com.example.AiInventoryPricing.enums.TriggerReason;
import com.example.AiInventoryPricing.repository.ProductRepository;
import com.example.AiInventoryPricing.repository.PricingSuggestionRepository;
import com.example.AiInventoryPricing.repository.ReorderSuggestionRepository;
import com.example.AiInventoryPricing.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AiInventoryPricingApplication.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class AsyncSuggestionGenerationIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PricingSuggestionRepository pricingSuggestionRepository;

    @Autowired
    private ReorderSuggestionRepository reorderSuggestionRepository;

    @Test
    public void testInventoryLowTriggerGeneratesSuggestions() {
        // Create a product with low stock (below reorder threshold)
        CreateProductRequestDto requestDto = new CreateProductRequestDto();
        requestDto.setSku("LOW-STOCK-001");
        requestDto.setName("Low Stock Product");
        requestDto.setCategory(Category.ELECTRONICS);
        requestDto.setCurrentPrice(new BigDecimal("100.00"));
        requestDto.setCostPrice(new BigDecimal("50.00"));
        requestDto.setStockLevel(5); // Low stock
        requestDto.setReorderThreshold(10); // Higher threshold

        // This should trigger INVENTORY_LOW event
        var productDto = productService.createProduct(requestDto);
        
        // Wait for async processing
        await().atMost(5, TimeUnit.SECONDS).until(() -> 
            !pricingSuggestionRepository.findPendingByProductIdAndTriggerReason(
                productDto.getId(), TriggerReason.INVENTORY_LOW).isEmpty() &&
            !reorderSuggestionRepository.findPendingByProductIdAndTriggerReason(
                productDto.getId(), TriggerReason.INVENTORY_LOW).isEmpty()
        );

        // Verify pricing suggestions were created
        List pricingSuggestions = pricingSuggestionRepository.findByProductIdAndStatus(
            productDto.getId(), SuggestionStatus.PENDING);
        assertFalse(pricingSuggestions.isEmpty());
        assertEquals(TriggerReason.INVENTORY_LOW, ((com.example.AiInventoryPricing.entity.PricingSuggestion) pricingSuggestions.get(0)).getTriggerReason());

        // Verify reorder suggestions were created
        List reorderSuggestions = reorderSuggestionRepository.findByProductIdAndStatus(
            productDto.getId(), SuggestionStatus.PENDING);
        assertFalse(reorderSuggestions.isEmpty());
        assertEquals(TriggerReason.INVENTORY_LOW, ((com.example.AiInventoryPricing.entity.ReorderSuggestion) reorderSuggestions.get(0)).getTriggerReason());
    }
}