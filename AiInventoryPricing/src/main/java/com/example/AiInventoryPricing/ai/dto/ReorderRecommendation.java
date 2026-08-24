package com.example.AiInventoryPricing.ai.dto;

import java.math.BigDecimal;

public class ReorderRecommendation {
    private Integer recommendedQuantity;
    private Integer suggestedLeadTimeDays;
    private BigDecimal confidence;
    private String reasoning;

    // Constructors
    public ReorderRecommendation() {}

    public ReorderRecommendation(Integer recommendedQuantity, Integer suggestedLeadTimeDays,
                                BigDecimal confidence, String reasoning) {
        this.recommendedQuantity = recommendedQuantity;
        this.suggestedLeadTimeDays = suggestedLeadTimeDays;
        this.confidence = confidence;
        this.reasoning = reasoning;
    }

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

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    @Override
    public String toString() {
        return "ReorderRecommendation{" +
                "recommendedQuantity=" + recommendedQuantity +
                ", suggestedLeadTimeDays=" + suggestedLeadTimeDays +
                ", confidence=" + confidence +
                ", reasoning='" + reasoning + '\'' +
                '}';
    }
}