package com.logitrack.b2b_tradehub.service;

import com.logitrack.b2b_tradehub.dto.order.OrderCreateRequest;
import com.logitrack.b2b_tradehub.dto.order.OrderResponse;
import com.logitrack.b2b_tradehub.dto.orderItem.OrderItemRequest;
import com.logitrack.b2b_tradehub.dto.orderItem.OrderItemResponse;
import com.logitrack.b2b_tradehub.entity.Client;
import com.logitrack.b2b_tradehub.entity.Order;
import com.logitrack.b2b_tradehub.entity.OrderItem;
import com.logitrack.b2b_tradehub.entity.Product;
import com.logitrack.b2b_tradehub.entity.enums.OrderStatus;
import com.logitrack.b2b_tradehub.exception.BusinessValidationException;
import com.logitrack.b2b_tradehub.exception.ResourceNotFoundException;
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

    // --- READ OPERATIONS (Return DTOs) ---

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByDateCommandeBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- WRITE OPERATIONS (Consume DTO, Return DTO) ---

    @Transactional
    public OrderResponse createOrderFlow(OrderCreateRequest request) {
        // 1. Fetch Client
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + request.getClientId()));

        // 2. Initialize Order (Empty initially)
        Order order = new Order();
        order.setClient(client);
        order.setStatus(OrderStatus.PENDING);
        order.setPromoCodeId(request.getPromoCode());
        order.setDateCommande(request.getOrderDate() != null ? request.getOrderDate() : LocalDateTime.now());

        // Initialize values to 0 to avoid DB null errors
        order.setSousTotalHT(BigDecimal.ZERO);
        order.setMontantRemiseTotale(BigDecimal.ZERO);
        order.setTauxTVA(new BigDecimal("20.0")); // Default TVA
        order.calculateTotals();

        // Save initial order to generate ID
        order = orderRepository.save(order);

        // 3. Process Items
        BigDecimal sousTotal = BigDecimal.ZERO;
        List<OrderItem> createdItems = new ArrayList<>();

        for (OrderItemRequest itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemDto.getProductId()));

            OrderItem orderItem = orderItemService.create(
                    order,
                    product,
                    itemDto.getQuantite(),
                    itemDto.getPrixUnitaireHT()
            );

            createdItems.add(orderItem);
            sousTotal = sousTotal.add(orderItem.getTotalLigne());
        }

        // 4. Update Order with real totals
        order.setSousTotalHT(sousTotal);
        order.setOrderItems(createdItems); // Ensure items are linked in memory for the response mapping
        order.calculateTotals();

        // 5. Save Final State
        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    @Transactional
    public OrderResponse confirmOrder(Long orderId) {
        Order order = findOrderEntityById(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessValidationException("Only PENDING orders can be confirmed. Current: " + order.getStatus());
        }
        if (order.getMontantRestant().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessValidationException("Payment incomplete. Remaining: " + order.getMontantRestant());
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());

        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = findOrderEntityById(orderId);

        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.REJECTED) {
            throw new BusinessValidationException("Cannot cancel order. It is already " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public void applyPromoCode(Long orderId, String promoCode) {
        Order order = findOrderEntityById(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessValidationException("Promo codes can only be applied to PENDING orders.");
        }
        order.setPromoCodeId(promoCode);
        orderRepository.save(order);
    }

    @Transactional
    public void updatePaymentAmount(Long orderId, BigDecimal paymentAmount) {
        Order order = findOrderEntityById(orderId);
        BigDecimal newRestant = order.getMontantRestant().subtract(paymentAmount);

        if (newRestant.compareTo(BigDecimal.ZERO) < 0) {
            newRestant = BigDecimal.ZERO;
        }
        order.setMontantRestant(newRestant);
        orderRepository.save(order);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete. Order not found with ID: " + id);
        }
        orderRepository.deleteById(id);
    }

    // --- ANALYTICS (Primitive types, no change needed) ---

    @Transactional(readOnly = true)
    public BigDecimal getTotalOrderValue(Long clientId) {
        List<Order> orders = orderRepository.findByClientId(clientId);
        if (orders == null || orders.isEmpty()) return BigDecimal.ZERO;
        return orders.stream().map(Order::getTotalTTC).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public Integer getOrderCount(Long clientId) {
        return orderRepository.countByClientId(clientId);
    }

    // --- INTERNAL HELPER METHODS ---

    // Keep this private to fetch entity internally
    private Order findOrderEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
    }

    // --- MAPPING LOGIC (Entity -> DTO) ---
    // You can move this to a dedicated Mapper class (e.g., MapStruct) later

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setClientId(order.getClient().getId());
        // response.setClientTierAtOrder(order.getClient().getTier()); // If available

        // Calculated Totals
        response.setSousTotal(order.getSousTotalHT());
        response.setMontantRemise(order.getMontantRemiseTotale());
        response.setMontantHTApresRemise(order.getSousTotalHT().subtract(order.getMontantRemiseTotale()));
        response.setTauxTVA(order.getTauxTVA());
        // response.setMontantTVA(order.getMontantTVA()); // If field exists or calculate it
        response.setTotalTTC(order.getTotalTTC());
        response.setMontantRestant(order.getMontantRestant());

        // Status & Dates
        response.setStatus(order.getStatus());
        response.setCodePromo(order.getPromoCodeId());
        response.setDateCommande(order.getDateCommande());
        // response.setCreatedAt(order.getCreatedAt()); // Assuming Auditable entity
        // response.setUpdatedAt(order.getUpdatedAt());
        response.setConfirmedAt(order.getConfirmedAt());

        // Items
        if (order.getOrderItems() != null) {
            List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                    .map(this::mapItemToResponse)
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        }

        return response;
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        // Assuming OrderItemResponse has these fields based on standard logic
        // You might need to adjust field names to match your OrderItemResponse exactly
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getNom()); // Assuming fetch logic
        response.setQuantite(item.getQuantite());
        response.setPrixUnitaireHT(item.getPrixUnitaireHT());
        response.setTotalLigne(item.getTotalLigne());
        return response;
    }
}