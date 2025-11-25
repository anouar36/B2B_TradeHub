package com.logitrack.b2b_tradehub.controller;

import com.logitrack.b2b_tradehub.entity.OrderItem;
import com.logitrack.b2b_tradehub.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/order-items")
@CrossOrigin(origins = "*")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    @GetMapping
    public ResponseEntity<List<OrderItem>> getAllOrderItems() {
        List<OrderItem> orderItems = orderItemService.findAll();
        return ResponseEntity.ok(orderItems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItem> getOrderItemById(@PathVariable Long id) {
        Optional<OrderItem> orderItem = orderItemService.findById(id);
        return orderItem.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItem>> getOrderItemsByOrderId(@PathVariable Long orderId) {
        List<OrderItem> orderItems = orderItemService.findByOrderId(orderId);
        return ResponseEntity.ok(orderItems);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<OrderItem>> getOrderItemsByProductId(@PathVariable Long productId) {
        List<OrderItem> orderItems = orderItemService.findByProductId(productId);
        return ResponseEntity.ok(orderItems);
    }

    @PostMapping
    public ResponseEntity<OrderItem> createOrderItem(@Valid @RequestBody OrderItem orderItem) {
        OrderItem savedOrderItem = orderItemService.save(orderItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrderItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItem> updateOrderItem(@PathVariable Long id, @Valid @RequestBody OrderItem orderItemDetails) {
        Optional<OrderItem> orderItemOpt = orderItemService.findById(id);
        if (orderItemOpt.isPresent()) {
            OrderItem orderItem = orderItemOpt.get();
            orderItem.setQuantite(orderItemDetails.getQuantite());
            orderItem.setPrixUnitaireHT(orderItemDetails.getPrixUnitaireHT());
            OrderItem updatedOrderItem = orderItemService.save(orderItem);
            return ResponseEntity.ok(updatedOrderItem);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Long id) {
        if (orderItemService.findById(id).isPresent()) {
            orderItemService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/order/{orderId}/total")
    public ResponseEntity<BigDecimal> calculateOrderTotal(@PathVariable Long orderId) {
        BigDecimal total = orderItemService.calculateOrderTotal(orderId);
        return ResponseEntity.ok(total);
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<Void> updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        if (orderItemService.findById(id).isPresent()) {
            orderItemService.updateQuantity(id, quantity);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<Void> updatePrice(@PathVariable Long id, @RequestParam BigDecimal price) {
        if (orderItemService.findById(id).isPresent()) {
            orderItemService.updatePrice(id, price);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/product/{productId}/total-quantity")
    public ResponseEntity<Integer> getTotalQuantityForProduct(@PathVariable Long productId) {
        Integer totalQuantity = orderItemService.getTotalQuantityForProduct(productId);
        return ResponseEntity.ok(totalQuantity);
    }
}
