package com.logitrack.b2b_tradehub.service;

import com.logitrack.b2b_tradehub.dto.client.ClientCreateRequest;
import com.logitrack.b2b_tradehub.dto.client.ClientResponse;
import com.logitrack.b2b_tradehub.dto.client.ClientUpdateRequest;
import com.logitrack.b2b_tradehub.dto.order.OrderResponse;
import com.logitrack.b2b_tradehub.entity.Client;
import com.logitrack.b2b_tradehub.entity.User;
import com.logitrack.b2b_tradehub.entity.enums.CustomerTier;
import com.logitrack.b2b_tradehub.entity.enums.UserRole;
import com.logitrack.b2b_tradehub.exception.ResourceNotFoundException;
import com.logitrack.b2b_tradehub.mapper.ClientMapper;
import com.logitrack.b2b_tradehub.mapper.OrderMapper;
import com.logitrack.b2b_tradehub.repository.ClientRepository;
import com.logitrack.b2b_tradehub.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ClientMapper clientMapper;
    private final OrderMapper orderMapper;

    // Requirement: EF 2.1 (Loyalty Thresholds)
    private static final int SILVER_ORDERS = 3;
    private static final BigDecimal SILVER_SPENT = new BigDecimal("1000.00");
    private static final int GOLD_ORDERS = 10;
    private static final BigDecimal GOLD_SPENT = new BigDecimal("5000.00");
    private static final int PLATINUM_ORDERS = 20;
    private static final BigDecimal PLATINUM_SPENT = new BigDecimal("15000.00");

    // Requirement: EF 1.1 (Create Client + User)
    @Transactional
    public ClientResponse createClient(ClientCreateRequest request) {
        User newUser = userService.createUser(request.getUsername(), request.getPassword(), UserRole.CLIENT);
        Client client = clientMapper.toEntity(request);
        client.setUser(newUser);
        client.setTier(CustomerTier.BASIC);
        Client savedClient = clientRepository.save(client);
        return clientMapper.toResponse(savedClient);
    }

    // Requirement: EF 1.2 (View Client)
    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));
        return clientMapper.toResponse(client);
    }

    // Requirement: EF 1.2 (Update Client)
    @Transactional
    public ClientResponse updateClient(Long id, ClientUpdateRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));
        clientMapper.updateEntityFromDto(request, client);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    // Requirement: EF 1.4 (Delete Client - Soft Delete if orders exist)
    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));
        if (client.getOrders() != null && !client.getOrders().isEmpty()) {
            client.setDeleted(true);
            clientRepository.save(client);
        } else {
            clientRepository.delete(client);
        }
    }

    // Requirement: EF 1.3 (Order History)
    @Transactional(readOnly = true)
    public List<OrderResponse> findOrderHistory(Long clientId) {
        return orderRepository.findByClientIdOrderByDateCommandeDesc(clientId).stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Requirement: EF 1.2 (List All Clients - Admin)
    @Transactional(readOnly = true)
    public List<ClientResponse> findAll() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Requirement: EF 2.2, 4.4 (Update Stats & Loyalty Level)
    @Transactional
    public void updateClientStatsAndTier(Client client, BigDecimal orderTotal) {
        client.setTotalOrders(client.getTotalOrders() + 1);
        client.setTotalSpent(client.getTotalSpent().add(orderTotal));
        client.setLastOrderDate(LocalDateTime.now());
        if (client.getFirstOrderDate() == null) {
            client.setFirstOrderDate(client.getLastOrderDate());
        }

        // Recalculate Tier
        CustomerTier newTier = calculateNewTier(client);
        if (newTier.ordinal() > client.getTier().ordinal()) {
            client.setTier(newTier);
        }
        clientRepository.save(client);
    }

    private CustomerTier calculateNewTier(Client client) {
        int orders = client.getTotalOrders();
        BigDecimal spent = client.getTotalSpent();
        if (orders >= PLATINUM_ORDERS || spent.compareTo(PLATINUM_SPENT) >= 0) return CustomerTier.PLATINUM;
        if (orders >= GOLD_ORDERS || spent.compareTo(GOLD_SPENT) >= 0) return CustomerTier.GOLD;
        if (orders >= SILVER_ORDERS || spent.compareTo(SILVER_SPENT) >= 0) return CustomerTier.SILVER;
        return CustomerTier.BASIC;
    }
}