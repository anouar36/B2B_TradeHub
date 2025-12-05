package com.logitrack.b2b_tradehub.controller;

import com.logitrack.b2b_tradehub.dto.payment.PaymentRequest;
import com.logitrack.b2b_tradehub.dto.payment.PaymentResponse;
import com.logitrack.b2b_tradehub.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.findByOrderId(orderId));
    }

    // Requirement: Système de Paiements Multi-Moyens (Adding a payment)
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Requirement: Traçabilité / Gestion de trésorerie (Process checks/transfers)
    @PatchMapping("/{id}/process")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.processPayment(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<PaymentResponse> rejectPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.rejectPayment(id));
    }

    // Requirement: Date d'échéance (Update valid ONLY for Checks/Transfers pending)
    @PatchMapping("/{id}/update-echeance")
    public ResponseEntity<Void> updateEcheance(@PathVariable Long id, @RequestParam String newEcheance) {
        // Parse String to LocalDate in Service
        paymentService.updateEcheance(id, java.time.LocalDate.parse(newEcheance));
        return ResponseEntity.ok().build();
    }

    // Treasury Management Helpers
    @GetMapping("/pending")
    public ResponseEntity<List<PaymentResponse>> getPendingPayments() {
        return ResponseEntity.ok(paymentService.findPendingPayments());
    }
}