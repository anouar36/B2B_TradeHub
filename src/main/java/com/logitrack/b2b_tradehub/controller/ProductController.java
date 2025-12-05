package com.logitrack.b2b_tradehub.controller;

import com.logitrack.b2b_tradehub.dto.product.ProductRequest;
import com.logitrack.b2b_tradehub.dto.product.ProductResponse;
import com.logitrack.b2b_tradehub.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Requirement: Consulter la liste des produits
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    // Requirement: Filtres (Consulter la liste avec filtres)
    @GetMapping("/available")
    public ResponseEntity<List<ProductResponse>> getAvailableProducts() {
        return ResponseEntity.ok(productService.findAvailableProducts());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProductsByName(@RequestParam String nom) {
        return ResponseEntity.ok(productService.findByNomContaining(nom));
    }

    // Requirement: Ajouter des produits
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse savedProduct = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    // Requirement: Modifier les informations produits
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    // Requirement: Supprimer des produits (soft delete)
    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<Void> softDeleteProduct(@PathVariable Long id) {
        productService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    // Helper: Check stock availability (Useful for UI/Validation, though enforced in Order Service)
    @GetMapping("/{id}/check-stock")
    public ResponseEntity<Boolean> checkStock(@PathVariable Long id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(productService.checkStock(id, quantity));
    }
}