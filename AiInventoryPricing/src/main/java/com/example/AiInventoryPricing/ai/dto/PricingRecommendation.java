package com.example.AiInventoryPricing.ai.dto;

import com.example.AiInventoryPricing.enums.ChangeDirection;

import java.math.BigDecimal;

public class PricingRecommendation {
    private BigDecimal recommendedPrice;
    private ChangeDirection changeDirection;
    private BigDecimal confidence;
    private String reasoning;

    // Constructors
    public PricingRecommendation() {}

    public PricingRecommendation(BigDecimal recommendedPrice, ChangeDirection changeDirection,
                                BigDecimal confidence, String reasoning) {
        this.recommendedPrice = recommendedPrice;
        this.changeDirection = changeDirection;
        this.confidence = confidence;
        this.reasoning = reasoning;
    }

    // Getters and Setters
    public BigDecimal getRecommendedPrice() {
        return recommendedPrice;
    }

    public void setRecommendedPrice(BigDecimal recommendedPrice) {
        this.recommendedPrice = recommendedPrice;
    }

    public ChangeDirection getChangeDirection() {
        return changeDirection;
    }

    public void setChangeDirection(ChangeDirection changeDirection) {
        this.changeDirection = changeDirection;
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
        return "PricingRecommendation{" +
                "recommendedPrice=" + recommendedPrice +
                ", changeDirection=" + changeDirection +
                ", confidence=" + confidence +
                ", reasoning='" + reasoning + '\'' +
                '}';
    }
}