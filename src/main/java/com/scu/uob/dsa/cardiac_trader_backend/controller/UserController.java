package com.scu.uob.dsa.cardiac_trader_backend.controller;

import com.scu.uob.dsa.cardiac_trader_backend.dto.LoginRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.RegisterRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.ForgotPasswordRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.ResetPasswordRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.ChangePasswordRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.model.User;
import com.scu.uob.dsa.cardiac_trader_backend.service.UserService;
import com.scu.uob.dsa.cardiac_trader_backend.service.GameService;
import com.scu.uob.dsa.cardiac_trader_backend.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GameService gameService;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    @Value("${jwt.refresh.expiration:604800000}")
    private Long jwtRefreshExpiration;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider, GameService gameService) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.gameService = gameService;
    }

    /**
     * Set access token in httpOnly cookie
     */
    private void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtExpiration / 1000)); // Convert milliseconds to seconds
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    /**
     * Set refresh token in httpOnly cookie
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtRefreshExpiration / 1000)); // Convert milliseconds to seconds
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    /**
     * Clear access token cookie
     */
    private void clearAccessTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * Clear refresh token cookie
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * Clear all token cookies
     */
    private void clearAllTokenCookies(HttpServletResponse response) {
        clearAccessTokenCookie(response);
        clearRefreshTokenCookie(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequestDTO request,
            HttpServletResponse httpResponse) {
        try {
            User user = userService.register(request);

            // Generate access and refresh tokens
            String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getId());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getId());

            // Set tokens in httpOnly cookies
            setAccessTokenCookie(httpResponse, accessToken);
            setRefreshTokenCookie(httpResponse, refreshToken);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            // Tokens are in httpOnly cookies, not in response body

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse httpResponse) {
        try {
            User user = userService.login(request);

            // Generate access and refresh tokens
            String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getId());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getId());

            // Set tokens in httpOnly cookies
            setAccessTokenCookie(httpResponse, accessToken);
            setRefreshTokenCookie(httpResponse, refreshToken);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            // Tokens are in httpOnly cookies, not in response body

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse httpResponse) {
        try {
            // Get refresh token from cookie
            String refreshToken = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (refreshToken == null || refreshToken.isEmpty()) {
                throw new RuntimeException("Refresh token not found");
            }

            // Validate refresh token
            String tokenType = jwtTokenProvider.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                throw new RuntimeException("Invalid refresh token");
            }

            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new RuntimeException("Refresh token expired or invalid");
            }

            // Extract user info from refresh token
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

            // Generate new access and refresh tokens
            String newAccessToken = jwtTokenProvider.generateAccessToken(username, userId);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(username, userId);

            // Set new tokens in httpOnly cookies
            setAccessTokenCookie(httpResponse, newAccessToken);
            setRefreshTokenCookie(httpResponse, newRefreshToken);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Token refreshed successfully");
            response.put("userId", userId);
            response.put("username", username);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        try {
            // Get user ID from token before clearing cookies
            UUID userId = null;
            String accessToken = null;
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        accessToken = cookie.getValue();
                        break;
                    }
                }
            }

            // If we have a valid token, abandon active game sessions
            if (accessToken != null && !accessToken.isEmpty()) {
                try {
                    userId = jwtTokenProvider.getUserIdFromToken(accessToken);
                    if (userId != null) {
                        // Abandon all active game sessions for this user
                        gameService.abandonActiveSessions(userId);
                    }
                } catch (Exception e) {
                    // If token is invalid/expired, just continue with logout
                    // Don't fail logout if we can't get userId
                }
            }
        } catch (Exception e) {
            // Continue with logout even if abandoning sessions fails
            // Log the error but don't fail the logout
        }

        // Clear all token cookies
        clearAllTokenCookies(httpResponse);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO request) {
        try {
            // Request password reset (sends email with reset token)
            userService.requestPasswordReset(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password reset email sent. Please check your email.");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request) {
        try {
            userService.resetPassword(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password reset successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(HttpServletRequest request) {
        try {
            // Get access token from cookie
            String accessToken = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        accessToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Access token not found");
            }

            // Validate token
            String tokenType = jwtTokenProvider.getTokenType(accessToken);
            if (!"access".equals(tokenType)) {
                throw new RuntimeException("Invalid token type");
            }

            if (!jwtTokenProvider.validateToken(accessToken)) {
                throw new RuntimeException("Token expired or invalid");
            }

            // Extract user info
            String username = jwtTokenProvider.getUsernameFromToken(accessToken);
            UUID userId = jwtTokenProvider.getUserIdFromToken(accessToken);

            // Get user details
            User user = userService.getUserById(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("userId", userId);
            response.put("username", username);
            response.put("displayName", user.getDisplayName());
            response.put("email", user.getEmail());
            response.put("emailVerified", user.getEmailVerified());
            response.put("avatarUrl", user.getAvatarUrl());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        try {
            userService.verifyEmail(token);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Email verified successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerificationEmail(
            @RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                throw new RuntimeException("Email is required");
            }

            userService.resendVerificationEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Verification email sent. Please check your email.");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(HttpServletRequest request) {
        try {
            // Get user ID from token
            String accessToken = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        accessToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Access token not found");
            }

            UUID userId = jwtTokenProvider.getUserIdFromToken(accessToken);
            User user = userService.getUserById(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("displayName", user.getDisplayName());
            response.put("email", user.getEmail());
            response.put("emailVerified", user.getEmailVerified());
            response.put("avatarUrl", user.getAvatarUrl());
            response.put("bio", user.getBio());
            response.put("createdAt", user.getCreatedAt());
            response.put("lastLoginAt", user.getLastLoginAt());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            // Get user ID from token
            String accessToken = null;
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        accessToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Access token not found");
            }

            UUID userId = jwtTokenProvider.getUserIdFromToken(accessToken);
            String displayName = request.get("displayName");
            String bio = request.get("bio");
            String avatarUrl = request.get("avatarUrl");

            User user = userService.updateProfile(userId, displayName, bio, avatarUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("displayName", user.getDisplayName());
            response.put("email", user.getEmail());
            response.put("avatarUrl", user.getAvatarUrl());
            response.put("bio", user.getBio());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            // Get user ID from token
            String accessToken = null;
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        accessToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Access token not found");
            }

            UUID userId = jwtTokenProvider.getUserIdFromToken(accessToken);
            userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password changed successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
