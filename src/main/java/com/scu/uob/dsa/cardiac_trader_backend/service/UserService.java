package com.scu.uob.dsa.cardiac_trader_backend.service;

import com.scu.uob.dsa.cardiac_trader_backend.dto.LoginRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.RegisterRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.ForgotPasswordRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.ResetPasswordRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.model.User;

import java.util.UUID;

/**
 * Service interface for user management
 */
public interface UserService {
    /**
     * Register a new user
     * @param request Registration request with username, password, email
     * @return Created User entity
     * @throws RuntimeException if username or email already exists
     */
    User register(RegisterRequestDTO request);

    /**
     * Authenticate a user
     * @param request Login request with username and password
     * @return User entity if authentication successful
     * @throws RuntimeException if credentials are invalid
     */
    User login(LoginRequestDTO request);

    /**
     * Get user by ID
     * @param userId The user ID
     * @return User entity
     * @throws com.scu.uob.dsa.cardiac_trader_backend.exception.ResourceNotFoundException if user not found
     */
    User getUserById(UUID userId);

    /**
     * Get user by username
     * @param username The username
     * @return User entity or null if not found
     */
    User getUserByUsername(String username);

    /**
     * Get user by email
     * @param email The email
     * @return User entity or null if not found
     */
    User getUserByEmail(String email);

    /**
     * Request password reset (sends reset token via email)
     * @param request Forgot password request with email
     * @throws RuntimeException if user not found
     */
    void requestPasswordReset(ForgotPasswordRequestDTO request);

    /**
     * Reset password using reset token
     * @param request Reset password request with token and new password
     * @throws RuntimeException if token is invalid or expired
     */
    void resetPassword(ResetPasswordRequestDTO request);

    /**
     * Verify email using verification token
     * @param token Verification token
     * @throws RuntimeException if token is invalid or expired
     */
    void verifyEmail(String token);

    /**
     * Resend verification email
     * @param email User email address
     * @throws RuntimeException if user not found
     */
    void resendVerificationEmail(String email);

    /**
     * Update user profile
     * @param userId User ID
     * @param displayName Display name (optional)
     * @param bio Bio (optional)
     * @param avatarUrl Avatar URL (optional)
     * @return Updated User entity
     * @throws RuntimeException if user not found
     */
    User updateProfile(UUID userId, String displayName, String bio, String avatarUrl);

    /**
     * Change user password
     * @param userId User ID
     * @param currentPassword Current password
     * @param newPassword New password
     * @throws RuntimeException if current password is incorrect or user not found
     */
    void changePassword(UUID userId, String currentPassword, String newPassword);
}

