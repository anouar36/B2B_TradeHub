package com.logitrack.b2b_tradehub.repository;

import com.logitrack.b2b_tradehub.entity.Order;
import com.logitrack.b2b_tradehub.entity.enums.OrderStatus;
import com.logitrack.b2b_tradehub.entity.Client;
import com.logitrack.b2b_tradehub.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByClient(Client client);
    
    List<Order> findByClientId(Long clientId);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByPromoCode(PromoCode promoCode);
    
    List<Order> findByDateCommandeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Order> findByStatusOrderByDateCommandeDesc(OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING'")
    List<Order> findPendingOrders();
    
    @Query("SELECT o FROM Order o WHERE o.client.id = :clientId ORDER BY o.dateCommande DESC")
    List<Order> findByClientIdOrderByDateDesc(@Param("clientId") Long clientId);
    
    @Query("SELECT SUM(o.totalTTC) FROM Order o WHERE o.client.id = :clientId AND o.status = 'CONFIRMED'")
    BigDecimal getTotalOrderValueByClient(@Param("clientId") Long clientId);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.client.id = :clientId AND o.status = 'CONFIRMED'")
    Long getOrderCountByClient(@Param("clientId") Long clientId);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.dateCommande BETWEEN :startDate AND :endDate")
    List<Order> findByStatusAndDateRange(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.montantRestant > 0")
    List<Order> findOrdersWithRemainingBalance();
    
    @Query("SELECT SUM(o.totalTTC) FROM Order o WHERE o.dateCommande BETWEEN :startDate AND :endDate AND o.status = 'CONFIRMED'")
    BigDecimal getTotalSalesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.client.id = :clientId ORDER BY o.dateCommande DESC")
    List<Order> findByClientIdOrderByDateCommandeDesc(@Param("clientId") Long clientId);

    Integer countByClientId(Long clientId);

}
