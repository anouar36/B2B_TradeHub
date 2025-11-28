package com.logitrack.b2b_tradehub.entity;

import com.logitrack.b2b_tradehub.entity.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_commande", nullable = false)
    private LocalDateTime dateCommande;

    @DecimalMin("0.0")
    @Column(name = "sous_total_ht", precision = 10, scale = 2, nullable = false)
    @Builder.Default // مهم جداً عند استخدام Builder
    private BigDecimal sousTotalHT = BigDecimal.ZERO; // تهيئة بـ صفر

    @DecimalMin("0.0")
    @Builder.Default
    @Column(name = "montant_remise_totale", precision = 10, scale = 2, nullable = false)
    private BigDecimal montantRemiseTotale = BigDecimal.ZERO;

    @DecimalMin("0.0")
    @Column(name = "montant_ht_apres_remise", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal montantHTApresRemise = BigDecimal.ZERO; // تهيئة بـ صفر

    @DecimalMin("0.0")
    @Column(name = "taux_tva", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal tauxTVA = new BigDecimal("20.00"); // القيمة الافتراضية

    @DecimalMin("0.0")
    @Column(name = "montant_tva", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal montantTVA = BigDecimal.ZERO; // تهيئة بـ صفر

    @DecimalMin("0.0")
    @Column(name = "total_ttc", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalTTC = BigDecimal.ZERO; // تهيئة بـ صفر

    @Size(max = 50)
    @Column(name = "promo_code_id")
    private String promoCodeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @DecimalMin("0.0")
    @Column(name = "montant_restant", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal montantRestant = BigDecimal.ZERO; // تهيئة بـ صفر

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_code_fk")
    private PromoCode promoCode;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    // Constructor المستخدم من قبل Service
    public Order(Client client, BigDecimal sousTotalHT, BigDecimal tauxTVA) {
        this();
        this.client = client;
        this.sousTotalHT = (sousTotalHT != null) ? sousTotalHT : BigDecimal.ZERO;
        this.tauxTVA = (tauxTVA != null) ? tauxTVA : new BigDecimal("20.00");
        calculateTotals();
    }

    public void calculateTotals() {
        // حماية من القيم الفارغة (Null Safety)
        if (this.sousTotalHT == null) this.sousTotalHT = BigDecimal.ZERO;
        if (this.montantRemiseTotale == null) this.montantRemiseTotale = BigDecimal.ZERO;
        if (this.tauxTVA == null) this.tauxTVA = BigDecimal.ZERO;

        this.montantHTApresRemise = this.sousTotalHT.subtract(this.montantRemiseTotale);
        this.montantTVA = this.montantHTApresRemise.multiply(this.tauxTVA).divide(BigDecimal.valueOf(100));
        this.totalTTC = this.montantHTApresRemise.add(this.montantTVA);
        this.montantRestant = this.totalTTC;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.dateCommande == null) {
            this.dateCommande = LocalDateTime.now();
        }
        if (this.montantHTApresRemise == null) calculateTotals();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}