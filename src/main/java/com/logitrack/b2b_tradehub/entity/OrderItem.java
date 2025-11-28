package com.logitrack.b2b_tradehub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Min(1)
    @Column(nullable = false)
    private Integer quantite;
    
    @DecimalMin("0.0")
    @Column(name = "prix_unitaire_ht", precision = 10, scale = 2, nullable = false)
    private BigDecimal prixUnitaireHT;
    
    @DecimalMin("0.0")
    @Column(name = "total_ligne", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalLigne;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    // Constructors
    public OrderItem() {
    }
    
    public OrderItem(Order order, Product product, Integer quantite, BigDecimal prixUnitaireHT) {
        this.order = order;
        this.product = product;
        this.quantite = quantite;
        this.prixUnitaireHT = prixUnitaireHT;
        calculateTotal();
    }
    
    // Business methods
    private void calculateTotal() {
        this.totalLigne = this.prixUnitaireHT.multiply(BigDecimal.valueOf(this.quantite));
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getQuantite() {
        return quantite;
    }
    
    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
        calculateTotal();
    }
    
    public BigDecimal getPrixUnitaireHT() {
        return prixUnitaireHT;
    }
    
    public void setPrixUnitaireHT(BigDecimal prixUnitaireHT) {
        this.prixUnitaireHT = prixUnitaireHT;
        calculateTotal();
    }
    
    public BigDecimal getTotalLigne() {
        return totalLigne;
    }
    
    public void setTotalLigne(BigDecimal totalLigne) {
        this.totalLigne = totalLigne;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
}
