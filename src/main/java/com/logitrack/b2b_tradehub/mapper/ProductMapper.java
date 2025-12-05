package com.logitrack.b2b_tradehub.mapper;

import com.logitrack.b2b_tradehub.dto.product.ProductRequest;
import com.logitrack.b2b_tradehub.dto.product.ProductResponse;
import com.logitrack.b2b_tradehub.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    // Request DTO -> Entity (For Create)
    public Product toEntity(ProductRequest request) {
        if (request == null) return null;
        return Product.builder()
                .nom(request.getNom())
                .prixUnitaireHT(request.getPrixUnitaireHT())
                .stockDisponible(request.getStockDisponible())
                .build();
    }

    // Entity -> Response DTO (For Read)
    public ProductResponse toResponse(Product product) {
        if (product == null) return null;
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setNom(product.getNom());
        response.setPrixUnitaireHT(product.getPrixUnitaireHT());
        response.setStockDisponible(product.getStockDisponible());
        response.setCreatedAt(product.getCreatedAt());
        response.setDeleted(product.getDeleted());
        return response;
    }

    // DTO -> Existing Entity (For Update)
    public void updateEntity(Product existingProduct, ProductRequest request) {
        if (request == null || existingProduct == null) return;

        existingProduct.setNom(request.getNom());
        existingProduct.setPrixUnitaireHT(request.getPrixUnitaireHT());
        existingProduct.setStockDisponible(request.getStockDisponible());
    }
}