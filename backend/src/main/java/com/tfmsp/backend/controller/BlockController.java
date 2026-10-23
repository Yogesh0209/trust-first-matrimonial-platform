package com.tfmsp.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.tfmsp.backend.service.BlockService;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping("/{profileId}")
    public ResponseEntity<Map<String, String>> block(@PathVariable Long profileId, Authentication auth) {
        blockService.block(Long.valueOf(auth.getName()), profileId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Profile blocked"));
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Map<String, String>> unblock(@PathVariable Long profileId, Authentication auth) {
        blockService.unblock(Long.valueOf(auth.getName()), profileId);
        return ResponseEntity.ok(Map.of("message", "Profile unblocked"));
    }

    @GetMapping
    public ResponseEntity<List<Long>> list(Authentication auth) {
        return ResponseEntity.ok(blockService.getBlockedProfileIds(Long.valueOf(auth.getName())));
    }
}