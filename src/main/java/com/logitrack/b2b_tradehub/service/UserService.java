package com.logitrack.b2b_tradehub.service;

import com.logitrack.b2b_tradehub.entity.User;
import com.logitrack.b2b_tradehub.entity.enums.UserRole;
import com.logitrack.b2b_tradehub.exception.BusinessValidationException;
import com.logitrack.b2b_tradehub.exception.UnauthorizedException;
import com.logitrack.b2b_tradehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Custom Hashing function using SHA-256 (less secure than BCrypt but adheres to strict constraints).
     * This method is implemented using standard Java Development Kit (JDK) classes only.
     * @param text The plain text password to hash.
     * @return The hashed text in Base64 format.
     */
    private String hashPassword(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to apply hashing algorithm (SHA-256).", e);
        }
    }

    /**
     * Authenticates the user by validating credentials against the stored hash.
     * @param username The username.
     * @param password The raw (unhashed) password input.
     * @return The authenticated User entity.
     * @throws UnauthorizedException If the credentials are invalid.
     */
    @Transactional(readOnly = true)
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UnauthorizedException("Invalid username or password."));

        String inputHash = hashPassword(password);

        if (!inputHash.equals(user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password.");
        }

        return user;
    }

    /**
     * Creates a new User entity, hashing the password before saving.
     */
    @Transactional
    public User createUser(String username, String rawPassword, UserRole role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessValidationException("Username already exists.");
        }

        String hashedPassword = hashPassword(rawPassword);

        User user = new User(username, hashedPassword, role);

        return userRepository.save(user);
    }

    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
}