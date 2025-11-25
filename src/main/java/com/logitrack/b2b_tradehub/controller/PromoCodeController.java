package com.logitrack.b2b_tradehub.controller;

import com.logitrack.b2b_tradehub.entity.PromoCode;
import com.logitrack.b2b_tradehub.service.PromoCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/promo-codes")
@CrossOrigin(origins = "*")
public class PromoCodeController {

    @Autowired
    private PromoCodeService promoCodeService;

    @GetMapping
    public ResponseEntity<List<PromoCode>> getAllPromoCodes() {
        List<PromoCode> promoCodes = promoCodeService.findAll();
        return ResponseEntity.ok(promoCodes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromoCode> getPromoCodeById(@PathVariable Long id) {
        Optional<PromoCode> promoCode = promoCodeService.findById(id);
        return promoCode.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<PromoCode> getPromoCodeByCode(@PathVariable String code) {
        Optional<PromoCode> promoCode = promoCodeService.findByCode(code);
        return promoCode.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<PromoCode>> getActivePromoCodes() {
        List<PromoCode> promoCodes = promoCodeService.findActivePromoCodes();
        return ResponseEntity.ok(promoCodes);
    }

    @GetMapping("/valid")
    public ResponseEntity<List<PromoCode>> getValidPromoCodes() {
        List<PromoCode> promoCodes = promoCodeService.findValidPromoCodes(LocalDate.now());
        return ResponseEntity.ok(promoCodes);
    }

    @PostMapping
    public ResponseEntity<PromoCode> createPromoCode(@Valid @RequestBody PromoCode promoCode) {
        PromoCode savedPromoCode = promoCodeService.save(promoCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPromoCode);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromoCode> updatePromoCode(@PathVariable Long id, @Valid @RequestBody PromoCode promoCodeDetails) {
        Optional<PromoCode> promoCodeOpt = promoCodeService.findById(id);
        if (promoCodeOpt.isPresent()) {
            PromoCode promoCode = promoCodeOpt.get();
            promoCode.setCode(promoCodeDetails.getCode());
            promoCode.setDiscountPercentage(promoCodeDetails.getDiscountPercentage());
            promoCode.setValidFrom(promoCodeDetails.getValidFrom());
            promoCode.setValidUntil(promoCodeDetails.getValidUntil());
            promoCode.setUsageUnique(promoCodeDetails.getUsageUnique());
            promoCode.setActive(promoCodeDetails.getActive());
            PromoCode updatedPromoCode = promoCodeService.save(promoCode);
            return ResponseEntity.ok(updatedPromoCode);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromoCode(@PathVariable Long id) {
        if (promoCodeService.findById(id).isPresent()) {
            promoCodeService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<Boolean> validatePromoCode(@PathVariable String code) {
        boolean isValid = promoCodeService.isPromoCodeValid(code);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/calculate-discount/{code}")
    public ResponseEntity<BigDecimal> calculateDiscount(@PathVariable String code, @RequestParam BigDecimal orderAmount) {
        BigDecimal discount = promoCodeService.calculateDiscount(code, orderAmount);
        return ResponseEntity.ok(discount);
    }

    @PatchMapping("/mark-used/{code}")
    public ResponseEntity<Void> markPromoCodeAsUsed(@PathVariable String code) {
        promoCodeService.markAsUsed(code);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePromoCode(@PathVariable Long id) {
        if (promoCodeService.findById(id).isPresent()) {
            promoCodeService.deactivatePromoCode(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
