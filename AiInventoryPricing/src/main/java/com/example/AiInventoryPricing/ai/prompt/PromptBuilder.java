package com.example.AiInventoryPricing.ai.prompt;

import com.example.AiInventoryPricing.ai.dto.CommerceContext;
import com.example.AiInventoryPricing.enums.TriggerReason;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildPricingPrompt(CommerceContext context, TriggerReason triggerReason) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("As an AI commerce expert, analyze the following product data and recommend optimal pricing:\n\n");
        prompt.append(buildProductContext(context));
        
        switch (triggerReason) {
            case INVENTORY_LOW:
                prompt.append("\nSpecial Consideration: Inventory is low. Consider reducing price to increase sales velocity and clear inventory.\n");
                break;
            case DEMAND_SPIKE:
                prompt.append("\nSpecial Consideration: Demand has spiked. Consider increasing price to maximize revenue.\n");
                break;
            default:
                prompt.append("\nProvide balanced pricing recommendations based on current market conditions.\n");
                break;
        }
        
        prompt.append("\nRespond in JSON format with the following structure:\n");
        prompt.append("{\n");
        prompt.append("  \"recommendedPrice\": number,\n");
        prompt.append("  \"changeDirection\": \"INCREASE|DECREASE|HOLD\",\n");
        prompt.append("  \"confidence\": number (0.00-1.00),\n");
        prompt.append("  \"reasoning\": \"explanation of recommendation\"\n");
        prompt.append("}\n");
        prompt.append("Ensure the response is valid JSON and nothing else.");
        
        return prompt.toString();
    }

    public String buildReorderPrompt(CommerceContext context, TriggerReason triggerReason) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("As an AI supply chain expert, analyze the following product data and recommend optimal reorder quantities:\n\n");
        prompt.append(buildProductContext(context));
        
        switch (triggerReason) {
            case INVENTORY_LOW:
                prompt.append("\nSpecial Consideration: Inventory is critically low. Recommend sufficient reorder quantity to prevent stockout.\n");
                break;
            case DEMAND_SPIKE:
                prompt.append("\nSpecial Consideration: Demand has spiked. Recommend increased reorder quantity to meet demand.\n");
                break;
            default:
                prompt.append("\nProvide balanced reorder recommendations based on current inventory trends.\n");
                break;
        }
        
        prompt.append("\nRespond in JSON format with the following structure:\n");
        prompt.append("{\n");
        prompt.append("  \"recommendedQuantity\": integer,\n");
        prompt.append("  \"suggestedLeadTimeDays\": integer,\n");
        prompt.append("  \"confidence\": number (0.00-1.00),\n");
        prompt.append("  \"reasoning\": \"explanation of recommendation\"\n");
        prompt.append("}\n");
        prompt.append("Ensure the response is valid JSON and nothing else.");
        
        return prompt.toString();
    }

    private String buildProductContext(CommerceContext context) {
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Product ID: ").append(context.getProductId()).append("\n");
        contextBuilder.append("SKU: ").append(context.getSku()).append("\n");
        contextBuilder.append("Name: ").append(context.getProductName()).append("\n");
        contextBuilder.append("Category: ").append(context.getCategory()).append("\n");
        contextBuilder.append("Current Price: $").append(context.getCurrentPrice()).append("\n");
        contextBuilder.append("Stock Level: ").append(context.getStockLevel()).append("\n");
        contextBuilder.append("Reorder Threshold: ").append(context.getReorderThreshold()).append("\n");
        contextBuilder.append("Demand Velocity: ").append(context.getDemandVelocity()).append(" units/day\n");
        contextBuilder.append("Lifecycle Status: ").append(context.getLifecycleStatus()).append("\n");
        if (context.getCostPrice() != null) {
            contextBuilder.append("Cost Price: $").append(context.getCostPrice()).append("\n");
        }
        return contextBuilder.toString();
    }
}