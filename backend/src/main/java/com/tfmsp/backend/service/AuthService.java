package com.tfmsp.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.AuthResponse;
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

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BureauRepository bureauRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
            BureauRepository bureauRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.bureauRepository = bureauRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {
        // 1. Legal Marriageable Age Validation (FRD PRO-04)
        validateLegalAge(request.getGender(), request.getDateOfBirth());

        // 2. Validate Bureau Code (FRD AUTH-01)
        Bureau bureau = bureauRepository.findByBureauCode(request.getBureauCode().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bureau code"));

        if (!"ACTIVE".equalsIgnoreCase(bureau.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bureau is currently not active for registration");
        }

        // 3. Unique Email and Phone Validation
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already registered");
        }

        // 4. Create User under Bureau as END_USER
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.END_USER);
        user.setBureauId(bureau.getId());
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getFullName(), saved.getEmail(), saved.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        // 1. Check if account is locked (FRD AUTH-03: 15 min lock after 5 failed
        // attempts)
        if (user.isAccountLocked()) {
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    "Account is locked due to 5 failed attempts. Please try again later.");
        }

        // 2. Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                userRepository.save(user);
                throw new ResponseStatusException(HttpStatus.LOCKED,
                        "Account locked for 15 minutes due to 5 failed login attempts");
            }
            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // 3. Check if user is blocked or suspended
        if (user.isBlocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account is blocked");
        }

        // 4. Successful login: Reset failed attempts & update last active time (FRD
        // FRE-01)
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastActiveAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getRole(), user.getBureauId(), jwtService.getExpirationSeconds());
    }

    private void validateLegalAge(Gender gender, LocalDate dateOfBirth) {
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (gender == Gender.FEMALE && age < 18) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Registration rejected: minimum legal marriageable age for females is 18 years");
        }
        if (gender == Gender.MALE && age < 21) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Registration rejected: minimum legal marriageable age for males is 21 years");
        }
        if (gender == Gender.OTHER && age < 18) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Registration rejected: minimum legal age is 18 years");
        }
    }
}
