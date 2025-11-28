package com.logitrack.b2b_tradehub.service;

import com.logitrack.b2b_tradehub.dto.PromoCode.PromoCodeRequest;
import com.logitrack.b2b_tradehub.dto.PromoCode.PromoCodeResponse;
import com.logitrack.b2b_tradehub.entity.PromoCode;
import com.logitrack.b2b_tradehub.exception.ResourceNotFoundException;
import com.logitrack.b2b_tradehub.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    @Transactional(readOnly = true)
    public List<PromoCodeResponse> findAllResponse() {
        return promoCodeRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public PromoCodeResponse create(PromoCodeRequest request) {
        PromoCode entity = new PromoCode(); // Map from request manually or use mapper
        entity.setCode(request.getCode());
        entity.setDiscountPercentage(request.getDiscountPercentage());
        entity.setValidFrom(request.getValidFrom());
        entity.setValidUntil(request.getValidUntil());
        entity.setUsageUnique(request.getUsageUnique());
        entity.setActive(true);
        return mapToResponse(promoCodeRepository.save(entity));
    }

    @Transactional
    public PromoCodeResponse update(Long id, PromoCodeRequest request) {
        PromoCode entity = promoCodeRepository.findById(id).orElseThrow();
        entity.setCode(request.getCode());
        // ... update other fields
        return mapToResponse(promoCodeRepository.save(entity));
    }

    public void deactivatePromoCode(Long id) {
        PromoCode p = promoCodeRepository.findById(id).orElseThrow();
        p.setActive(false);
        promoCodeRepository.save(p);
    }

    public BigDecimal calculateDiscount(String code, BigDecimal amount) {
        return promoCodeRepository.findByCode(code)
                .filter(this::isValid)
                .map(p -> amount.multiply(p.getDiscountPercentage()).divide(new BigDecimal("100")))
                .orElse(BigDecimal.ZERO);
    }

    public void markAsUsed(String code) {
        promoCodeRepository.findByCode(code).ifPresent(p -> {
            if(p.getUsageUnique()) {
                p.setIsUsed(true);
                promoCodeRepository.save(p);
            }
        });
    }

    private boolean isValid(PromoCode p) {
        LocalDate now = LocalDate.now();
        return p.getActive() && !now.isBefore(p.getValidFrom()) && !now.isAfter(p.getValidUntil()) && (!p.getUsageUnique() || !p.getIsUsed());
    }

    public PromoCodeResponse findByCodeResponse(String code) {
        return mapToResponse(promoCodeRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Code not found")));
    }

    private PromoCodeResponse mapToResponse(PromoCode entity) {
        // Simple manual mapper logic to return DTO
        PromoCodeResponse response = new PromoCodeResponse();
        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setDiscountPercentage(entity.getDiscountPercentage());
        // set other fields...
        return response;
    }
}