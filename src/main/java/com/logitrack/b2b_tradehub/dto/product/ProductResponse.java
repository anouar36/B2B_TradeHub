    package com.logitrack.b2b_tradehub.dto.product  ;

    import lombok.Data;
    import java.math.BigDecimal;
    import java.time.LocalDateTime;

    @Data
    public class ProductResponse {
        private Long id;
        private String nom;
        private BigDecimal prixUnitaireHT;
        private Integer stockDisponible;
        private LocalDateTime createdAt;
        private Boolean deleted;
    }