package com.moneyme.service;

import com.moneyme.config.JwtTokenProvider;
import com.moneyme.dto.request.LoginRequest;
import com.moneyme.dto.request.RegisterRequest;
import com.moneyme.dto.response.AuthResponse;
import com.moneyme.entity.Role;
import com.moneyme.entity.User;
import com.moneyme.exception.BadRequestException;
import com.moneyme.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("An account with this email address already exists.");
        }

        // Assign ADMIN role if it's an admin email or the first registered user
        Role role = Role.USER;
        if (userRepository.count() == 0 || normalizedEmail.startsWith("admin@")) {
            role = Role.ADMIN;
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(normalizedEmail, encodedPassword, request.getFullName().trim(), role);
        User savedUser = userRepository.save(user);

        auditService.log(savedUser.getId(), "USER_REGISTERED", "USER", savedUser.getId().toString(),
                "Registered with email: " + savedUser.getEmail() + ", role: " + savedUser.getRole());

        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole().name()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        auditService.log(user.getId(), "USER_LOGIN", "USER", user.getId().toString(), "User logged in successfully");

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );
    }
}
