package com.example.AiInventoryPricing.service.impl;

import com.example.AiInventoryPricing.dto.*;
import com.example.AiInventoryPricing.enums.SuggestionStatus;
import com.example.AiInventoryPricing.enums.TriggerReason;
import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.entity.PricingSuggestion;
import com.example.AiInventoryPricing.entity.ReorderSuggestion;
import com.example.AiInventoryPricing.repository.ProductRepository;
import com.example.AiInventoryPricing.enums.ChangeDirection;
import com.example.AiInventoryPricing.repository.PricingSuggestionRepository;
import com.example.AiInventoryPricing.repository.ReorderSuggestionRepository;
import com.example.AiInventoryPricing.service.SuggestionService;
import com.example.AiInventoryPricing.exception.ResourceNotFoundException;
import com.example.AiInventoryPricing.exception.InvalidSuggestionOperationException;
import com.example.AiInventoryPricing.strategy.StrategyRegistry;
import com.example.AiInventoryPricing.strategy.pricing.PricingStrategy;
import com.example.AiInventoryPricing.strategy.reorder.ReorderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SuggestionServiceImpl implements SuggestionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SuggestionServiceImpl.class);
    
    @Autowired
    private PricingSuggestionRepository pricingSuggestionRepository;
    
    @Autowired
    private ReorderSuggestionRepository reorderSuggestionRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private StrategyRegistry strategyRegistry;
    
    @Override
    public List<PricingSuggestionDto> getAllPricingSuggestions() {
        List<PricingSuggestion> suggestions = pricingSuggestionRepository.findAll();
        List<PricingSuggestionDto> dtos = new ArrayList<>();
        for (PricingSuggestion suggestion : suggestions) {
            dtos.add(convertToPricingDto(suggestion));
        }
        return dtos;
    }
    
    @Override
    public List<PricingSuggestionDto> getPricingSuggestionsByStatus(SuggestionStatus status) {
        List<PricingSuggestion> suggestions = pricingSuggestionRepository.findByStatus(status);
        List<PricingSuggestionDto> dtos = new ArrayList<>();
        for (PricingSuggestion suggestion : suggestions) {
            dtos.add(convertToPricingDto(suggestion));
        }
        return dtos;
    }
    
    @Override
    public PricingSuggestionDto getPricingSuggestionById(Long id) {
        PricingSuggestion suggestion = pricingSuggestionRepository.findById(id)
                .orElse(null);
        if (suggestion == null) {
            throw new ResourceNotFoundException("Pricing suggestion not found with id: " + id);
        }
        return convertToPricingDto(suggestion);
    }
    
    @Override
    @Transactional
    public PricingSuggestionDto updatePricingSuggestionStatus(Long id, AcceptSuggestionRequestDto requestDto) {
        PricingSuggestion suggestion = pricingSuggestionRepository.findById(id)
                .orElse(null);
        if (suggestion == null) {
            throw new ResourceNotFoundException("Pricing suggestion not found with id: " + id);
        }
        
        // Validate transition
        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new InvalidSuggestionOperationException("Cannot update status of suggestion that is not PENDING");
        }
        
        if (requestDto.getStatus() == SuggestionStatus.PENDING) {
            throw new InvalidSuggestionOperationException("Cannot update suggestion status to PENDING");
        }
        
        suggestion.setStatus(requestDto.getStatus());
        PricingSuggestion updatedSuggestion = pricingSuggestionRepository.save(suggestion);
        
        // If accepted, update the product's current price
        if (requestDto.getStatus() == SuggestionStatus.ACCEPTED) {
            Product product = suggestion.getProduct();
            product.setCurrentPrice(suggestion.getRecommendedPrice());
            productRepository.save(product);
        }
        
        return convertToPricingDto(updatedSuggestion);
    }
    
    @Override
    @Transactional
    public PricingSuggestion createPricingSuggestion(PricingSuggestion pricingSuggestion) {
        return pricingSuggestionRepository.save(pricingSuggestion);
    }
    
    @Override
    public List<ReorderSuggestionDto> getAllReorderSuggestions() {
        List<ReorderSuggestion> suggestions = reorderSuggestionRepository.findAll();
        List<ReorderSuggestionDto> dtos = new ArrayList<>();
        for (ReorderSuggestion suggestion : suggestions) {
            dtos.add(convertToReorderDto(suggestion));
        }
        return dtos;
    }
    
    @Override
    public List<ReorderSuggestionDto> getReorderSuggestionsByStatus(SuggestionStatus status) {
        List<ReorderSuggestion> suggestions = reorderSuggestionRepository.findByStatus(status);
        List<ReorderSuggestionDto> dtos = new ArrayList<>();
        for (ReorderSuggestion suggestion : suggestions) {
            dtos.add(convertToReorderDto(suggestion));
        }
        return dtos;
    }
    
    @Override
    public ReorderSuggestionDto getReorderSuggestionById(Long id) {
        ReorderSuggestion suggestion = reorderSuggestionRepository.findById(id)
                .orElse(null);
        if (suggestion == null) {
            throw new ResourceNotFoundException("Reorder suggestion not found with id: " + id);
        }
        return convertToReorderDto(suggestion);
    }
    
    @Override
    @Transactional
    public ReorderSuggestionDto updateReorderSuggestionStatus(Long id, AcceptSuggestionRequestDto requestDto) {
        ReorderSuggestion suggestion = reorderSuggestionRepository.findById(id)
                .orElse(null);
        if (suggestion == null) {
            throw new ResourceNotFoundException("Reorder suggestion not found with id: " + id);
        }
        
        // Validate transition
        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new InvalidSuggestionOperationException("Cannot update status of suggestion that is not PENDING");
        }
        
        if (requestDto.getStatus() == SuggestionStatus.PENDING) {
            throw new InvalidSuggestionOperationException("Cannot update suggestion status to PENDING");
        }
        
        suggestion.setStatus(requestDto.getStatus());
        ReorderSuggestion updatedSuggestion = reorderSuggestionRepository.save(suggestion);
        
        // If accepted, update the product's stock level
        if (requestDto.getStatus() == SuggestionStatus.ACCEPTED) {
            Product product = suggestion.getProduct();
            int quantityToAdd = suggestion.getRecommendedQuantity() != null ? suggestion.getRecommendedQuantity() : 0;
            int newStockLevel = product.getStockLevel() + quantityToAdd;
            product.setStockLevel(newStockLevel);
            productRepository.save(product);
        }
        
        return convertToReorderDto(updatedSuggestion);
    }
    
    @Override
    @Transactional
    public ReorderSuggestion createReorderSuggestion(ReorderSuggestion reorderSuggestion) {
        return reorderSuggestionRepository.save(reorderSuggestion);
    }
    
    @Override
    @Transactional
    public PricingSuggestionDto generatePricingSuggestionForProduct(Long productId, CreatePricingSuggestionRequestDto requestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Create pricing suggestion based on business logic
        PricingSuggestion suggestion = new PricingSuggestion();
        suggestion.setProduct(product);
        suggestion.setCurrentPrice(product.getCurrentPrice());
        suggestion.setRecommendedPrice(requestDto.getProposedPrice());
        suggestion.setChangeDirection(determineChangeDirection(product.getCurrentPrice(), requestDto.getProposedPrice()));
        suggestion.setConfidence(requestDto.getConfidence());
        suggestion.setReasoning(requestDto.getReasoning());
        suggestion.setTriggerReason(requestDto.getTriggerReason());
        suggestion.setStatus(SuggestionStatus.PENDING);
        
        PricingSuggestion savedSuggestion = pricingSuggestionRepository.save(suggestion);
        return convertToPricingDto(savedSuggestion);
    }
    
    @Override
    @Transactional
    public ReorderSuggestionDto generateReorderSuggestionForProduct(Long productId, CreateReorderSuggestionRequestDto requestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Create reorder suggestion based on business logic
        ReorderSuggestion suggestion = new ReorderSuggestion();
        suggestion.setProduct(product);
        suggestion.setCurrentStock(product.getStockLevel());
        suggestion.setRecommendedQuantity(requestDto.getRecommendedQuantity());
        suggestion.setSuggestedLeadTimeDays(requestDto.getSuggestedLeadTimeDays());
        suggestion.setConfidence(requestDto.getConfidence());
        suggestion.setReasoning(requestDto.getReasoning());
        suggestion.setTriggerReason(requestDto.getTriggerReason());
        suggestion.setStatus(SuggestionStatus.PENDING);
        
        ReorderSuggestion savedSuggestion = reorderSuggestionRepository.save(suggestion);
        return convertToReorderDto(savedSuggestion);
    }
    
    private ChangeDirection determineChangeDirection(java.math.BigDecimal currentPrice, java.math.BigDecimal recommendedPrice) {
        int comparison = currentPrice.compareTo(recommendedPrice);
        if (comparison < 0) {
            return ChangeDirection.INCREASE;
        } else if (comparison > 0) {
            return ChangeDirection.DECREASE;
        } else {
            return ChangeDirection.HOLD;
        }
    }
    
    @Override
    @Transactional
    public PricingSuggestionDto generatePricingSuggestionUsingStrategy(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        Optional<PricingStrategy> strategyOpt = strategyRegistry.getActivePricingStrategy();
        if (!strategyOpt.isPresent()) {
            throw new IllegalStateException("No active pricing strategy configured");
        }
        
        PricingStrategy strategy = strategyOpt.get();
        PricingSuggestion suggestion = strategy.generatePricingSuggestion(product);
        
        PricingSuggestion savedSuggestion = pricingSuggestionRepository.save(suggestion);
        return convertToPricingDto(savedSuggestion);
    }
    
    @Override
    @Transactional
    public ReorderSuggestionDto generateReorderSuggestionUsingStrategy(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        Optional<ReorderStrategy> strategyOpt = strategyRegistry.getActiveReorderStrategy();
        if (!strategyOpt.isPresent()) {
            throw new IllegalStateException("No active reorder strategy configured");
        }
        
        ReorderStrategy strategy = strategyOpt.get();
        ReorderSuggestion suggestion = strategy.generateReorderSuggestion(product);
        
        ReorderSuggestion savedSuggestion = reorderSuggestionRepository.save(suggestion);
        return convertToReorderDto(savedSuggestion);
    }
    
    @Override
    @Transactional
    public boolean generatePricingSuggestionAsync(Long productId, TriggerReason triggerReason) {
        // Check if there's already a pending suggestion for this product and trigger reason
        Optional<PricingSuggestion> existingPendingSuggestion = 
            pricingSuggestionRepository.findPendingByProductIdAndTriggerReason(productId, triggerReason);
        
        if (existingPendingSuggestion.isPresent()) {
            logger.info("Skipping pricing suggestion generation - pending suggestion already exists for product ID: {}, trigger: {}", 
                       productId, triggerReason);
            return false; // Already has a pending suggestion
        }
        
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
            
            Optional<PricingStrategy> strategyOpt = strategyRegistry.getActivePricingStrategy();
            if (!strategyOpt.isPresent()) {
                logger.warn("No active pricing strategy configured for product ID: {}", productId);
                return false;
            }
            
            PricingStrategy strategy = strategyOpt.get();
            PricingSuggestion suggestion = strategy.generatePricingSuggestion(product);
            suggestion.setTriggerReason(triggerReason); // Set the trigger reason
            
            pricingSuggestionRepository.save(suggestion);
            logger.info("Generated pricing suggestion for product ID: {}, trigger: {}", productId, triggerReason);
            return true;
        } catch (Exception e) {
            logger.error("Error generating pricing suggestion for product ID: {}, trigger: {}", productId, triggerReason, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean generateReorderSuggestionAsync(Long productId, TriggerReason triggerReason) {
        // Check if there's already a pending suggestion for this product and trigger reason
        Optional<ReorderSuggestion> existingPendingSuggestion = 
            reorderSuggestionRepository.findPendingByProductIdAndTriggerReason(productId, triggerReason);
        
        if (existingPendingSuggestion.isPresent()) {
            logger.info("Skipping reorder suggestion generation - pending suggestion already exists for product ID: {}, trigger: {}", 
                       productId, triggerReason);
            return false; // Already has a pending suggestion
        }
        
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
            
            Optional<ReorderStrategy> strategyOpt = strategyRegistry.getActiveReorderStrategy();
            if (!strategyOpt.isPresent()) {
                logger.warn("No active reorder strategy configured for product ID: {}", productId);
                return false;
            }
            
            ReorderStrategy strategy = strategyOpt.get();
            ReorderSuggestion suggestion = strategy.generateReorderSuggestion(product);
            suggestion.setTriggerReason(triggerReason); // Set the trigger reason
            
            reorderSuggestionRepository.save(suggestion);
            logger.info("Generated reorder suggestion for product ID: {}, trigger: {}", productId, triggerReason);
            return true;
        } catch (Exception e) {
            logger.error("Error generating reorder suggestion for product ID: {}, trigger: {}", productId, triggerReason, e);
            return false;
        }
    }
    
    @Override
    public boolean setActivePricingStrategy(String strategyName) {
        return strategyRegistry.setActivePricingStrategy(strategyName);
    }
    
    @Override
    public boolean setActiveReorderStrategy(String strategyName) {
        return strategyRegistry.setActiveReorderStrategy(strategyName);
    }
    
    @Override
    public String[] getAvailablePricingStrategies() {
        return strategyRegistry.getPricingStrategyNames();
    }
    
    @Override
    public String[] getAvailableReorderStrategies() {
        return strategyRegistry.getReorderStrategyNames();
    }
    
    @Override
    public String getActivePricingStrategy() {
        Optional<PricingStrategy> activeStrategy = strategyRegistry.getActivePricingStrategy();
        return activeStrategy.isPresent() ? activeStrategy.get().getName() : "NONE";
    }
    
    @Override
    public String getActiveReorderStrategy() {
        Optional<ReorderStrategy> activeStrategy = strategyRegistry.getActiveReorderStrategy();
        return activeStrategy.isPresent() ? activeStrategy.get().getName() : "NONE";
    }
    
    private PricingSuggestionDto convertToPricingDto(PricingSuggestion suggestion) {
        PricingSuggestionDto dto = new PricingSuggestionDto();
        dto.setId(suggestion.getId());
        dto.setProductId(suggestion.getProduct().getId());
        dto.setCurrentPrice(suggestion.getCurrentPrice());
        dto.setRecommendedPrice(suggestion.getRecommendedPrice());
        dto.setChangeDirection(suggestion.getChangeDirection());
        dto.setConfidence(suggestion.getConfidence());
        dto.setReasoning(suggestion.getReasoning());
        dto.setStatus(suggestion.getStatus());
        dto.setTriggerReason(suggestion.getTriggerReason());
        return dto;
    }
    
    private ReorderSuggestionDto convertToReorderDto(ReorderSuggestion suggestion) {
        ReorderSuggestionDto dto = new ReorderSuggestionDto();
        dto.setId(suggestion.getId());
        dto.setProductId(suggestion.getProduct().getId());
        dto.setCurrentStock(suggestion.getCurrentStock());
        dto.setRecommendedQuantity(suggestion.getRecommendedQuantity());
        dto.setSuggestedLeadTimeDays(suggestion.getSuggestedLeadTimeDays());
        dto.setConfidence(suggestion.getConfidence());
        dto.setReasoning(suggestion.getReasoning());
        dto.setStatus(suggestion.getStatus());
        dto.setTriggerReason(suggestion.getTriggerReason());
        return dto;
    }
}