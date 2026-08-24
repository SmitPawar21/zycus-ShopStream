package com.example.AiInventoryPricing.dto;

import com.example.AiInventoryPricing.enums.SuggestionStatus;
import jakarta.validation.constraints.NotNull;

public class AcceptSuggestionRequestDto {
    @NotNull(message = "Status is required")
    private SuggestionStatus status;

    // Constructors
    public AcceptSuggestionRequestDto() {}

    // Getters and Setters
    public SuggestionStatus getStatus() {
        return status;
    }

    public void setStatus(SuggestionStatus status) {
        this.status = status;
    }
}