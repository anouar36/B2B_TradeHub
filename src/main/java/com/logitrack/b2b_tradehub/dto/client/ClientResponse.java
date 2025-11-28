package com.logitrack.b2b_tradehub.dto.client;

import com.logitrack.b2b_tradehub.entity.enums.CustomerTier;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ClientResponse {
    private Long id;
    private Long userId;
    private String nom;
    private String email;
    private CustomerTier tier;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    private LocalDateTime firstOrderDate;
    private LocalDateTime lastOrderDate;
    private LocalDateTime createdAt;

}