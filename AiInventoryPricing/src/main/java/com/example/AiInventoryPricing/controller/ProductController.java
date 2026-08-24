package com.example.AiInventoryPricing.controller;

import com.example.AiInventoryPricing.dto.*;
import com.example.AiInventoryPricing.enums.Category;
import com.example.AiInventoryPricing.enums.LifecycleStatus;
import com.example.AiInventoryPricing.service.ProductService;
import com.example.AiInventoryPricing.service.SuggestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private SuggestionService suggestionService;
    
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductRequestDto requestDto) {
        ProductDto productDto = productService.createProduct(requestDto);
        return new ResponseEntity<>(productDto, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<ProductDto>> getProducts(
            @RequestParam(required = false) LifecycleStatus status,
            @RequestParam(required = false) Category category) {
        
        List<ProductDto> products;
        
        if (status != null && category != null) {
            products = productService.getProductsByStatusAndCategory(status, category);
        } else if (status != null) {
            products = productService.getProductsByStatus(status);
        } else if (category != null) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllProducts();
        }
        
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        ProductDto productDto = productService.getProductById(id);
        return ResponseEntity.ok(productDto);
    }
    
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductDto> updateStockLevel(@PathVariable Long id, 
                                                       @Valid @RequestBody UpdateStockRequestDto requestDto) {
        ProductDto productDto = productService.updateStockLevel(id, requestDto);
        return ResponseEntity.ok(productDto);
    }
    
    @PostMapping("/{id}/orders")
    public ResponseEntity<ProductDto> simulateSale(@PathVariable Long id) {
        // For now, just increment demand velocity
        productService.incrementDemandVelocity(id);
        ProductDto productDto = productService.getProductById(id);
        return ResponseEntity.ok(productDto);
    }
    
    @PostMapping("/{id}/suggest-pricing")
    public ResponseEntity<PricingSuggestionDto> createPricingSuggestion(
            @PathVariable Long id, 
            @Valid @RequestBody CreatePricingSuggestionRequestDto requestDto) {
        PricingSuggestionDto suggestionDto = suggestionService.generatePricingSuggestionForProduct(id, requestDto);
        return new ResponseEntity<>(suggestionDto, HttpStatus.CREATED);
    }
    
    @PostMapping("/{id}/suggest-reorder")
    public ResponseEntity<ReorderSuggestionDto> createReorderSuggestion(
            @PathVariable Long id, 
            @Valid @RequestBody CreateReorderSuggestionRequestDto requestDto) {
        ReorderSuggestionDto suggestionDto = suggestionService.generateReorderSuggestionForProduct(id, requestDto);
        return new ResponseEntity<>(suggestionDto, HttpStatus.CREATED);
    }
    
    // New strategy-based endpoints
    @PostMapping("/{id}/suggest-pricing/strategy")
    public ResponseEntity<PricingSuggestionDto> generatePricingSuggestionUsingStrategy(@PathVariable Long id) {
        PricingSuggestionDto suggestionDto = suggestionService.generatePricingSuggestionUsingStrategy(id);
        return new ResponseEntity<>(suggestionDto, HttpStatus.CREATED);
    }
    
    @PostMapping("/{id}/suggest-reorder/strategy")
    public ResponseEntity<ReorderSuggestionDto> generateReorderSuggestionUsingStrategy(@PathVariable Long id) {
        ReorderSuggestionDto suggestionDto = suggestionService.generateReorderSuggestionUsingStrategy(id);
        return new ResponseEntity<>(suggestionDto, HttpStatus.CREATED);
    }
}