package com.logitrack.b2b_tradehub.dto.client;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ClientUpdateRequest {
    private String nom;

    @Email(message = "Format d'email invalide")
    private String email;

}