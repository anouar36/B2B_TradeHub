package com.logitrack.b2b_tradehub.service;

import com.logitrack.b2b_tradehub.dto.product.ProductRequest;
import com.logitrack.b2b_tradehub.dto.product.ProductResponse;
import com.logitrack.b2b_tradehub.entity.Product;
import com.logitrack.b2b_tradehub.exception.ResourceNotFoundException;
import com.logitrack.b2b_tradehub.mapper.ProductMapper;
import com.logitrack.b2b_tradehub.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductRepository productRepository;

    // Requirement: EF 3 (List Products with filters)
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(productMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return productMapper.toResponse(productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found")));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAvailableProducts() {
        return productRepository.findByDeletedFalse().stream().map(productMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findByNomContaining(String nom) {
        return productRepository.findByNomContaining(nom).stream().map(productMapper::toResponse).collect(Collectors.toList());
    }

    // Requirement: EF 3 (Add/Modify Product)
    @Transactional
    public ProductResponse create(ProductRequest request) {
        return productMapper.toResponse(productRepository.save(productMapper.toEntity(request)));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        productMapper.updateEntity(product, request);
        return productMapper.toResponse(productRepository.save(product));
    }

    // Requirement: EF 3 (Soft Delete)
    @Transactional
    public void softDelete(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setDeleted(true);
        productRepository.save(product);
    }

    // Requirement: EF 6 (Stock Validation)
    @Transactional(readOnly = true)
    public boolean checkStock(Long id, Integer quantity) {
        return productRepository.findById(id)
                .map(p -> p.getStockDisponible() >= quantity && !p.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    // Requirement: EF 4 (Update Stock after Order)
    @Transactional
    public void updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        int newStock = product.getStockDisponible() - quantity;
        if (newStock < 0) throw new RuntimeException("Insufficient stock");
        product.setStockDisponible(newStock);
        productRepository.save(product);
    }
}