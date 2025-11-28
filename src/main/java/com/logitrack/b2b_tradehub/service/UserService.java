package com.logitrack.b2b_tradehub.service;

import com.logitrack.b2b_tradehub.entity.User;
import com.logitrack.b2b_tradehub.entity.enums.UserRole;
import com.logitrack.b2b_tradehub.exception.BusinessValidationException;
import com.logitrack.b2b_tradehub.exception.UnauthorizedException;
import com.logitrack.b2b_tradehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private String hashPassword(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to apply hashing algorithm (SHA-256).", e);
        }
    }

    @Transactional(readOnly = true)
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UnauthorizedException("Invalid username or password."));
        if (!hashPassword(password).equals(user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password.");
        }
        return user;
    }

    @Transactional
    public User createUser(String username, String rawPassword, UserRole role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessValidationException("Username already exists.");
        }
        return userRepository.save(new User(username, hashPassword(rawPassword), role));
    }

    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
}