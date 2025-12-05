package com.logitrack.b2b_tradehub.controller;

import com.logitrack.b2b_tradehub.dto.order.OrderCreateRequest;
import com.logitrack.b2b_tradehub.dto.order.OrderResponse;
import com.logitrack.b2b_tradehub.entity.enums.OrderStatus;
import com.logitrack.b2b_tradehub.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    // Requirement: Suivre les statuts
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.findByStatus(status));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<OrderResponse>> getPendingOrders() {
        return ResponseEntity.ok(orderService.findPendingOrders());
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<OrderResponse>> getOrdersByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(orderService.findByDateRange(startDate, endDate));
    }

    // Requirement: Créer une commande multi-produits
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse newOrder = orderService.createOrderFlow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
    }

    // Requirement: Transition PENDING -> CONFIRMED (Admin)
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.confirmOrder(id));
    }

    // Requirement: Transition PENDING -> CANCELED (Admin)
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}