package com.tfmsp.backend.service;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.time.Period;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.AuthResponse;
import com.tfmsp.backend.dto.ChangePasswordRequest;
import com.tfmsp.backend.dto.LoginRequest;
import com.tfmsp.backend.dto.RegisterRequest;
import com.tfmsp.backend.dto.UserResponse;
import com.tfmsp.backend.entity.Bureau;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Gender;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.repository.BureauRepository;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BureauRepository bureauRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PlanLimitService planLimitService;

    public AuthService(UserRepository userRepository,
            BureauRepository bureauRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PlanLimitService planLimitService) {
        this.userRepository = userRepository;
        this.bureauRepository = bureauRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.planLimitService = planLimitService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 1. Legal Marriageable Age Validation
        validateLegalAge(request.getGender(), request.getDateOfBirth());

        // 2. Validate Bureau Code
        Bureau bureau = bureauRepository.findByBureauCodeForUpdate(request.getBureauCode().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bureau code"));

        if (!"ACTIVE".equalsIgnoreCase(bureau.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bureau is currently not active for registration");
        }

        // 3. Tenant Capacity Check (FRD TEN-09, UC-01)
        Integer limit = planLimitService.getMemberLimit(bureau.getId());
        long currentMembers = userRepository.countByBureauIdAndRole(bureau.getId(), Role.END_USER);
        if (limit != null && currentMembers >= limit) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bureau member limit reached for its current plan");
        }

        // 4. Unique Email and Phone Validation
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already registered");
        }

        // 5. Create User under Bureau with initial PENDING status (FRD UC-01, Section 1.4)
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.END_USER);
        user.setStatus(UserStatus.PENDING);
        user.setBureauId(bureau.getId());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getFullName(), saved.getEmail(), saved.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        // 1. Check if account is locked (FRD AUTH-03: 15 min lock after 5 failed attempts)
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

        // 3. Check if user is blocked or suspended (FRD Section 1.3, 1.5)
        if (user.isBlocked() || user.getStatus() == UserStatus.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account has been suspended");
        }
        if (user.getStatus() == UserStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account has been deleted");
        }

        // Check if home bureau is suspended (FRD Section 1.3)
        if (user.getBureauId() != null) {
            Bureau bureau = bureauRepository.findById(user.getBureauId()).orElse(null);
            if (bureau != null && "SUSPENDED".equalsIgnoreCase(bureau.getStatus())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your bureau is currently suspended");
            }
        }

        // 4. Successful login: Reset failed attempts & update last active time (FRD FRE-01)
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastActiveAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getRole(), user.getBureauId(), jwtService.getExpirationSeconds());
    }

    @org.springframework.transaction.annotation.Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
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
