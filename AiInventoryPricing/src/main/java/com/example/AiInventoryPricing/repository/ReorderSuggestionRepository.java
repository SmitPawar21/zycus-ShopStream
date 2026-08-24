package com.example.AiInventoryPricing.repository;

import com.example.AiInventoryPricing.entity.ReorderSuggestion;
import com.example.AiInventoryPricing.enums.SuggestionStatus;
import com.example.AiInventoryPricing.enums.TriggerReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReorderSuggestionRepository extends JpaRepository<ReorderSuggestion, Long> {
    List<ReorderSuggestion> findByProductId(Long productId);
    
    List<ReorderSuggestion> findByStatus(SuggestionStatus status);
    
    List<ReorderSuggestion> findByProductIdAndStatus(Long productId, SuggestionStatus status);
    
    @Query("SELECT rs FROM ReorderSuggestion rs WHERE rs.product.id = :productId AND rs.triggerReason = :triggerReason AND rs.status = 'PENDING'")
    Optional<ReorderSuggestion> findPendingByProductIdAndTriggerReason(
        @Param("productId") Long productId, 
        @Param("triggerReason") TriggerReason triggerReason
    );
}