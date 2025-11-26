package com.logitrack.b2b_tradehub.service;

import com.logitrack.b2b_tradehub.dto.client.ClientCreateRequest;
import com.logitrack.b2b_tradehub.dto.client.ClientResponse;
import com.logitrack.b2b_tradehub.dto.client.ClientUpdateRequest;
import com.logitrack.b2b_tradehub.dto.order.OrderResponse;
import com.logitrack.b2b_tradehub.entity.Client;
import com.logitrack.b2b_tradehub.entity.enums.CustomerTier;
import com.logitrack.b2b_tradehub.entity.User;
import com.logitrack.b2b_tradehub.entity.enums.UserRole;
import com.logitrack.b2b_tradehub.exception.ResourceNotFoundException;
import com.logitrack.b2b_tradehub.mapper.ClientMapper;
import com.logitrack.b2b_tradehub.mapper.OrderMapper;
import com.logitrack.b2b_tradehub.repository.ClientRepository;
import com.logitrack.b2b_tradehub.repository.OrderRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final ClientMapper clientMapper; // MapStruct
    private final OrderMapper orderMapper; // MapStruct

    private static final int SILVER_ORDERS = 3;
    private static final BigDecimal SILVER_SPENT = new BigDecimal("1000.00");
    private static final int GOLD_ORDERS = 10;
    private static final BigDecimal GOLD_SPENT = new BigDecimal("5000.00");
    private static final int PLATINUM_ORDERS = 20;
    private static final BigDecimal PLATINUM_SPENT = new BigDecimal("15000.00");

    /**
     * EF 1.1: Créer un client (opération transactionnelle User + Client).
     */
    @Transactional
    public ClientResponse createClient(ClientCreateRequest request) {
        User newUser = userService.createUser(request.getUsername(), request.getPassword(), UserRole.CLIENT);

        Client client = modelMapper.map(request,Client.class);
        client.setUser(newUser);
        client.setTier(CustomerTier.BASIC);

        Client savedClient = clientRepository.save(client);

        // 3. Mettre à jour la référence bidirectionnelle dans User (important pour la navigation)
        newUser.setClientProfile(savedClient);

        return modelMapper.map(savedClient,ClientResponse.class);
    }

    /**
     * EF 1.2: Consulter les informations d'un client.
     */
    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));
        return modelMapper.map(client,ClientResponse.class);
    }

    /**
     * EF 1.2: Mettre à jour les données d’un client.
     */
    @Transactional
    public ClientResponse updateClient(Long id, ClientUpdateRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        // Utiliser le mapper pour mettre à jour les champs
            clientMapper.updateEntityFromDto(request, client);

        Client updatedClient = clientRepository.save(client);
        return clientMapper.toResponse(updatedClient);
    }

    /**
     * EF 1.4: Suppression d’un client (soft delete si commandes existantes).
     */
    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        // Si des commandes existent, effectuer un soft delete
        if (!client.getOrders().isEmpty()) {
            client.setDeleted(true);
            clientRepository.save(client);
        } else {
            // Sinon, suppression physique du Client et de l'User associé (via CascadeType.ALL)
            clientRepository.delete(client);
        }
    }

    /**
     * EF 1.3: Consulter l'historique des commandes.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> findOrderHistory(Long clientId) {
        // Le DTO OrderResponse doit contenir les champs requis (ID, Date, Total TTC, Statut)
        return orderRepository.findByClientIdOrderByDateCommandeDesc(clientId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    /**
     * EF 1.2: Lister tous les clients (pour l'ADMIN).
     */
    @Transactional(readOnly = true)
    public List<ClientResponse> findAll() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    // --- Logique Métier Complexe (Système de Fidélité) ---

    /**
     * EF 1.2, 2.2, 4.4: Mise à jour des statistiques client et recalcul du niveau après confirmation de commande.
     * Cette méthode est appelée par OrderService après qu'une commande soit CONFIRMED.
     * @param client Le client à mettre à jour.
     * @param orderTotal Le montant TTC de la commande confirmée.
     */
    @Transactional
    public void updateClientStatsAndTier(Client client, BigDecimal orderTotal) {
        client.setTotalOrders(client.getTotalOrders() + 1);
        client.setTotalSpent(client.getTotalSpent().add(orderTotal));
        client.setLastOrderDate(LocalDateTime.now());

        if (client.getFirstOrderDate() == null) {
            client.setFirstOrderDate(client.getLastOrderDate());
        }

        CustomerTier newTier = calculateNewTier(client);

        if (newTier.ordinal() > client.getTier().ordinal()) {
            client.setTier(newTier);
        }

        clientRepository.save(client);
    }

    /**
     * EF 2.1: Calcul automatique du niveau basé sur l'historique client.
     */
    private CustomerTier calculateNewTier(Client client) {
        int orders = client.getTotalOrders();
        BigDecimal spent = client.getTotalSpent();

        if (orders >= PLATINUM_ORDERS || spent.compareTo(PLATINUM_SPENT) >= 0) {
            return CustomerTier.PLATINUM;
        } else if (orders >= GOLD_ORDERS || spent.compareTo(GOLD_SPENT) >= 0) {
            return CustomerTier.GOLD;
        } else if (orders >= SILVER_ORDERS || spent.compareTo(SILVER_SPENT) >= 0) {
            return CustomerTier.SILVER;
        } else {
            return CustomerTier.BASIC;
        }
    }
}