package com.example.AiInventoryPricing.ai.dto;

import com.example.AiInventoryPricing.enums.Category;
import com.example.AiInventoryPricing.enums.LifecycleStatus;

import java.math.BigDecimal;

public class CommerceContext {
    private Long productId;
    private String sku;
    private String productName;
    private Category category;
    private BigDecimal currentPrice;
    private Integer stockLevel;
    private Integer reorderThreshold;
    private Integer demandVelocity;
    private LifecycleStatus lifecycleStatus;
    private BigDecimal costPrice;

    // Constructors
    public CommerceContext() {}

    public CommerceContext(Long productId, String sku, String productName, Category category,
                          BigDecimal currentPrice, Integer stockLevel, Integer reorderThreshold,
                          Integer demandVelocity, LifecycleStatus lifecycleStatus, BigDecimal costPrice) {
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.category = category;
        this.currentPrice = currentPrice;
        this.stockLevel = stockLevel;
        this.reorderThreshold = reorderThreshold;
        this.demandVelocity = demandVelocity;
        this.lifecycleStatus = lifecycleStatus;
        this.costPrice = costPrice;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    @Override
    public String toString() {
        return "CommerceContext{" +
                "productId=" + productId +
                ", sku='" + sku + '\'' +
                ", productName='" + productName + '\'' +
                ", category=" + category +
                ", currentPrice=" + currentPrice +
                ", stockLevel=" + stockLevel +
                ", reorderThreshold=" + reorderThreshold +
                ", demandVelocity=" + demandVelocity +
                ", lifecycleStatus=" + lifecycleStatus +
                ", costPrice=" + costPrice +
                '}';
    }
}