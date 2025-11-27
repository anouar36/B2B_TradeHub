package com.logitrack.b2b_tradehub.mapper;

import com.logitrack.b2b_tradehub.dto.order.OrderCreateRequest;
import com.logitrack.b2b_tradehub.dto.order.OrderResponse;
import com.logitrack.b2b_tradehub.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    // --- TO ENTITY ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)     // يتم التعامل معه في الـ Service
    @Mapping(target = "promoCode", ignore = true)  // يتم التعامل معه في الـ Service
    @Mapping(target = "orderItems", ignore = true) // القائمة في Entity اسمها orderItems
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "payments", ignore = true)

    // ربط التاريخ من DTO (orderDate) إلى Entity (dateCommande)
    @Mapping(target = "dateCommande", source = "orderDate")
    Order toEntity(OrderCreateRequest dto);


    // --- TO RESPONSE ---
    @Mapping(target = "clientId", source = "client.id")

    // ربط القائمة: Entity (orderItems) -> DTO (items)
    @Mapping(target = "items", source = "orderItems")

    // ربط كود الخصم
    @Mapping(target = "codePromo", source = "promoCodeId")

    // ربط الحسابات المالية (اختلاف الأسماء)
    @Mapping(target = "sousTotal", source = "sousTotalHT")
    @Mapping(target = "montantRemise", source = "montantRemiseTotale")

    OrderResponse toResponse(Order order);
}