package com.research.mano.controller;

import com.research.mano.controller.request.*;
import com.research.mano.controller.responce.*;
import com.research.mano.entity.User;
import com.research.mano.exception.*;
import com.research.mano.security.JwtTokenProvider;
import com.research.mano.service.Impl.CustomUserDetailsService;
import com.research.mano.service.UserService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Authentication Controller
 * Handles user registration, login, JWT token management, and password operations
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          CustomUserDetailsService userDetailsService,
                          JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * POST /api/auth/register
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // Check if a username exists
        if (userService.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("username", registerRequest.getUsername());
        }

        // Check if email exists
        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("email", registerRequest.getEmail());
        }

        // Create a new user
        User user = userService.createUser(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getFirstName(),
                registerRequest.getLastName()
        );

        // Set additional user properties
        if (registerRequest.getPhone() != null) {
            user.setPhone(registerRequest.getPhone());
        }
        if (registerRequest.getGender() != null) {
            user.setGender(User.Gender.valueOf(registerRequest.getGender().toUpperCase()));
        }
        if (registerRequest.getDateOfBirth() != null) {
            user.setDateOfBirth(LocalDate.parse(registerRequest.getDateOfBirth()).atStartOfDay());
        }

        user.setPrivacyConsent(registerRequest.isPrivacyConsent());
        user.setDataSharingConsent(registerRequest.isDataSharingConsent());

        userService.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "User registered successfully. Please verify your email."));
    }

    /**
     * POST /api/auth/login
     * Authenticate user and return JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        CustomUserDetailsService.UserPrincipal userPrincipal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        User user = userService.findById(userPrincipal.getId())
                .orElseThrow(() -> new UserNotFoundException(userPrincipal.getId()));

        // Check if email is verified
        if (!user.getIsEmailVerified()) {
            throw new AccountNotVerifiedException();
        }

        // Generate tokens
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getUsername());
        Long expiresIn = tokenProvider.getJwtExpirationInMs();

        // Update last login
        userService.updateLastLogin(user.getId());

        // Store refresh token
        LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusDays(30); // 30 days
        userService.updateRefreshToken(user.getId(), refreshToken, refreshTokenExpiry);

        // Create a user summary
        UserSummaryDTO userSummary = createUserSummary(user);

        return ResponseEntity.ok(new JwtAuthenticationResponse(accessToken, refreshToken, expiresIn, userSummary));
    }

    /**
     * POST /api/auth/refresh * JWT access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        // Validate refresh token
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new InvalidJwtTokenException("Invalid refresh token");
        }

        // Find user by refresh token
        User user = userService.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidJwtTokenException("Refresh token not found"));

        // Check if the refresh token is expired
        if (user.getRefreshTokenExpires().isBefore(LocalDateTime.now())) {
            userService.clearRefreshToken(user.getId());
            throw new JwtTokenExpiredException();
        }

        // Generate a new access token
        CustomUserDetailsService.UserPrincipal userPrincipal = (CustomUserDetailsService.UserPrincipal)
                userDetailsService.loadUserById(user.getId());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );

        String newAccessToken = tokenProvider.generateToken(authentication);
        Long expiresIn = tokenProvider.getJwtExpirationInMs();

        UserSummaryDTO userSummary = createUserSummary(user);

        return ResponseEntity.ok(new JwtAuthenticationResponse(newAccessToken, refreshToken, expiresIn, userSummary));
    }

    /**
     * POST /api/auth/logout * user and clear refresh token
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.UserPrincipal) {
            CustomUserDetailsService.UserPrincipal userPrincipal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
            userService.clearRefreshToken(userPrincipal.getId());
        }

        return ResponseEntity.ok(new ApiResponse(true, "User logged out successfully"));
    }

    /**
     * POST /api/auth/forgot-password
     * Send password reset email
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        userService.resetPassword(passwordResetRequest.getEmail());
        return ResponseEntity.ok(new ApiResponse(true, "Password reset email sent"));
    }

    /**
     * POST /api/auth/change-password
     * Change user password (authenticated)
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequest passwordChangeRequest,
                                            Authentication authentication) {
        CustomUserDetailsService.UserPrincipal userPrincipal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();

        boolean success = userService.changePassword(
                userPrincipal.getId(),
                passwordChangeRequest.getCurrentPassword(),
                passwordChangeRequest.getNewPassword()
        );

        if (!success) {
            throw new InvalidCredentialsException();
        }

        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
    }

    /**
     * GET /api/auth/verify-email/{token}
     * Verify user email address
     */
    @GetMapping("/verify-email/{token}")
    public ResponseEntity<?> verifyEmail(@PathVariable String token) {
        boolean verified = userService.verifyEmail(token);

        if (!verified) {
            throw new ValidationException("Invalid or expired verification token");
        }

        return ResponseEntity.ok(new ApiResponse(true, "Email verified successfully"));
    }

    /**
     * GET /api/auth/me
     * Get current user information
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal userPrincipal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        User user = userService.findById(userPrincipal.getId())
                .orElseThrow(() -> new UserNotFoundException(userPrincipal.getId()));

        UserSummaryDTO userSummary = createUserSummary(user);
        return ResponseEntity.ok(userSummary);
    }

    /**
     * Helper method to create user summary DTO
     */
    private UserSummaryDTO createUserSummary(User user) {
        UserSummaryDTO userSummary = new UserSummaryDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );

        userSummary.setProfileImageUrl(user.getProfileImageUrl());
        userSummary.setEmailVerified(user.getIsEmailVerified());

        // Set roles
        String[] roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toArray(String[]::new);
        userSummary.setRoles(roles);

        userSummary.setMentalHealthStatus(user.getMentalHealthStatus() != null ?
                user.getMentalHealthStatus().name() : null);

        return userSummary;
    }

    /**
     * Generic API Response class
     */
    @Getter
    @Setter
    public static class ApiResponse {
        private Boolean success;
        private String message;

        public ApiResponse(Boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}