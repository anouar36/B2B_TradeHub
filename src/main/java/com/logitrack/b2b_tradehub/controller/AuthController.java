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

    private final UserService userService;
    public static final String SESSION_USER_KEY = "CURRENT_USER";

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        User user = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        session.setAttribute(SESSION_USER_KEY, user);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            throw new RuntimeException("Non authentifié"); // Should be handled by global exception handler (401)
        }
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}