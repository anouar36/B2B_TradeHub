package com.logitrack.b2b_tradehub.dto.payment;

import com.logitrack.b2b_tradehub.entity.enums.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment type is required")
    private PaymentType typePaiement;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal montant;

    @NotNull(message = "Payment date is required")
    private LocalDate datePaiement;

    private LocalDate dateEncaissement;
    private LocalDate dateEcheance;
}