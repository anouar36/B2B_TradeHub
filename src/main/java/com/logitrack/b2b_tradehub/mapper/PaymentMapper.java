package com.logitrack.b2b_tradehub.mapper;

import com.logitrack.b2b_tradehub.dto.payment.PaymentRequest;
import com.logitrack.b2b_tradehub.dto.payment.PaymentResponse;
import com.logitrack.b2b_tradehub.entity.Payment;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    // Entity -> Response DTO
    @Mapping(source = "order.id", target = "orderId")
    PaymentResponse toResponse(Payment payment);

    // Request DTO -> Entity
    // We ignore 'order' here because we will set it manually in the Service
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "EN_ATTENTE") // Default status
    @Mapping(target = "numeroPaiement", expression = "java(java.util.UUID.randomUUID().toString())") // Simple generation
    Payment toEntity(PaymentRequest request);

    // Update existing Entity from Request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true) // Don't change order on update usually
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget Payment payment, PaymentRequest request);
}