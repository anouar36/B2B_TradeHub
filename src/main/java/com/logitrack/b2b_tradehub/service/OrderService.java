package com.logitrack.b2b_tradehub.service;

import com.logitrack.b2b_tradehub.dto.order.OrderCreateRequest;
import com.logitrack.b2b_tradehub.dto.order.OrderResponse;
import com.logitrack.b2b_tradehub.dto.orderItem.OrderItemRequest;
import com.logitrack.b2b_tradehub.entity.*;
import com.logitrack.b2b_tradehub.entity.enums.OrderStatus;
import com.logitrack.b2b_tradehub.exception.BusinessValidationException;
import com.logitrack.b2b_tradehub.exception.ResourceNotFoundException;
import com.logitrack.b2b_tradehub.mapper.OrderMapper;
import com.logitrack.b2b_tradehub.repository.ClientRepository;
import com.logitrack.b2b_tradehub.repository.OrderRepository;
import com.logitrack.b2b_tradehub.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final OrderItemService orderItemService;
    private final ClientService clientService; // For updating loyalty
    private final ProductService productService; // For stock management
    private final PromoCodeService promoCodeService; // For discount calculation
    private final OrderMapper orderMapper;

    // --- READ OPERATIONS ---

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream().map(orderMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return orderMapper.toResponse(findOrderEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream().map(orderMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findPendingOrders() {
        return findByStatus(OrderStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByDateCommandeBetween(startDate, endDate).stream()
                .map(orderMapper::toResponse).collect(Collectors.toList());
    }

    // --- CORE BUSINESS LOGIC (Create, Confirm, Cancel) ---

    // Requirement: EF 4 (Create Order)
    @Transactional
    public OrderResponse createOrderFlow(OrderCreateRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Order order = new Order();
        order.setClient(client);
        order.setStatus(OrderStatus.PENDING);
        order.setDateCommande(request.getOrderDate() != null ? request.getOrderDate() : LocalDateTime.now());

        // Save first to generate ID
        order = orderRepository.save(order);

        BigDecimal sousTotal = BigDecimal.ZERO;
        List<OrderItem> createdItems = new ArrayList<>();

        // Inside createOrderFlow method...

        for (OrderItemRequest itemDto : request.getItems()) {
            // 1. Check Stock
            if (!productService.checkStock(itemDto.getProductId(), itemDto.getQuantite())) {
                throw new BusinessValidationException("Insufficient stock for product ID: " + itemDto.getProductId());
            }

            // 2. Fetch Product (To get the REAL Price)
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            // 3. Create Item using PRODUCT PRICE, not DTO price
            OrderItem orderItem = orderItemService.create(
                    order,
                    product,
                    itemDto.getQuantite(),
                    product.getPrixUnitaireHT() // <--- CHANGE THIS: Use product.getPrixUnitaireHT()
            );

            createdItems.add(orderItem);
            sousTotal = sousTotal.add(orderItem.getTotalLigne());
        }

        order.setOrderItems(createdItems);
        order.setSousTotalHT(sousTotal);

        // Requirement: EF 4 (Apply Discounts & VAT)
        applyDiscountsAndCalculateTotal(order, request.getPromoCode());

        return orderMapper.toResponse(orderRepository.save(order));
    }

    private void applyDiscountsAndCalculateTotal(Order order, String promoCodeInput) {
        BigDecimal discountTotal = BigDecimal.ZERO;

        // 1. Loyalty Discount
        BigDecimal loyaltyDiscount = calculateLoyaltyDiscount(order.getClient(), order.getSousTotalHT());
        discountTotal = discountTotal.add(loyaltyDiscount);

        // 2. Promo Code Discount
        if (promoCodeInput != null && !promoCodeInput.isEmpty()) {
            BigDecimal promoDiscount = promoCodeService.calculateDiscount(promoCodeInput, order.getSousTotalHT());
            if (promoDiscount.compareTo(BigDecimal.ZERO) > 0) {
                order.setPromoCodeId(promoCodeInput);
                discountTotal = discountTotal.add(promoDiscount);
            }
        }

        order.setMontantRemiseTotale(discountTotal);
        order.setTauxTVA(new BigDecimal("20.0")); // Default 20%
        order.calculateTotals(); // Calculates HT after discount, TVA, TTC
    }

    private BigDecimal calculateLoyaltyDiscount(Client client, BigDecimal amount) {
        // Requirement: EF 2 (Loyalty System)
        switch (client.getTier()) {
            case SILVER: return amount.compareTo(new BigDecimal("500")) >= 0 ? amount.multiply(new BigDecimal("0.05")) : BigDecimal.ZERO;
            case GOLD: return amount.compareTo(new BigDecimal("800")) >= 0 ? amount.multiply(new BigDecimal("0.10")) : BigDecimal.ZERO;
            case PLATINUM: return amount.compareTo(new BigDecimal("1200")) >= 0 ? amount.multiply(new BigDecimal("0.15")) : BigDecimal.ZERO;
            default: return BigDecimal.ZERO;
        }
    }

    // Requirement: EF 4 (Confirm Order - Validation par ADMIN)
    @Transactional
    public OrderResponse confirmOrder(Long orderId) {
        Order order = findOrderEntityById(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessValidationException("Only PENDING orders can be confirmed.");
        }
        // Requirement: EF 5 (Payment Complete Check)
        if (order.getMontantRestant().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessValidationException("Cannot confirm: Payment incomplete. Remaining: " + order.getMontantRestant());
        }

        // Requirement: EF 4 (Update Stock & Stats)
        for (OrderItem item : order.getOrderItems()) {
            productService.updateStock(item.getProduct().getId(), item.getQuantite());
        }
        clientService.updateClientStatsAndTier(order.getClient(), order.getTotalTTC());

        // Mark promo code used if applicable
        if(order.getPromoCodeId() != null) {
            promoCodeService.markAsUsed(order.getPromoCodeId());
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    // Requirement: EF 4 (Cancel Order)
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = findOrderEntityById(orderId);
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            // Requirement: "Statuts finaux... aucune modification possible" implies cancelled from Pending mostly.
            // But if logic allows cancelling confirmed, we must restore stock.
            // "CANCELED : annulée manuellement par ADMIN (uniquement si PENDING)" -> Strict rule in EF 4.
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new BusinessValidationException("Only PENDING orders can be cancelled.");
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    // Helper for PaymentService
    @Transactional
    public void updatePaymentAmount(Long orderId, BigDecimal paymentAmount) {
        Order order = findOrderEntityById(orderId);
        BigDecimal newRestant = order.getMontantRestant().subtract(paymentAmount);
        if (newRestant.compareTo(BigDecimal.ZERO) < 0) newRestant = BigDecimal.ZERO;
        order.setMontantRestant(newRestant);
        orderRepository.save(order);
    }

    // Helper to apply promo (if needed separately, though usually done at Create)
    @Transactional
    public void applyPromoCode(Long orderId, String promoCode) {
        Order order = findOrderEntityById(orderId);
        applyDiscountsAndCalculateTotal(order, promoCode);
        orderRepository.save(order);
    }

    // Analytics Helpers
    @Transactional(readOnly = true)
    public BigDecimal getTotalOrderValue(Long clientId) {
        return orderRepository.findByClientId(clientId).stream()
                .map(Order::getTotalTTC).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public Integer getOrderCount(Long clientId) {
        return orderRepository.countByClientId(clientId);
    }

    // For internal use
    public void deleteById(Long id) {
        // Only if absolutely necessary for cleanup of empty/test orders
        orderRepository.deleteById(id);
    }

    private Order findOrderEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
}