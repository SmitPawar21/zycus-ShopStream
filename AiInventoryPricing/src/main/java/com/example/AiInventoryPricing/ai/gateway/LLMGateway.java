package com.example.AiInventoryPricing.ai.gateway;

import com.example.AiInventoryPricing.ai.dto.CommerceContext;
import com.example.AiInventoryPricing.enums.TriggerReason;

public interface LLMGateway {
    String generatePricingRecommendation(CommerceContext context, TriggerReason triggerReason);
    String generateReorderRecommendation(CommerceContext context, TriggerReason triggerReason);
}