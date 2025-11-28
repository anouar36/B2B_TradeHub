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


    List<Order> findByStatus(OrderStatus status);

    List<Order> findByDateCommandeBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByClientId(Long clientId);

    @Query("SELECT o FROM Order o WHERE o.client.id = :clientId ORDER BY o.dateCommande DESC")
    List<Order> findByClientIdOrderByDateCommandeDesc(@Param("clientId") Long clientId);

    Integer countByClientId(Long clientId);

}
