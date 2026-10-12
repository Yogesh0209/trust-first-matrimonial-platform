package com.tfmsp.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tfmsp.backend.dto.AdminDashboardResponse;
import com.tfmsp.backend.dto.HostDashboardResponse;
import com.tfmsp.backend.entity.*;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.enums.SubscriptionStatus;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.repository.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BureauRepository bureauRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private VerificationRepository verificationRepository;
    @Mock
    private InterestRepository interestRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private PlanRepository planRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private AuditLogRepository auditLogRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                userRepository,
                bureauRepository,
                profileRepository,
                verificationRepository,
                interestRepository,
                reportRepository,
                planRepository,
                subscriptionRepository,
                auditLogRepository
        );
    }

    @Test
    void getHostDashboard_CalculatesMetricsCorrectly() {
        User u1 = new User();
        u1.setId(10L);
        u1.setStatus(UserStatus.ACTIVE);
        u1.setRole(Role.END_USER);

        User u2 = new User();
        u2.setId(20L);
        u2.setStatus(UserStatus.PENDING);
        u2.setRole(Role.END_USER);

        when(userRepository.findByBureauIdAndRole(5L, Role.END_USER)).thenReturn(List.of(u1, u2));

        Subscription sub = new Subscription();
        sub.setPlanId(1L);
        when(subscriptionRepository.findFirstByBureauIdAndStatusOrderByEndDateDesc(5L, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(sub));

        Plan plan = new Plan();
        plan.setName("Free");
        plan.setMemberLimit(100);
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));

        HostDashboardResponse resp = dashboardService.getHostDashboard(5L);

        assertNotNull(resp);
        assertEquals(2, resp.getTotalMembers());
        assertEquals(1, resp.getActiveMembers());
        assertEquals("Free", resp.getSubscriptionPlan());
        assertEquals(100, resp.getMemberLimit());
        assertEquals(2, resp.getQuotaUsagePercentage()); // 2 out of 100 = 2%
    }

    @Test
    void getAdminDashboard_CalculatesPlatformTotals() {
        Bureau b1 = new Bureau();
        b1.setStatus("ACTIVE");
        Bureau b2 = new Bureau();
        b2.setStatus("PENDING");
        when(bureauRepository.findAll()).thenReturn(List.of(b1, b2));
        when(userRepository.count()).thenReturn(150L);

        AdminDashboardResponse resp = dashboardService.getAdminDashboard();

        assertNotNull(resp);
        assertEquals(2, resp.getTotalBureaus());
        assertEquals(1, resp.getActiveBureaus());
        assertEquals(1, resp.getPendingBureaus());
        assertEquals(150L, resp.getTotalPlatformUsers());
    }
}
