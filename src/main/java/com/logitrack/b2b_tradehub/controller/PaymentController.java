package com.logitrack.b2b_tradehub.controller;


import com.logitrack.b2b_tradehub.entity.Payment;
import com.logitrack.b2b_tradehub.entity.enums.PaymentStatus;
import com.logitrack.b2b_tradehub.entity.enums.PaymentType;
import com.logitrack.b2b_tradehub.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.findAll();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Optional<Payment> payment = paymentService.findById(id);
        return payment.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/numero/{numeroPaiement}")
    public ResponseEntity<Payment> getPaymentByNumeroPaiement(@PathVariable String numeroPaiement) {
        Optional<Payment> payment = paymentService.findByNumeroPaiement(numeroPaiement);
        return payment.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrderId(@PathVariable Long orderId) {
        List<Payment> payments = paymentService.findByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<Payment> payments = paymentService.findByStatus(status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Payment>> getPaymentsByType(@PathVariable PaymentType type) {
        List<Payment> payments = paymentService.findByType(type);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Payment>> getPendingPayments() {
        List<Payment> payments = paymentService.findPendingPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Payment>> getOverduePayments() {
        List<Payment> payments = paymentService.findOverduePayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Payment>> getPaymentsByDateRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        List<Payment> payments = paymentService.findByDateRange(startDate, endDate);
        return ResponseEntity.ok(payments);
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody Payment payment) {
        Payment savedPayment = paymentService.save(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPayment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @Valid @RequestBody Payment paymentDetails) {
        Optional<Payment> paymentOpt = paymentService.findById(id);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setNumeroPaiement(paymentDetails.getNumeroPaiement());
            payment.setMontant(paymentDetails.getMontant());
            payment.setTypePaiement(paymentDetails.getTypePaiement());
            payment.setDatePaiement(paymentDetails.getDatePaiement());
            payment.setDateEcheance(paymentDetails.getDateEcheance());
            payment.setStatus(paymentDetails.getStatus());
            Payment updatedPayment = paymentService.save(payment);
            return ResponseEntity.ok(updatedPayment);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        if (paymentService.findById(id).isPresent()) {
            paymentService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/process")
    public ResponseEntity<Payment> processPayment(@PathVariable Long id) {
        Payment payment = paymentService.processPayment(id);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Payment> rejectPayment(@PathVariable Long id) {
        Payment payment = paymentService.rejectPayment(id);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/order/{orderId}/total")
    public ResponseEntity<BigDecimal> getTotalPaymentsForOrder(@PathVariable Long orderId) {
        BigDecimal total = paymentService.getTotalPaymentsForOrder(orderId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/order/{orderId}/pending-total")
    public ResponseEntity<BigDecimal> getPendingPaymentsForOrder(@PathVariable Long orderId) {
        BigDecimal pendingTotal = paymentService.getPendingPaymentsForOrder(orderId);
        return ResponseEntity.ok(pendingTotal);
    }

    @PatchMapping("/{id}/update-echeance")
    public ResponseEntity<Void> updateEcheance(@PathVariable Long id, @RequestParam LocalDate newEcheance) {
        if (paymentService.findById(id).isPresent()) {
            paymentService.updateEcheance(id, newEcheance);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
