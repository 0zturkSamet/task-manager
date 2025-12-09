package com.taskmanager.service;

import com.taskmanager.dto.AuthResponse;
import com.taskmanager.dto.LoginRequest;
import com.taskmanager.dto.RegisterRequest;
import com.taskmanager.entity.User;
import com.taskmanager.exception.DuplicateResourceException;
import com.taskmanager.exception.RateLimitExceededException;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.JwtService;
import com.taskmanager.security.RateLimitService;
import com.taskmanager.security.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RateLimitService rateLimitService;
    private final SecurityAuditService securityAuditService;

    @Transactional
    public AuthResponse register(RegisterRequest request, String clientIpAddress) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check rate limit for registration
        if (!rateLimitService.isRegistrationAllowed(clientIpAddress)) {
            throw new RateLimitExceededException(
                    "Too many registration attempts. Please try again later.",
                    rateLimitService.getRemainingLockoutTime(clientIpAddress)
            );
        }
        rateLimitService.recordRegistrationAttempt(clientIpAddress);

        // Check if user already exists - use generic message to prevent enumeration
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Registration failed. Please check your details and try again.");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        // Audit log successful registration
        securityAuditService.logSuccessfulRegistration(savedUser.getId(), savedUser.getEmail(), clientIpAddress);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return buildAuthResponse(savedUser, token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, String clientIpAddress) {
        log.info("User login attempt for email: {}", request.getEmail());

        // Check rate limit for login
        if (!rateLimitService.isLoginAllowed(clientIpAddress)) {
            long retryAfter = rateLimitService.getRemainingLockoutTime(clientIpAddress);
            throw new RateLimitExceededException(
                    "Too many login attempts. Please try again in " + (retryAfter / 60) + " minutes.",
                    retryAfter
            );
        }

        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            // Record failed attempt and audit log
            rateLimitService.recordFailedLogin(clientIpAddress);
            securityAuditService.logFailedLogin(request.getEmail(), clientIpAddress, "Invalid credentials");
            throw e;
        }

        // Get user from database
        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Clear rate limit on successful login
        rateLimitService.clearLoginAttempts(clientIpAddress);

        String token = jwtService.generateToken(user);

        // Audit log successful login
        securityAuditService.logSuccessfulLogin(user.getEmail(), clientIpAddress, null);
        log.info("User logged in successfully: {}", user.getId());

        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profileImage(user.getProfileImage())
                .token(token)
                .expiresIn(jwtService.getExpirationMs())
                .build();
    }
}
