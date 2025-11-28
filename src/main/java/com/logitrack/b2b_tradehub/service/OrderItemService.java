package com.logitrack.b2b_tradehub.service;

import com.logitrack.b2b_tradehub.entity.Order;
import com.logitrack.b2b_tradehub.entity.OrderItem;
import com.logitrack.b2b_tradehub.entity.Product;
import com.logitrack.b2b_tradehub.exception.BusinessValidationException;
import com.logitrack.b2b_tradehub.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    // Requirement: EF 4 (Create Order) - Helper to create items
    @Transactional
    public OrderItem create(Order order, Product product, Integer quantite, BigDecimal prixUnitaireHT) {
        if (quantite <= 0) {
            throw new BusinessValidationException("Quantity must be greater than 0.");
        }
        if (prixUnitaireHT.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessValidationException("Price cannot be negative.");
        }

        OrderItem orderItem = new OrderItem(order, product, quantite, prixUnitaireHT);
        // Total line calculation is usually handled inside OrderItem constructor or setter
        // Assuming OrderItem entity handles this or:
        orderItem.setTotalLigne(prixUnitaireHT.multiply(new BigDecimal(quantite)));

        return orderItemRepository.save(orderItem);
    }


}