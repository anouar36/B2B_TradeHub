package com.logitrack.b2b_tradehub.dto.payment;

import com.logitrack.b2b_tradehub.entity.enums.PaymentStatus;
import com.logitrack.b2b_tradehub.entity.enums.PaymentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private String numeroPaiement;
    private BigDecimal montant;
    private PaymentType typePaiement;
    private PaymentStatus status;
    private LocalDate datePaiement;
    private LocalDate dateEncaissement;
    private LocalDate dateEcheance;
    private Long orderId; // Only returning ID to avoid infinite recursion
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}