package com.example.AiInventoryPricing.ai.parser;

import com.example.AiInventoryPricing.ai.dto.PricingRecommendation;
import com.example.AiInventoryPricing.ai.dto.ReorderRecommendation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LLMResponseParser {
    
    private static final Logger logger = LoggerFactory.getLogger(LLMResponseParser.class);
    private final ObjectMapper objectMapper;
    
    public LLMResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public PricingRecommendation parsePricingRecommendation(String jsonResponse) {
        try {
            // Extract the content from the chat completions response
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode choices = rootNode.path("choices");
            
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message");
                String content = message.path("content").asText();
                
                // Parse the actual recommendation JSON from the content
                JsonNode recommendationNode = objectMapper.readTree(content);
                
                PricingRecommendation recommendation = new PricingRecommendation();
                recommendation.setRecommendedPrice(new BigDecimal(recommendationNode.path("recommendedPrice").asText()));
                
                String changeDirectionStr = recommendationNode.path("changeDirection").asText();
                recommendation.setChangeDirection(com.example.AiInventoryPricing.enums.ChangeDirection.valueOf(changeDirectionStr));
                
                recommendation.setConfidence(new BigDecimal(recommendationNode.path("confidence").asText()));
                recommendation.setReasoning(recommendationNode.path("reasoning").asText());
                
                logger.info("Successfully parsed pricing recommendation: {}", recommendation);
                return recommendation;
            }
        } catch (Exception e) {
            logger.error("Failed to parse pricing recommendation from JSON: {}", jsonResponse, e);
        }
        
        return null;
    }
    
    public ReorderRecommendation parseReorderRecommendation(String jsonResponse) {
        try {
            // Extract the content from the chat completions response
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode choices = rootNode.path("choices");
            
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message");
                String content = message.path("content").asText();
                
                // Parse the actual recommendation JSON from the content
                JsonNode recommendationNode = objectMapper.readTree(content);
                
                ReorderRecommendation recommendation = new ReorderRecommendation();
                recommendation.setRecommendedQuantity(recommendationNode.path("recommendedQuantity").asInt());
                recommendation.setSuggestedLeadTimeDays(recommendationNode.path("suggestedLeadTimeDays").asInt());
                recommendation.setConfidence(new BigDecimal(recommendationNode.path("confidence").asText()));
                recommendation.setReasoning(recommendationNode.path("reasoning").asText());
                
                logger.info("Successfully parsed reorder recommendation: {}", recommendation);
                return recommendation;
            }
        } catch (Exception e) {
            logger.error("Failed to parse reorder recommendation from JSON: {}", jsonResponse, e);
        }
        
        return null;
    }
}