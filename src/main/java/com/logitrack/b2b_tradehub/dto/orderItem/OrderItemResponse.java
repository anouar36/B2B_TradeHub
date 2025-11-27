package com.logitrack.b2b_tradehub.dto.orderItem;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemResponse {

    private Long id;

    private Long productId;
    private String productName;

    private Integer quantite;

    private BigDecimal prixUnitaireHT;

    private BigDecimal totalLigne;
}