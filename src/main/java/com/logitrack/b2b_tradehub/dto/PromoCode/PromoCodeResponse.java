package com.logitrack.b2b_tradehub.dto.PromoCode;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PromoCodeResponse {
    private Long id;
    private String code;
    private BigDecimal discountPercentage;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Boolean usageUnique;
    private Boolean active;
}
