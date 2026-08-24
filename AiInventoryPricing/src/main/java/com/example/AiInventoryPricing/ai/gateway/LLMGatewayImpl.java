package com.example.AiInventoryPricing.ai.gateway;

import com.example.AiInventoryPricing.ai.dto.CommerceContext;
import com.example.AiInventoryPricing.ai.prompt.PromptBuilder;
import com.example.AiInventoryPricing.enums.TriggerReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.Map;

@Component
public class LLMGatewayImpl implements LLMGateway {
    
    private static final Logger logger = LoggerFactory.getLogger(LLMGatewayImpl.class);
    
    @Value("${llm.api.key:}")
    private String apiKey;
    
    @Value("${llm.api.url:https://litellm-qc.zycus.net/v1/chat/completions}")
    private String apiUrl;
    
    @Value("${llm.model.name:qwen-cursor}")
    private String modelName;
    
    private final PromptBuilder promptBuilder;
    private final RestTemplate restTemplate;
    
    public LLMGatewayImpl(PromptBuilder promptBuilder, RestTemplate restTemplate) {
        this.promptBuilder = promptBuilder;
        this.restTemplate = restTemplate;
    }
    
    @Override
    public String generatePricingRecommendation(CommerceContext context, TriggerReason triggerReason) {
        String prompt = promptBuilder.buildPricingPrompt(context, triggerReason);
        return callLLM(prompt, context.getSku());
    }
    
    @Override
    public String generateReorderRecommendation(CommerceContext context, TriggerReason triggerReason) {
        String prompt = promptBuilder.buildReorderPrompt(context, triggerReason);
        return callLLM(prompt, context.getSku());
    }
    
    private String callLLM(String prompt, String productId) {
        // Check if API key is configured
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("LLM API key not configured. Returning empty response.");
            return "";
        }
        
        try {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.add("product", productId);
            
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            
            requestBody.put("messages", new Object[]{message});
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            logger.info("Calling LLM API for product: {}", productId);
            
            // Make the API call with a timeout
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            logger.info("Received response from LLM API for product: {}", productId);
            return response.getBody();
            
        } catch (HttpClientErrorException e) {
            logger.error("HTTP client error calling LLM API for product {}: {}", productId, e.getMessage());
            return "";
        } catch (HttpServerErrorException e) {
            logger.error("HTTP server error calling LLM API for product {}: {}", productId, e.getMessage());
            return "";
        } catch (ResourceAccessException e) {
            logger.error("Network error calling LLM API for product {}: {}", productId, e.getMessage());
            return "";
        } catch (Exception e) {
            logger.error("Unexpected error calling LLM API for product {}: {}", productId, e.getMessage());
            return "";
        }
    }
}