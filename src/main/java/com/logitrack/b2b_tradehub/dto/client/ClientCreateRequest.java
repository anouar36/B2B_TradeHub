package com.logitrack.b2b_tradehub.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientCreateRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    // Champs liés à l'utilisateur pour l'association
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    private String username;

    @Size(min = 6, message = "Le mot de passe doit avoir au moins 6 caractères")
    private String password;

}