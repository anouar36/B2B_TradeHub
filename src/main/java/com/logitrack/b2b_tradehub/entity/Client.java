package com.logitrack.b2b_tradehub.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logitrack.b2b_tradehub.entity.enums.CustomerTier;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "email"})

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Clé étrangère One-to-One vers User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @JsonIgnore
    private User user;

    // --- Attributs de base (Simplifiés) ---
    @NotBlank(message = "Le nom est obligatoire.")
    @Size(max = 100)
    @Column(nullable = false)
    private String nom;

    @Email(message = "Format d'email invalide.")
    @NotBlank(message = "L'email est obligatoire.")
    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerTier tier;

    // --- Statistiques de fidélité ---
    @Min(0)
    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders = 0;

    @DecimalMin("0.0")
    @Column(name = "total_spent", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "first_order_date")
    private LocalDateTime firstOrderDate;

    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;

    // --- Timestamps & Soft Delete ---
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean deleted = false;

    // --- Relation One-to-Many avec Order ---
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    // --- Custom Constructor ---
    public Client(User user, String nom, String email, CustomerTier tier) {
        this.user = user;
        this.nom = nom;
        this.email = email;
        this.tier = tier;
        this.totalOrders = 0;
        this.totalSpent = BigDecimal.ZERO;
        this.deleted = false;
        this.onCreate();
    }

    // --- Lifecycle Callbacks ---
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.tier == null) this.tier = CustomerTier.BASIC;
        if (this.totalSpent == null) this.totalSpent = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}