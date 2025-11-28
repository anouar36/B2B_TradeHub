package com.logitrack.b2b_tradehub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "promo_codes")
@Data
@AllArgsConstructor
public class PromoCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false)
    private String code;
    
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(name = "discount_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal discountPercentage;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;
    
    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;
    
    @Column(name = "usage_unique", nullable = false)
    private Boolean usageUnique = false;
    
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @OneToMany(mappedBy = "promoCode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;
    
    // Constructors
    public PromoCode() {
    }
    
    public PromoCode(String code, BigDecimal discountPercentage, LocalDate validFrom, LocalDate validUntil, Boolean usageUnique) {
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.usageUnique = usageUnique;
    }
    

}
