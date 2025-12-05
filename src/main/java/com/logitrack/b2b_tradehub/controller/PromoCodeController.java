package com.logitrack.b2b_tradehub.controller;

import com.logitrack.b2b_tradehub.dto.PromoCode.PromoCodeRequest;
import com.logitrack.b2b_tradehub.dto.PromoCode.PromoCodeResponse;
import com.logitrack.b2b_tradehub.service.PromoCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/promo-codes")
@CrossOrigin(origins = "*")
public class PromoCodeController {

    @Autowired
    private PromoCodeService promoCodeService;

    @GetMapping
    public ResponseEntity<List<PromoCodeResponse>> getAllPromoCodes() {
        return ResponseEntity.ok(promoCodeService.findAllResponse());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<PromoCodeResponse> getPromoCodeByCode(@PathVariable String code) {
        return ResponseEntity.ok(promoCodeService.findByCodeResponse(code));
    }

    @PostMapping
    public ResponseEntity<PromoCodeResponse> createPromoCode(@Valid @RequestBody PromoCodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promoCodeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromoCodeResponse> updatePromoCode(@PathVariable Long id, @Valid @RequestBody PromoCodeRequest request) {
        return ResponseEntity.ok(promoCodeService.update(id, request));
    }

    // Requirement: Soft Delete / Deactivation
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePromoCode(@PathVariable Long id) {
        promoCodeService.deactivatePromoCode(id);
        return ResponseEntity.ok().build();
    }

    // Helper for Frontend/Validation
    @GetMapping("/calculate-discount/{code}")
    public ResponseEntity<BigDecimal> calculateDiscount(@PathVariable String code, @RequestParam BigDecimal orderAmount) {
        return ResponseEntity.ok(promoCodeService.calculateDiscount(code, orderAmount));
    }
}