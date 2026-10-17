package com.tfmsp.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.BureauRegisterRequest;
import com.tfmsp.backend.dto.BureauResponse;
import com.tfmsp.backend.dto.MemberSummaryResponse;
import com.tfmsp.backend.dto.SubscriptionResponse;
import com.tfmsp.backend.entity.*;
import com.tfmsp.backend.enums.*;
import com.tfmsp.backend.repository.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private BureauRepository bureauRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProfilePhotoRepository profilePhotoRepository;
    @Mock
    private PlanRepository planRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ProfileService profileService;

    private TenantService tenantService;

    @BeforeEach
    void setUp() {
        tenantService = new TenantService(
                bureauRepository,
                userRepository,
                profileRepository,
                profilePhotoRepository,
                planRepository,
                subscriptionRepository,
                auditLogRepository,
                passwordEncoder,
                profileService
        );
    }

    @Test
    void registerBureau_Success() {
        BureauRegisterRequest req = new BureauRegisterRequest();
        req.setBureauName("Apex Matrimony");
        req.setBureauCode("APEX01");
        req.setCity("Mumbai");
        req.setHostFullName("Sunil Gupta");
        req.setEmail("sunil@apex.com");
        req.setPhone("9876543210");
        req.setPassword("Password123!");

        when(bureauRepository.existsByBureauCode("APEX01")).thenReturn(false);
        when(userRepository.existsByEmail("sunil@apex.com")).thenReturn(false);
        when(userRepository.existsByPhone("9876543210")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        Bureau savedBureau = new Bureau();
        savedBureau.setId(10L);
        savedBureau.setName("Apex Matrimony");
        savedBureau.setBureauCode("APEX01");
        savedBureau.setCity("Mumbai");
        savedBureau.setStatus("PENDING");
        when(bureauRepository.save(any(Bureau.class))).thenReturn(savedBureau);

        Plan freePlan = new Plan("Free", BigDecimal.ZERO, 100, false);
        freePlan.setId(1L);
        when(planRepository.findByNameIgnoreCase("Basic")).thenReturn(Optional.of(freePlan));

        BureauResponse resp = tenantService.registerBureau(req);

        assertNotNull(resp);
        assertEquals("Apex Matrimony", resp.getName());
        assertEquals("APEX01", resp.getBureauCode());
        assertEquals("PENDING", resp.getStatus());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void registerBureau_DuplicateCode_ThrowsConflict() {
        BureauRegisterRequest req = new BureauRegisterRequest();
        req.setBureauCode("EXISTING");
        when(bureauRepository.existsByBureauCode("EXISTING")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> tenantService.registerBureau(req));
    }

    @Test
    void updateBureauStatus_Success() {
        Bureau bureau = new Bureau();
        bureau.setId(5L);
        bureau.setName("Shadi Hub");
        bureau.setStatus("PENDING");
        when(bureauRepository.findById(5L)).thenReturn(Optional.of(bureau));
        when(bureauRepository.save(any(Bureau.class))).thenReturn(bureau);

        BureauResponse resp = tenantService.updateBureauStatus(5L, BureauStatus.ACTIVE, 1L);

        assertEquals("ACTIVE", resp.getStatus());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void getHostMembers_ReturnsEnrichedMemberList() {
        User member = new User();
        member.setId(20L);
        member.setBureauId(5L);
        member.setFullName("Rohan Sharma");
        member.setRole(Role.END_USER);
        member.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByBureauIdAndRole(5L, Role.END_USER)).thenReturn(List.of(member));

        Profile profile = new Profile();
        profile.setId(100L);
        profile.setUserId(20L);
        profile.setTrustLevel((short) 2);
        profile.setGender(Gender.MALE);
        profile.setMaritalStatus(MaritalStatus.NEVER_MARRIED);
        profile.setCity("Delhi");
        when(profileRepository.findByUserId(20L)).thenReturn(Optional.of(profile));
        when(profileService.calculateCompleteness(profile)).thenReturn(85);
        when(profileService.calculateTrustScore(member, profile, 85)).thenReturn(60);

        List<MemberSummaryResponse> list = tenantService.getHostMembers(5L);

        assertEquals(1, list.size());
        assertEquals("Rohan Sharma", list.get(0).getFullName());
        assertEquals(85, list.get(0).getCompletenessPercentage());
        assertEquals(60, list.get(0).getTrustScore());
    }

    @Test
    void updateMemberStatus_TenantIsolationEnforced() {
        User user = new User();
        user.setId(30L);
        user.setBureauId(99L); // different bureau!

        when(userRepository.findById(30L)).thenReturn(Optional.of(user));

        // Attempting to update from bureau 5L should throw FORBIDDEN
        assertThrows(ResponseStatusException.class, () ->
                tenantService.updateMemberStatus(5L, 30L, UserStatus.PAUSED, 10L));
    }

    @Test
    void upgradeSubscription_Success() {
        Plan plan = new Plan("Pro", new BigDecimal("499"), 1000, true);
        plan.setId(2L);
        when(planRepository.findById(2L)).thenReturn(Optional.of(plan));

        Subscription activeSub = new Subscription(5L, 1L, LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findFirstByBureauIdAndStatusOrderByEndDateDesc(5L, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSub));

        Subscription newSub = new Subscription(5L, 2L, LocalDate.now(), LocalDate.now().plusMonths(1), SubscriptionStatus.ACTIVE);
        newSub.setId(55L);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(newSub);

        SubscriptionResponse resp = tenantService.upgradeSubscription(5L, 2L, 10L);

        assertNotNull(resp);
        assertEquals("Pro", resp.getPlanName());
        assertEquals(1000, resp.getMemberLimit());
        assertTrue(resp.isGovIdVerificationEnabled());
    }

    @Test
    void checkMemberQuota_Exceeded_ThrowsForbidden() {
        Plan basicPlan = new Plan("Basic", BigDecimal.ZERO, 5, false);
        basicPlan.setId(1L);
        Subscription sub = new Subscription(5L, 1L, LocalDate.now(), LocalDate.now().plusDays(30), SubscriptionStatus.ACTIVE);

        when(subscriptionRepository.findFirstByBureauIdAndStatusOrderByEndDateDesc(5L, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(sub));
        when(planRepository.findById(1L)).thenReturn(Optional.of(basicPlan));
        when(userRepository.countByBureauId(5L)).thenReturn(5L); // reached limit!

        assertThrows(ResponseStatusException.class, () -> tenantService.checkMemberQuota(5L));
    }
}
