package com.moneyme.service;

import com.moneyme.config.JwtTokenProvider;
import com.moneyme.dto.request.LoginRequest;
import com.moneyme.dto.request.RegisterRequest;
import com.moneyme.dto.response.AuthResponse;
import com.moneyme.entity.Role;
import com.moneyme.entity.User;
import com.moneyme.exception.BadRequestException;
import com.moneyme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("alice@example.com", "encodedPass", "Alice Doe", Role.USER);
        sampleUser.setId(1L);
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("Alice Doe", "alice@example.com", "password123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");
        when(userRepository.count()).thenReturn(1L);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateToken(anyLong(), anyString(), anyString())).thenReturn("mockJwtToken");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("alice@example.com", response.getEmail());
        assertEquals("mockJwtToken", response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsBadRequestException() {
        RegisterRequest request = new RegisterRequest("Alice Doe", "alice@example.com", "password123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("alice@example.com", "password123");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "encodedPass")).thenReturn(true);
        when(tokenProvider.generateToken(sampleUser.getId(), sampleUser.getEmail(), sampleUser.getRole().name()))
                .thenReturn("mockJwtToken");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mockJwtToken", response.getToken());
        assertEquals("alice@example.com", response.getEmail());
    }

    @Test
    void login_WrongPassword_ThrowsBadCredentialsException() {
        LoginRequest request = new LoginRequest("alice@example.com", "wrongPassword");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPass")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}

