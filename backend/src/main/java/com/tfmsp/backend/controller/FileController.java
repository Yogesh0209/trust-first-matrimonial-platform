package com.tfmsp.backend.controller;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.repository.ProfileRepository;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.repository.VerificationRepository;
import com.tfmsp.backend.service.FileStorageService;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService storage;
    private final VerificationRepository verificationRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public FileController(FileStorageService storage, VerificationRepository verificationRepository,
            ProfileRepository profileRepository, UserRepository userRepository) {
        this.storage = storage;
        this.verificationRepository = verificationRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadPhoto(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("url", storage.savePhoto(file)));
    }

    @PostMapping(value = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadDocument(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("url", storage.saveDocument(file)));
    }

    // Public: profile photos are shown in <img> tags, which cannot send a token.
    @GetMapping("/photos/{name}")
    public ResponseEntity<Resource> photo(@PathVariable String name) {
        return serve(storage.loadPhoto(name), name, true);
    }

    // Private: only the owner, the owner's bureau host, or the super admin.
    @GetMapping("/documents/{name}")
    public ResponseEntity<Resource> document(@PathVariable String name, Authentication auth) {
        User caller = userRepository.findById(Long.valueOf(auth.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (!canViewDocument(caller, name)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view this document");
        }
        return serve(storage.loadDocument(name), name, false);
    }

    private boolean canViewDocument(User caller, String name) {
        if (caller.getRole() == Role.SUPER_ADMIN) {
            return true;
        }
        String url = "/api/files/documents/" + name;
        return verificationRepository.findByFileUrlOrFileUrlBack(url, url).stream()
                .map(v -> profileRepository.findById(v.getProfileId()).orElse(null))
                .filter(p -> p != null)
                .map(p -> userRepository.findById(p.getUserId()).orElse(null))
                .filter(owner -> owner != null)
                .anyMatch(owner -> owner.getId().equals(caller.getId())
                        || (caller.getRole() == Role.HOST
                                && caller.getBureauId() != null
                                && caller.getBureauId().equals(owner.getBureauId())));
    }

    private ResponseEntity<Resource> serve(Resource resource, String name, boolean cache) {
        MediaType type = name.endsWith(".png") ? MediaType.IMAGE_PNG
                : name.endsWith(".webp") ? MediaType.parseMediaType("image/webp")
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok()
                .contentType(type)
                .cacheControl(cache ? CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic() : CacheControl.noStore())
                .body(resource);
    }
}