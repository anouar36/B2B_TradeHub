package com.logitrack.b2b_tradehub.dto.order;

import com.logitrack.b2b_tradehub.dto.orderItem.OrderItemResponse;
import com.logitrack.b2b_tradehub.entity.enums.OrderStatus;
import com.logitrack.b2b_tradehub.entity.enums.CustomerTier;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private Long clientId;
    private CustomerTier clientTierAtOrder;

    // --- Calculated Totals ---
    private BigDecimal sousTotal;
    private BigDecimal montantRemise;
    private BigDecimal montantHTApresRemise;
    private BigDecimal tauxTVA;
    private BigDecimal montantTVA;
    private BigDecimal totalTTC;
    private BigDecimal montantRestant;

    // --- Status and Dates ---
    private OrderStatus status;
    private String codePromo;
    private LocalDateTime dateCommande;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;

    // --- Items ---
    private List<OrderItemResponse> items;
}