package com.tfmsp.backend.security;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Simple per-minute request limiter (in memory, one server). */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000;
    private static final int LOGIN_REGISTER_LIMIT = 10; // per IP per minute
    private static final int OTP_LIMIT = 5;             // per IP per minute
    private static final int GENERAL_LIMIT = 120;       // per user (or IP) per minute

    // key -> [window start time, request count]
    private final Map<String, long[]> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        // Skip: non-API calls, CORS preflight, and public photo images (many per page)
        if (!path.startsWith("/api/") || path.startsWith("/api/files/photos/")
                || "OPTIONS".equals(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String ip = clientIp(request);
        String key;
        int limit;
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")
                || path.equals("/api/tenants/register")) {
            key = "auth:" + ip;
            limit = LOGIN_REGISTER_LIMIT;
        } else if (path.startsWith("/api/auth/")) {
            key = "otp:" + ip;
            limit = OTP_LIMIT;
        } else {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            boolean loggedIn = a != null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken);
            key = "gen:" + (loggedIn ? "u:" + a.getName() : "ip:" + ip);
            limit = GENERAL_LIMIT;
        }

        long now = System.currentTimeMillis();
        long[] w = windows.compute(key, (k, old) -> {
            if (old == null || now - old[0] >= WINDOW_MS) {
                return new long[] { now, 1 };
            }
            old[1]++;
            return old;
        });

        if (windows.size() > 10_000) { // keep memory small
            windows.entrySet().removeIf(e -> now - e.getValue()[0] >= WINDOW_MS);
        }

        if (w[1] > limit) {
            long retryAfter = Math.max(1, (WINDOW_MS - (now - w[0])) / 1000);
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"code\":\"TOO_MANY_REQUESTS\","
                    + "\"message\":\"Too many requests. Please try again in " + retryAfter + " seconds.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    // Behind a proxy (Render/Railway) the real client is the last X-Forwarded-For entry.
    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String[] parts = xff.split(",");
            return parts[parts.length - 1].trim();
        }
        return request.getRemoteAddr();
    }
}