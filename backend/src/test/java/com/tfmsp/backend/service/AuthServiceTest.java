package com.tfmsp.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.LoginRequest;
import com.tfmsp.backend.dto.RegisterRequest;
import com.tfmsp.backend.dto.UserResponse;
import com.tfmsp.backend.entity.Bureau;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Gender;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.repository.BureauRepository;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BureauRepository bureauRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, bureauRepository, passwordEncoder, jwtService);
    }

    private RegisterRequest createValidRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Aarav Sharma");
        req.setEmail("aarav@example.com");
        req.setPhone("9876543210");
        req.setPassword("Password@123");
        req.setGender(Gender.MALE);
        req.setDateOfBirth(LocalDate.now().minusYears(25));
        req.setBureauCode("BUR001");
        return req;
    }

    @Test
    void register_underAgeMale_throwsBadRequest() {
        RegisterRequest req = createValidRequest();
        req.setGender(Gender.MALE);
        req.setDateOfBirth(LocalDate.now().minusYears(20)); // under 21

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("minimum legal marriageable age for males is 21"));
    }

    @Test
    void register_underAgeFemale_throwsBadRequest() {
        RegisterRequest req = createValidRequest();
        req.setGender(Gender.FEMALE);
        req.setDateOfBirth(LocalDate.now().minusYears(17)); // under 18

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("minimum legal marriageable age for females is 18"));
    }

    @Test
    void register_invalidBureauCode_throwsBadRequest() {
        RegisterRequest req = createValidRequest();
        when(bureauRepository.findByBureauCode("BUR001")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Invalid bureau code", ex.getReason());
    }

    @Test
    void register_inactiveBureau_throwsForbidden() {
        RegisterRequest req = createValidRequest();
        Bureau inactiveBureau = new Bureau();
        inactiveBureau.setId(10L);
        inactiveBureau.setStatus("PENDING");

        when(bureauRepository.findByBureauCode("BUR001")).thenReturn(Optional.of(inactiveBureau));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(req));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(ex.getReason().contains("not active"));
    }

    @Test
    void register_success() {
        RegisterRequest req = createValidRequest();
        Bureau activeBureau = new Bureau();
        activeBureau.setId(10L);
        activeBureau.setStatus("ACTIVE");

        when(bureauRepository.findByBureauCode("BUR001")).thenReturn(Optional.of(activeBureau));
        when(userRepository.existsByEmail("aarav@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("9876543210")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName("Aarav Sharma");
        savedUser.setEmail("aarav@example.com");
        savedUser.setRole(Role.END_USER);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse res = authService.register(req);
        assertNotNull(res);
        assertEquals(1L, res.getId());
        assertEquals("aarav@example.com", res.getEmail());
        assertEquals(Role.END_USER, res.getRole());
    }

    @Test
    void login_fiveFailedAttempts_locksAccount() {
        LoginRequest req = new LoginRequest();
        req.setEmail("aarav@example.com");
        req.setPassword("WrongPassword");

        User user = new User();
        user.setEmail("aarav@example.com");
        user.setPasswordHash("hashedPassword");
        user.setFailedLoginAttempts(4); // 4th attempt already failed

        when(userRepository.findByEmail("aarav@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashedPassword")).thenReturn(false);

        // 5th failed attempt should lock
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(req));
        assertEquals(HttpStatus.LOCKED, ex.getStatusCode());
        assertTrue(user.isAccountLocked());
        assertEquals(5, user.getFailedLoginAttempts());
        assertNotNull(user.getLockedUntil());
    }
}
