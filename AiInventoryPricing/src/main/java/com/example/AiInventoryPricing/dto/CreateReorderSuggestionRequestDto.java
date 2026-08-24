package com.example.AiInventoryPricing.dto;

import com.example.AiInventoryPricing.enums.TriggerReason;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateReorderSuggestionRequestDto {
    
    @NotNull(message = "Recommended quantity is required")
    @Min(value = 1, message = "Recommended quantity must be at least 1")
    private Integer recommendedQuantity;
    
    @Min(value = 1, message = "Lead time must be at least 1 day")
    private Integer suggestedLeadTimeDays;
    
    @NotNull(message = "Reasoning is required")
    private String reasoning;
    
    @DecimalMin(value = "0.00", message = "Confidence cannot be negative")
    @DecimalMax(value = "1.00", message = "Confidence cannot be greater than 1.00")
    private BigDecimal confidence;
    
    @NotNull(message = "Trigger reason is required")
    private TriggerReason triggerReason;

    // Constructors
    public CreateReorderSuggestionRequestDto() {}

    // Getters and Setters
    public Integer getRecommendedQuantity() {
        return recommendedQuantity;
    }

    public void setRecommendedQuantity(Integer recommendedQuantity) {
        this.recommendedQuantity = recommendedQuantity;
    }

    public Integer getSuggestedLeadTimeDays() {
        return suggestedLeadTimeDays;
    }

    public void setSuggestedLeadTimeDays(Integer suggestedLeadTimeDays) {
        this.suggestedLeadTimeDays = suggestedLeadTimeDays;
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