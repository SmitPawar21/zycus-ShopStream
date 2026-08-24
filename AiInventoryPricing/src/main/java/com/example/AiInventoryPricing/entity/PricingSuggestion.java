package com.example.AiInventoryPricing.entity;

import com.example.AiInventoryPricing.enums.ChangeDirection;
import com.example.AiInventoryPricing.enums.SuggestionStatus;
import com.example.AiInventoryPricing.enums.TriggerReason;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "pricing_suggestions")
public class PricingSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @DecimalMin(value = "0.0", inclusive = false, message = "Current price must be greater than zero")
    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Recommended price cannot be negative")
    @Column(name = "recommended_price", precision = 10, scale = 2)
    private BigDecimal recommendedPrice;

    @Enumerated(EnumType.STRING)
    private ChangeDirection changeDirection;

    @Column(name = "confidence", precision = 3, scale = 2)
    private BigDecimal confidence; // 0.00 - 1.00

    @Column(name = "reasoning", length = 1000)
    private String reasoning;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    @Column(name = "status")
    private SuggestionStatus status = SuggestionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Trigger reason is required")
    @Column(name = "trigger_reason")
    private TriggerReason triggerReason;

    // Constructors
    public PricingSuggestion() {}

    public PricingSuggestion(Product product, BigDecimal currentPrice, BigDecimal recommendedPrice,
                             ChangeDirection changeDirection, BigDecimal confidence, String reasoning,
                             TriggerReason triggerReason) {
        this.product = product;
        this.currentPrice = currentPrice;
        this.recommendedPrice = recommendedPrice;
        this.changeDirection = changeDirection;
        this.confidence = confidence;
        this.reasoning = reasoning;
        this.triggerReason = triggerReason;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

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

    public SuggestionStatus getStatus() {
        return status;
    }

    public void setStatus(SuggestionStatus status) {
        this.status = status;
    }

    public TriggerReason getTriggerReason() {
        return triggerReason;
    }

    public void setTriggerReason(TriggerReason triggerReason) {
        this.triggerReason = triggerReason;
    }
}