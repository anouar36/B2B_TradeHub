package com.logitrack.b2b_tradehub.repository;

import com.logitrack.b2b_tradehub.entity.OrderItem;
import com.logitrack.b2b_tradehub.entity.Order;
import com.logitrack.b2b_tradehub.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    List<OrderItem> findByProductId(Long productId);
    List<OrderItem> findByOrder(Order order);
    List<OrderItem> findByProduct(Product product);
}
