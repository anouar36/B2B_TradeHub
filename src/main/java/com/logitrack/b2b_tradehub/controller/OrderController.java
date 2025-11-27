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

import java.math.BigDecimal;
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
        // Service now throws ResourceNotFoundException if missing, handled by global handler
        // or returns the DTO directly
        return ResponseEntity.ok(orderService.findById(id));
    }

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

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse newOrder = orderService.createOrderFlow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
    }

    // NOTE: Removed generic "updateOrder" because updating a full order is complex via DTO
    // usually specific patch methods (like below) are safer.
    // If you need full update, create an OrderUpdateRequest DTO.

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.confirmOrder(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @PatchMapping("/{id}/apply-promo")
    public ResponseEntity<Void> applyPromoCode(@PathVariable Long id, @RequestParam String promoCode) {
        orderService.applyPromoCode(id, promoCode);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/update-payment")
    public ResponseEntity<Void> updatePaymentAmount(@PathVariable Long id, @RequestParam BigDecimal paymentAmount) {
        orderService.updatePaymentAmount(id, paymentAmount);
        return ResponseEntity.ok().build();
    }

    // Analytics Endpoints

    @GetMapping("/client/{clientId}/total-value")
    public ResponseEntity<BigDecimal> getTotalOrderValue(@PathVariable Long clientId) {
        return ResponseEntity.ok(orderService.getTotalOrderValue(clientId));
    }

    @GetMapping("/client/{clientId}/count")
    public ResponseEntity<Integer> getOrderCount(@PathVariable Long clientId) {
        return ResponseEntity.ok(orderService.getOrderCount(clientId));
    }
}