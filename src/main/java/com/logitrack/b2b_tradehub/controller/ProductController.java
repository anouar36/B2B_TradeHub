package com.logitrack.b2b_tradehub.controller;

import com.logitrack.b2b_tradehub.entity.Product;
import com.logitrack.b2b_tradehub.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        return product.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/available")
    public ResponseEntity<List<Product>> getAvailableProducts() {
        List<Product> products = productService.findAvailableProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/in-stock")
    public ResponseEntity<List<Product>> getProductsInStock() {
        List<Product> products = productService.findInStock();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProductsByName(@RequestParam String nom) {
        List<Product> products = productService.findByNomContaining(nom);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productService.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productDetails) {
        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setNom(productDetails.getNom());
            product.setPrixUnitaireHT(productDetails.getPrixUnitaireHT());
            product.setStockDisponible(productDetails.getStockDisponible());
            Product updatedProduct = productService.save(product);
            return ResponseEntity.ok(updatedProduct);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.findById(id).isPresent()) {
            productService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<Void> softDeleteProduct(@PathVariable Long id) {
        if (productService.findById(id).isPresent()) {
            productService.softDelete(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/check-stock")
    public ResponseEntity<Boolean> checkStock(@PathVariable Long id, @RequestParam Integer quantity) {
        boolean hasStock = productService.checkStock(id, quantity);
        return ResponseEntity.ok(hasStock);
    }

    @PatchMapping("/{id}/update-stock")
    public ResponseEntity<Void> updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        if (productService.findById(id).isPresent()) {
            productService.updateStock(id, quantity);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/restore-stock")
    public ResponseEntity<Void> restoreStock(@PathVariable Long id, @RequestParam Integer quantity) {
        if (productService.findById(id).isPresent()) {
            productService.restoreStock(id, quantity);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
