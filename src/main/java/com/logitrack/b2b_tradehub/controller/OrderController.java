package com.logitrack.b2b_tradehub.controller;


import com.logitrack.b2b_tradehub.entity.Order;
import com.logitrack.b2b_tradehub.entity.enums.OrderStatus;
import com.logitrack.b2b_tradehub.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.findAll();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.findById(id);
        return order.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }



    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<Order> orders = orderService.findByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Order>> getPendingOrders() {
        List<Order> orders = orderService.findPendingOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Order>> getOrdersByDateRange(
            @RequestParam LocalDateTime startDate, 
            @RequestParam LocalDateTime endDate) {
        List<Order> orders = orderService.findByDateRange(startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        Order savedOrder = orderService.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @Valid @RequestBody Order orderDetails) {
        Optional<Order> orderOpt = orderService.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setSousTotalHT(orderDetails.getSousTotalHT());
            order.setTauxTVA(orderDetails.getTauxTVA());
            order.setMontantRemiseTotale(orderDetails.getMontantRemiseTotale());
            order.setStatus(orderDetails.getStatus());
            Order updatedOrder = orderService.save(order);
            return ResponseEntity.ok(updatedOrder);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        if (orderService.findById(id).isPresent()) {
            orderService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Order> confirmOrder(@PathVariable Long id) {
        Order order = orderService.confirmOrder(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        Order order = orderService.cancelOrder(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }


    @PatchMapping("/{id}/apply-promo")
    public ResponseEntity<Void> applyPromoCode(@PathVariable Long id, @RequestParam String promoCode) {
        if (orderService.findById(id).isPresent()) {
            orderService.applyPromoCode(id, promoCode);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/client/{clientId}/total-value")
    public ResponseEntity<BigDecimal> getTotalOrderValue(@PathVariable Long clientId) {
        BigDecimal totalValue = orderService.getTotalOrderValue(clientId);
        return ResponseEntity.ok(totalValue);
    }

    @GetMapping("/client/{clientId}/count")
    public ResponseEntity<Integer> getOrderCount(@PathVariable Long clientId) {
        Integer count = orderService.getOrderCount(clientId);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/update-payment")
    public ResponseEntity<Void> updatePaymentAmount(@PathVariable Long id, @RequestParam BigDecimal paymentAmount) {
        if (orderService.findById(id).isPresent()) {
            orderService.updatePaymentAmount(id, paymentAmount);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
