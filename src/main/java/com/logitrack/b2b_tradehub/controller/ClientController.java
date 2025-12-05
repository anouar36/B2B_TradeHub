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

    // Requirement: Créer un client
    @PostMapping
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody ClientCreateRequest request, HttpSession session) {
        AuthUtil.checkRole(session, UserRole.ADMIN);
        ClientResponse response = clientService.createClient(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Requirement: Consulter les informations (List)
    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients(HttpSession session) {
        AuthUtil.checkRole(session, UserRole.ADMIN);
        return ResponseEntity.ok(clientService.findAll());
    }

    // Requirement: Consulter les informations d'un client (Specific)
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable Long id, HttpSession session) {
        AuthUtil.checkClientOrAdmin(session, id);
        return ResponseEntity.ok(clientService.findById(id));
    }

    // Requirement: Mettre à jour les données d’un client
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(@PathVariable Long id, @Valid @RequestBody ClientUpdateRequest request, HttpSession session) {
        AuthUtil.checkClientOrAdmin(session, id);
        return ResponseEntity.ok(clientService.updateClient(id, request));
    }

    // Requirement: Suppression d’un client
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id, HttpSession session) {
        AuthUtil.checkRole(session, UserRole.ADMIN);
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    // Requirement: Consulter l'historique des commandes
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderResponse>> getClientOrderHistory(@PathVariable Long id, HttpSession session) {
        AuthUtil.checkClientOrAdmin(session, id);
        return ResponseEntity.ok(clientService.findOrderHistory(id));
    }
}