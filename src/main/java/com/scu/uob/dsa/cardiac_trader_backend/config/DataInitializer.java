package com.scu.uob.dsa.cardiac_trader_backend.config;

import com.scu.uob.dsa.cardiac_trader_backend.model.User;
import com.scu.uob.dsa.cardiac_trader_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PHASE 1: Data Initializer - Creates default/initial user if it doesn't exist
 * This is for testing without authentication.
 * Removes or comments out for production when authentication is enabled.
 * 
 * DISABLED: Authentication system is now working, so initial user creation is not needed.
 * 
 * The default user will have ID 1 if it's the first user in a fresh database.
 * Frontend uses TEST_USER_ID = 1, so this ensures compatibility.
 */
// @Component
// @Order(1) // Run early to ensure default user exists before other components need it
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // PHASE 1: Default user credentials
    private static final String DEFAULT_USERNAME = "testuser";
    private static final String DEFAULT_PASSWORD = "password";
    private static final String DEFAULT_EMAIL = "test@example.com";

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // PHASE 1: Ensure default user exists
        // Check if default user exists by username
        User defaultUser = userRepository.findByUsername(DEFAULT_USERNAME).orElse(null);
        
        if (defaultUser == null) {
            // Check if database is empty - if so, this will be the first user (ID = 1)
            long userCount = userRepository.count();
            
            if (userCount == 0) {
                // Fresh database - create default user (will get ID = 1)
                defaultUser = createDefaultUser();
                System.out.println("PHASE 1: ✓ Default user created with ID " + defaultUser.getId() + 
                    " (username: " + DEFAULT_USERNAME + ", password: " + DEFAULT_PASSWORD + ")");
            } else {
                // Database has users - create default user (may not be ID 1)
                defaultUser = createDefaultUser();
                System.out.println("PHASE 1: ✓ Default user created with ID " + defaultUser.getId() + 
                    " (username: " + DEFAULT_USERNAME + ", password: " + DEFAULT_PASSWORD + ")");
                System.out.println("PHASE 1: ⚠ Warning: Frontend TEST_USER_ID is set to 1, but default user ID is " + 
                    defaultUser.getId() + ". Update frontend if needed.");
            }
        } else {
            System.out.println("PHASE 1: ✓ Default user already exists with ID " + defaultUser.getId() + 
                " (username: " + DEFAULT_USERNAME + ")");
            // Note: With UUID, IDs are no longer numeric, so comparison removed
        }
    }

    private User createDefaultUser() {
        User user = new User();
        user.setUsername(DEFAULT_USERNAME);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setEmail(DEFAULT_EMAIL);
        return userRepository.save(user);
    }
}

