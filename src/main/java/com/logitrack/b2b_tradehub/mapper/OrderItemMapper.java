package com.logitrack.b2b_tradehub.mapper;

import com.logitrack.b2b_tradehub.dto.orderItem.OrderItemResponse;
import com.logitrack.b2b_tradehub.entity.OrderItem;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for manual conversion between OrderItem Entity and OrderItemResponse DTO.
 * Used primarily by the OrderMapper to handle nested item lists.
 */
public final class OrderItemMapper {

    // Private constructor to prevent instantiation of the utility class
    private OrderItemMapper() {}

    /**
     * Converts a single OrderItem Entity to OrderItemResponse DTO.
     * Maps the essential product details (ID, Name) and line calculations.
     * @param item The source OrderItem entity.
     * @return The resulting OrderItemResponse DTO.
     */
    public static OrderItemResponse toResponse(OrderItem item) {
        if (item == null) {
            return null;
        }

        OrderItemResponse dto = new OrderItemResponse();

        // --- Core Item Data ---
        dto.setId(item.getId());
        dto.setQuantite(item.getQuantite());
        dto.setPrixUnitaireHT(item.getPrixUnitaireHT());
        dto.setTotalLigne(item.getTotalLigne());

        // --- Product Reference (Nested Data) ---
        // Ensure the Product entity is accessible from the OrderItem (eagerly or lazily loaded)
        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getNom());
        }

        return dto;
    }

    /**
     * Converts a list of OrderItem Entities to a list of OrderItemResponse DTOs.
     * @param items List of OrderItem entities.
     * @return List of OrderItemResponse DTOs.
     */
    public static List<OrderItemResponse> toResponseList(List<OrderItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(OrderItemMapper::toResponse)
                .collect(Collectors.toList());
    }
}