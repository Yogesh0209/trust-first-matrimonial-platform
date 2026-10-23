package com.tfmsp.backend.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tfmsp.backend.entity.Bureau;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.repository.BureauRepository;
import com.tfmsp.backend.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // End users must be ACTIVE to use these areas
    private static final List<String> ACTIVE_ONLY_PATHS = List.of(
            "/api/search", "/api/interests", "/api/conversations", "/api/shortlists");

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BureauRepository bureauRepository;

    public JwtAuthenticationFilter(JwtService jwtService,
            UserRepository userRepository,
            BureauRepository bureauRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.bureauRepository = bureauRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.parseToken(header.substring(7));
            Long userId = Long.valueOf(claims.getSubject());

            // Always re-check the user in the database (token may be old)
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                writeError(response, 401, "UNAUTHORIZED", "User no longer exists");
                return;
            }
            if (user.isBlocked() || user.getStatus() == UserStatus.SUSPENDED) {
                writeError(response, 403, "ACCOUNT_SUSPENDED", "Your account is suspended");
                return;
            }
            if (user.getStatus() == UserStatus.DELETED) {
                writeError(response, 403, "ACCOUNT_DELETED", "Your account has been deleted");
                return;
            }
            if (user.getBureauId() != null) {
                Bureau bureau = bureauRepository.findById(user.getBureauId()).orElse(null);
                if (bureau != null && "SUSPENDED".equalsIgnoreCase(bureau.getStatus())) {
                    writeError(response, 403, "TENANT_SUSPENDED", "Your bureau is suspended");
                    return;
                }
            }
            if (user.getRole() == Role.END_USER && user.getStatus() != UserStatus.ACTIVE) {
                String path = request.getRequestURI();
                if (ACTIVE_ONLY_PATHS.stream().anyMatch(path::startsWith)) {
                    writeError(response, 403, "ACCOUNT_NOT_ACTIVE",
                            "Your account is not active yet");
                    return;
                }
            }

            // Role and tenant come from the database, not from the token
            var authentication = new UsernamePasswordAuthenticationToken(
                    String.valueOf(user.getId()), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (user.getBureauId() != null) {
                TenantContext.setTenantId(user.getBureauId());
            }

            try {
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
            }

        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            TenantContext.clear();
            writeError(response, 401, "UNAUTHORIZED", "Invalid or expired JWT");
        }
    }

    private void writeError(HttpServletResponse res, int status, String code, String message)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.getWriter().write("{\"status\":" + status + ",\"code\":\"" + code
                + "\",\"message\":\"" + message + "\"}");
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean publicAuth = path.startsWith("/api/auth/") && !path.equals("/api/auth/change-password");
        return publicAuth
                || path.equals("/api/tenants/register")
                || path.startsWith("/actuator/health");
    }
}