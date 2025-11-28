package com.logitrack.b2b_tradehub.repository;

import com.logitrack.b2b_tradehub.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByDeletedFalse();
    
    List<Product> findByNomContaining(String nom);
    // Custom query for stock
    @Query("SELECT p FROM Product p WHERE p.stockDisponible > 0 AND p.deleted = false")
    List<Product> findInStock();
    

}
