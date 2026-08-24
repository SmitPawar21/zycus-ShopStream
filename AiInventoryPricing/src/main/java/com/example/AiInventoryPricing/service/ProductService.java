package com.example.AiInventoryPricing.service;

import com.example.AiInventoryPricing.dto.*;
import com.example.AiInventoryPricing.enums.Category;
import com.example.AiInventoryPricing.enums.LifecycleStatus;
import com.example.AiInventoryPricing.enums.SuggestionStatus;
import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.entity.PricingSuggestion;
import com.example.AiInventoryPricing.entity.ReorderSuggestion;

import java.util.List;

public interface ProductService {
    ProductDto createProduct(CreateProductRequestDto requestDto);
    
    List<ProductDto> getAllProducts();
    
    List<ProductDto> getProductsByStatus(LifecycleStatus status);
    
    List<ProductDto> getProductsByCategory(Category category);
    
    List<ProductDto> getProductsByStatusAndCategory(LifecycleStatus status, Category category);
    
    ProductDto getProductById(Long id);
    
    ProductDto updateStockLevel(Long id, UpdateStockRequestDto requestDto);
    
    Product getProductEntityById(Long id);
    
    void incrementDemandVelocity(Long id);
    
    Double getCategoryAverageDemandVelocity(Category category);
}