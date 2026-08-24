package com.example.AiInventoryPricing.dto;

import com.example.AiInventoryPricing.enums.TriggerReason;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreatePricingSuggestionRequestDto {
    
    @NotNull(message = "Proposed price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Proposed price must be greater than zero")
    private BigDecimal proposedPrice;
    
    @NotNull(message = "Reasoning is required")
    private String reasoning;
    
    @DecimalMin(value = "0.00", message = "Confidence cannot be negative")
    @DecimalMax(value = "1.00", message = "Confidence cannot be greater than 1.00")
    private BigDecimal confidence;
    
    @NotNull(message = "Trigger reason is required")
    private TriggerReason triggerReason;

    // Constructors
    public CreatePricingSuggestionRequestDto() {}

    // Getters and Setters
    public BigDecimal getProposedPrice() {
        return proposedPrice;
    }

    public void setProposedPrice(BigDecimal proposedPrice) {
        this.proposedPrice = proposedPrice;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public TriggerReason getTriggerReason() {
        return triggerReason;
    }

    public void setTriggerReason(TriggerReason triggerReason) {
        this.triggerReason = triggerReason;
    }
}