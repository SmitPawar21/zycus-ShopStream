package com.example.AiInventoryPricing.repository;

import com.example.AiInventoryPricing.entity.Product;
import com.example.AiInventoryPricing.enums.Category;
import com.example.AiInventoryPricing.enums.LifecycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    
    List<Product> findByLifecycleStatus(LifecycleStatus status);
    
    List<Product> findByCategory(Category category);
    
    List<Product> findByLifecycleStatusAndCategory(LifecycleStatus status, Category category);
    
    @Query("SELECT AVG(p.demandVelocity) FROM Product p WHERE p.category = :category")
    Double getAverageDemandVelocityByCategory(@Param("category") Category category);
}