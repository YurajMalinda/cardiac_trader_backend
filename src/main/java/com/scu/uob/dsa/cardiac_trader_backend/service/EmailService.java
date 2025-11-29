package com.scu.uob.dsa.cardiac_trader_backend.service;

/**
 * Service interface for sending emails
 */
public interface EmailService {
    /**
     * Send email verification email
     * @param to Recipient email address
     * @param username Username
     * @param verificationToken Verification token
     */
    void sendVerificationEmail(String to, String username, String verificationToken);

    /**
     * Send password reset email
     * @param to Recipient email address
     * @param username Username
     * @param resetToken Reset token
     */
    void sendPasswordResetEmail(String to, String username, String resetToken);

    /**
     * Send OTP email for password reset or verification
     * @param to Recipient email address
     * @param username Username
     * @param otp OTP code
     */
    void sendOtpEmail(String to, String username, String otp);
}

