package com.logitrack.b2b_tradehub.dto.order;

import com.logitrack.b2b_tradehub.dto.orderItem.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderCreateRequest {

    @NotNull(message = "Client ID is required.")
    private Long clientId;

    private String promoCode; // Optional

    private LocalDateTime orderDate;

    @NotEmpty(message = "Order must contain at least one item.")
    @Valid
    private List<OrderItemRequest> items;
}