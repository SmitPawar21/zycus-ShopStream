package com.example.AiInventoryPricing.event;

import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.enums.TriggerReason;

public class ProductEvent {
    private final Product product;
    private final TriggerReason triggerReason;
    
    public ProductEvent(Product product, TriggerReason triggerReason) {
        this.product = product;
        this.triggerReason = triggerReason;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public TriggerReason getTriggerReason() {
        return triggerReason;
    }
}