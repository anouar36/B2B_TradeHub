package com.logitrack.b2b_tradehub.dto.orderItem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemRequest {

    @NotNull(message = "Product ID is required.")
    private Long productId;

    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    private Integer quantite;

    /**
     * CRITICAL FIELD: This must be present to avoid NullPointerException
     */
    @NotNull(message = "Unit price (HT) is required.")
    @Positive(message = "Price must be positive.")
    private BigDecimal prixUnitaireHT;
}