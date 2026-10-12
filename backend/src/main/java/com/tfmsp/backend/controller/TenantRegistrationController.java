package com.tfmsp.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tfmsp.backend.dto.BureauRegisterRequest;
import com.tfmsp.backend.dto.BureauResponse;
import com.tfmsp.backend.service.TenantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tenants")
public class TenantRegistrationController {

    private final TenantService tenantService;

    public TenantRegistrationController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping("/register")
    public ResponseEntity<BureauResponse> registerBureau(@Valid @RequestBody BureauRegisterRequest req) {
        BureauResponse response = tenantService.registerBureau(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
