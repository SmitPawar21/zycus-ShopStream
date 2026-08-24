package com.example.AiInventoryPricing.entity;

import com.example.AiInventoryPricing.enums.Category;
import com.example.AiInventoryPricing.enums.LifecycleStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "SKU is required")
    @Column(unique = true)
    private String sku;

    @NotBlank(message = "Name is required")
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Category is required")
    private Category category;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Min(value = 0, message = "Stock level cannot be negative")
    @Column(name = "stock_level")
    private Integer stockLevel = 0;

    @Min(value = 0, message = "Reorder threshold cannot be negative")
    @Column(name = "reorder_threshold")
    private Integer reorderThreshold = 0;

    @Min(value = 0, message = "Demand velocity cannot be negative")
    @Column(name = "demand_velocity")
    private Integer demandVelocity = 0;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Lifecycle status is required")
    @Column(name = "lifecycle_status")
    private LifecycleStatus lifecycleStatus = LifecycleStatus.ACTIVE;

    // Extension placeholder for Sprint 2
    @Column(precision = 10, scale = 2)
    private BigDecimal costPrice;

    // Constructors
    public Product() {}

    public Product(String sku, String name, Category category, BigDecimal currentPrice, 
                   Integer stockLevel, Integer reorderThreshold) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.currentPrice = currentPrice;
        this.stockLevel = stockLevel;
        this.reorderThreshold = reorderThreshold;
        this.lifecycleStatus = stockLevel > 0 ? LifecycleStatus.ACTIVE : LifecycleStatus.OUT_OF_STOCK;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Integer getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
        // Update lifecycle status based on stock level
        if (this.stockLevel <= 0) {
            this.lifecycleStatus = LifecycleStatus.OUT_OF_STOCK;
        } else if (this.lifecycleStatus == LifecycleStatus.OUT_OF_STOCK) {
            this.lifecycleStatus = LifecycleStatus.ACTIVE;
        }
    }

    public Integer getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public Integer getDemandVelocity() {
        return demandVelocity;
    }

    public void setDemandVelocity(Integer demandVelocity) {
        this.demandVelocity = demandVelocity;
    }

    public LifecycleStatus getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(LifecycleStatus lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    // Helper methods
    public boolean isBelowReorderThreshold() {
        return this.stockLevel < this.reorderThreshold;
    }

    public void incrementDemandVelocity() {
        this.demandVelocity++;
    }
}