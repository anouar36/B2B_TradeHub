package com.logitrack.b2b_tradehub.config;

import com.logitrack.b2b_tradehub.dto.client.ClientCreateRequest;
import com.logitrack.b2b_tradehub.entity.User;
import com.logitrack.b2b_tradehub.entity.enums.CustomerTier;
import com.logitrack.b2b_tradehub.entity.enums.UserRole;
import com.logitrack.b2b_tradehub.repository.UserRepository;
import com.logitrack.b2b_tradehub.service.ClientService;
import com.logitrack.b2b_tradehub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Executes upon application startup to seed the database with required default users (ADMIN and CLIENT).
 */
@Component
@RequiredArgsConstructor
public class    DataLoader implements CommandLineRunner {

    private final UserRepository userRepository; // Used only for existence check
    private final UserService userService;
    private final ClientService clientService;

    // --- Default Credentials ---
    private static final String ADMIN_USERNAME = "admin.microtech";
    private static final String ADMIN_PASSWORD = "AdminSecure123";

    private static final String CLIENT_USERNAME = "client.default";
    private static final String CLIENT_PASSWORD = "ClientPwd123";


    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Log to confirm execution
        System.out.println("--- Starting database initialization (Default Users) ---");

        createDefaultAdmin();
        createDefaultClient();

        System.out.println("--- Database initialization finished ---");
    }

    private void createDefaultAdmin() {
        if (userRepository.findByUsername(ADMIN_USERNAME).isEmpty()) {
            // Since the ADMIN user does not require a linked Client profile,
            // we call the lower-level UserService directly.
            User adminUser = userService.createUser(
                    ADMIN_USERNAME,
                    ADMIN_PASSWORD,
                    UserRole.ADMIN
            );
            System.out.println("-> Default ADMIN user created: " + ADMIN_USERNAME);
        } else {
            System.out.println("-> ADMIN user already exists. Skipping creation.");
        }
    }

    private void createDefaultClient() {
        if (userRepository.findByUsername(CLIENT_USERNAME).isEmpty()) {

            // Prepare the DTO for the ClientService
            ClientCreateRequest clientRequest = new ClientCreateRequest();
            clientRequest.setNom("Default Client Company");
            clientRequest.setEmail("contact@defaultclient.com");
            clientRequest.setUsername(CLIENT_USERNAME);
            clientRequest.setPassword(CLIENT_PASSWORD);

            // Use ClientService to handle the transactional creation of both User and Client profile
            clientService.createClient(clientRequest);
            System.out.println("-> Default CLIENT profile created: " + CLIENT_USERNAME);

        } else {
            System.out.println("-> CLIENT user already exists. Skipping creation.");
        }
    }
}