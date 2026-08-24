package com.example.AiInventoryPricing.service.impl;

import com.example.AiInventoryPricing.dto.*;
import com.example.AiInventoryPricing.enums.Category;
import com.example.AiInventoryPricing.enums.LifecycleStatus;
import com.example.AiInventoryPricing.enums.SuggestionStatus;
import com.example.AiInventoryPricing.enums.TriggerReason;
import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.entity.PricingSuggestion;
import com.example.AiInventoryPricing.entity.ReorderSuggestion;
import com.example.AiInventoryPricing.repository.ProductRepository;
import com.example.AiInventoryPricing.repository.PricingSuggestionRepository;
import com.example.AiInventoryPricing.repository.ReorderSuggestionRepository;
import com.example.AiInventoryPricing.service.ProductService;
import com.example.AiInventoryPricing.exception.ResourceNotFoundException;
import com.example.AiInventoryPricing.event.ProductEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private PricingSuggestionRepository pricingSuggestionRepository;
    
    @Autowired
    private ReorderSuggestionRepository reorderSuggestionRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public ProductDto createProduct(CreateProductRequestDto requestDto) {
        Product product = new Product(
            requestDto.getSku(),
            requestDto.getName(),
            requestDto.getCategory(),
            requestDto.getCurrentPrice(),
            requestDto.getStockLevel(),
            requestDto.getReorderThreshold()
        );
        product.setCostPrice(requestDto.getCostPrice());
        
        Product savedProduct = productRepository.save(product);
        
        // Check for triggers on newly created product
        checkTriggersAndPublishEvents(savedProduct);
        
        return convertToDto(savedProduct);
    }
    
    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductDto> getProductsByStatus(LifecycleStatus status) {
        return productRepository.findByLifecycleStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductDto> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductDto> getProductsByStatusAndCategory(LifecycleStatus status, Category category) {
        return productRepository.findByLifecycleStatusAndCategory(status, category).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertToDto(product);
    }
    
    @Override
    @Transactional
    public ProductDto updateStockLevel(Long id, UpdateStockRequestDto requestDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        product.setStockLevel(requestDto.getStockLevel());
        Product updatedProduct = productRepository.save(product);
        
        // Check for triggers after updating stock level
        checkTriggersAndPublishEvents(updatedProduct);
        
        return convertToDto(updatedProduct);
    }
    
    @Override
    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }
    
    @Override
    @Transactional
    public void incrementDemandVelocity(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        product.incrementDemandVelocity();
        
        // Also decrement stock by 1 to simulate an actual sale
        if (product.getStockLevel() > 0) {
            product.setStockLevel(product.getStockLevel() - 1);
        }
        
        Product updatedProduct = productRepository.save(product);
        
        // Check for triggers after the sale
        checkTriggersAndPublishEvents(updatedProduct);
    }
    
    @Override
    public Double getCategoryAverageDemandVelocity(Category category) {
        return productRepository.getAverageDemandVelocityByCategory(category);
    }
    
    @Override
    public void checkTriggersAndPublishEvents(Product product) {
        // Check for INVENTORY_LOW trigger
        if (product.getStockLevel() <= product.getReorderThreshold()) {
            eventPublisher.publishEvent(new ProductEvent(product, TriggerReason.INVENTORY_LOW));
        }
        
        // Check for DEMAND_SPIKE trigger
        Double categoryAvgDemand = getCategoryAverageDemandVelocity(product.getCategory());
        if (categoryAvgDemand != null && product.getDemandVelocity() > categoryAvgDemand * 1.5) {
            eventPublisher.publishEvent(new ProductEvent(product, TriggerReason.DEMAND_SPIKE));
        }
    }
    
    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setCurrentPrice(product.getCurrentPrice());
        dto.setStockLevel(product.getStockLevel());
        dto.setReorderThreshold(product.getReorderThreshold());
        dto.setDemandVelocity(product.getDemandVelocity());
        dto.setLifecycleStatus(product.getLifecycleStatus());
        dto.setCostPrice(product.getCostPrice());
        return dto;
    }
}