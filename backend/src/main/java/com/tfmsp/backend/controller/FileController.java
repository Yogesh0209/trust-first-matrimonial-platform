package com.tfmsp.backend.controller;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tfmsp.backend.service.FileStorageService;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService storage;

    public FileController(FileStorageService storage) {
        this.storage = storage;
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

    // Private: needs a logged-in user (selfie and ID images).
    @GetMapping("/documents/{name}")
    public ResponseEntity<Resource> document(@PathVariable String name) {
        return serve(storage.loadDocument(name), name, false);
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