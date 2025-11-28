package com.logitrack.b2b_tradehub.entity;

import com.logitrack.b2b_tradehub.entity.enums.PaymentStatus;
import com.logitrack.b2b_tradehub.entity.enums.PaymentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "numero_paiement", unique = true, nullable = false)
    private String numeroPaiement;
    
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal montant;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_paiement", nullable = false)
    private PaymentType typePaiement;
    
    @Column(name = "date_paiement", nullable = false)
    private LocalDate datePaiement;
    
    @Column(name = "date_encaissement")
    private LocalDate dateEncaissement;
    
    @Column(name = "date_echeance")
    private LocalDate dateEcheance;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    // Constructors
    public Payment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.datePaiement = LocalDate.now();
        this.status = PaymentStatus.EN_ATTENTE;
    }
    

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
