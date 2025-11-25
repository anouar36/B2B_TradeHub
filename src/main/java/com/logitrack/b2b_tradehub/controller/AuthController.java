package com.logitrack.b2b_tradehub.controller;


import com.logitrack.b2b_tradehub.dto.user.LoginRequest;

import com.logitrack.b2b_tradehub.dto.user.UserResponse;
import com.logitrack.b2b_tradehub.entity.User;
import com.logitrack.b2b_tradehub.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService; // Supposons un service pour la validation

    public static final String SESSION_USER_KEY = "CURRENT_USER";


    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        User user = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

        session.setAttribute(SESSION_USER_KEY, user);

        UserResponse response = UserResponse.fromEntity(user); // Utiliser MapStruct ou une méthode statique
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/logout
     * Invalide la session HTTP.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/auth/me
     * Renvoie le profil de l'utilisateur connecté.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            throw new RuntimeException("Non authentifié");
        }
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}