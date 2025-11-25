package com.logitrack.b2b_tradehub.controller;


import com.logitrack.b2b_tradehub.dto.client.ClientCreateRequest;
import com.logitrack.b2b_tradehub.dto.client.ClientResponse;
import com.logitrack.b2b_tradehub.dto.client.ClientUpdateRequest;
import com.logitrack.b2b_tradehub.dto.order.OrderResponse;
import com.logitrack.b2b_tradehub.entity.enums.UserRole;
import com.logitrack.b2b_tradehub.service.ClientService;
import com.logitrack.b2b_tradehub.util.AuthUtil;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    /**
     * POST /api/clients - Créer un client (ADMIN only)
     */
    @PostMapping
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody ClientCreateRequest request, HttpSession session) {
        AuthUtil.checkRole(session, UserRole.ADMIN);
        ClientResponse response = clientService.createClient(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/clients - Lister tous les clients (ADMIN only)
     */
    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients(HttpSession session) {
        AuthUtil.checkRole(session, UserRole.ADMIN);
        List<ClientResponse> clients = clientService.findAll();
        return ResponseEntity.ok(clients);
    }

    /**
     * GET /api/clients/{id} - Consulter un client (ADMIN / CLIENT self)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable Long id, HttpSession session) {
        AuthUtil.checkClientOrAdmin(session, id);
        ClientResponse response = clientService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/clients/{id} - Mettre à jour un client (ADMIN / CLIENT self)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(@PathVariable Long id, @Valid @RequestBody ClientUpdateRequest request, HttpSession session) {
        AuthUtil.checkClientOrAdmin(session, id);
        ClientResponse response = clientService.updateClient(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/clients/{id} - Suppression d’un client (ADMIN only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id, HttpSession session) {
        AuthUtil.checkRole(session, UserRole.ADMIN);
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/clients/{id}/orders - Historique des commandes (ADMIN / CLIENT self)
     */
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderResponse>> getClientOrderHistory(@PathVariable Long id, HttpSession session) {
        AuthUtil.checkClientOrAdmin(session, id);
        List<OrderResponse> orders = clientService.findOrderHistory(id);
        return ResponseEntity.ok(orders);
    }
}