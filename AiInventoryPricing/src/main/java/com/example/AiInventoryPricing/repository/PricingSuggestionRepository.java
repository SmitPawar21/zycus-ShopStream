package com.example.AiInventoryPricing.repository;

import com.example.AiInventoryPricing.entity.PricingSuggestion;
import com.example.AiInventoryPricing.enums.SuggestionStatus;
import com.example.AiInventoryPricing.enums.TriggerReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingSuggestionRepository extends JpaRepository<PricingSuggestion, Long> {
    List<PricingSuggestion> findByProductId(Long productId);
    
    List<PricingSuggestion> findByStatus(SuggestionStatus status);
    
    List<PricingSuggestion> findByProductIdAndStatus(Long productId, SuggestionStatus status);
    
    @Query("SELECT ps FROM PricingSuggestion ps WHERE ps.product.id = :productId AND ps.triggerReason = :triggerReason AND ps.status = 'PENDING'")
    Optional<PricingSuggestion> findPendingByProductIdAndTriggerReason(
        @Param("productId") Long productId, 
        @Param("triggerReason") TriggerReason triggerReason
    );
}