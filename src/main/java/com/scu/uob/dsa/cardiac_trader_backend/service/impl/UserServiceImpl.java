package com.scu.uob.dsa.cardiac_trader_backend.service.impl;

import com.scu.uob.dsa.cardiac_trader_backend.dto.LoginRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.RegisterRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.ForgotPasswordRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.ResetPasswordRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.enums.AccountStatus;
import com.scu.uob.dsa.cardiac_trader_backend.exception.ResourceNotFoundException;
import com.scu.uob.dsa.cardiac_trader_backend.model.User;
import com.scu.uob.dsa.cardiac_trader_backend.repository.UserRepository;
import com.scu.uob.dsa.cardiac_trader_backend.service.EmailService;
import com.scu.uob.dsa.cardiac_trader_backend.service.UserService;
import com.scu.uob.dsa.cardiac_trader_backend.util.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public User register(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getUsername()); // Default display name to username
        user.setEmailVerified(false);
        user.setStatus(AccountStatus.ACTIVE);

        // Generate email verification token only if email is provided
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            String verificationToken = UUID.randomUUID().toString();
            user.setEmailVerificationToken(verificationToken);
        }

        user = userRepository.save(user);

        // Send verification email only if email is provided
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), user.getEmailVerificationToken());
        }

        return user;
    }

    @Override
    @Transactional
    public User login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Check account status
        if (user.getStatus() == AccountStatus.SUSPENDED) {
            throw new RuntimeException("Account is suspended");
        }
        if (user.getStatus() == AccountStatus.DELETED) {
            throw new RuntimeException("Account is deleted");
        }

        // Update last login timestamp
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return user;
    }

    @Override
    @SuppressWarnings("null")
    public User getUserById(UUID userId) {
        return userRepository.findById(userId) // NOSONAR - orElseThrow guarantees non-null
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User with this email not found"));

        // Generate password reset token
        String resetToken = jwtTokenProvider.generatePasswordResetToken(user.getUsername(), user.getId());

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetToken);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        try {
            // Validate reset token
            String tokenType = jwtTokenProvider.getTokenType(request.getToken());
            if (!"password_reset".equals(tokenType)) {
                throw new RuntimeException("Invalid reset token");
            }

            // Extract username from token
            String username = jwtTokenProvider.getUsernameFromToken(request.getToken());

            // Validate token
            if (!jwtTokenProvider.validateToken(request.getToken())) {
                throw new RuntimeException("Reset token expired or invalid");
            }

            // Get user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update password
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired reset token: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with this email not found"));

        if (user.getEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        // Generate new verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationToken);
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public User updateProfile(UUID userId, String displayName, String bio, String avatarUrl) {
        User user = userRepository.findById(userId) // NOSONAR - orElseThrow guarantees non-null
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId) // NOSONAR - orElseThrow guarantees non-null
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
