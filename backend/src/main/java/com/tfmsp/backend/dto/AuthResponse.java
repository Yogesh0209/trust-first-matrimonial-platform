package com.tfmsp.backend.dto;

import com.tfmsp.backend.enums.Role;

public class AuthResponse {

    private final String token;
    private final Role role;
    private final Long tenantId;
    private final long expiresIn;

    public AuthResponse(String token, Role role, Long tenantId, long expiresIn) {
        this.token = token;
        this.role = role;
        this.tenantId = tenantId;
        this.expiresIn = expiresIn;
    }

    public String getToken() { return token; }
    public Role getRole() { return role; }
    public Long getTenantId() { return tenantId; }
    public long getExpiresIn() { return expiresIn; }
}