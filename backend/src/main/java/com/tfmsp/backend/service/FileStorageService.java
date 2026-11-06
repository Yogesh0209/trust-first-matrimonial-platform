package com.tfmsp.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileStorageService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> TYPES =
            Map.of("image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");
    private static final Pattern SAFE_NAME =
            Pattern.compile("^[a-f0-9\\-]{36}\\.(jpg|png|webp)$");

    private final Path photoDir;
    private final Path documentDir;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String dir) throws IOException {
        Path root = Paths.get(dir).toAbsolutePath().normalize();
        this.photoDir = root.resolve("photos");
        this.documentDir = root.resolve("documents");
        Files.createDirectories(photoDir);
        Files.createDirectories(documentDir);
    }

    public String savePhoto(MultipartFile file) {
        return save(file, photoDir, "/api/files/photos/");
    }

    public String saveDocument(MultipartFile file) {
        return save(file, documentDir, "/api/files/documents/");
    }

    public Resource loadPhoto(String name) {
        return load(photoDir, name);
    }

    public Resource loadDocument(String name) {
        return load(documentDir, name);
    }

    private String save(MultipartFile file, Path dir, String urlPrefix) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please choose a file");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be 5 MB or smaller");
        }
        String ext = TYPES.get(file.getContentType());
        if (ext == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG, PNG or WEBP images are allowed");
        }
        try {
            if (!looksLikeImage(file, ext)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is not a valid image");
            }
            String name = UUID.randomUUID() + "." + ext;
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dir.resolve(name));
            }
            return urlPrefix + name;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save the file");
        }
    }

    // The browser-sent content type can be faked, so also check the first bytes of the file.
    private boolean looksLikeImage(MultipartFile file, String ext) throws IOException {
        byte[] h = new byte[12];
        try (InputStream in = file.getInputStream()) {
            if (in.readNBytes(h, 0, 12) < 12) return false;
        }
        return switch (ext) {
            case "jpg" -> (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF;
            case "png" -> (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G';
            case "webp" -> h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                    && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
            default -> false;
        };
    }

    private Resource load(Path dir, String name) {
        if (!SAFE_NAME.matcher(name).matches()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        Path file = dir.resolve(name).normalize();
        if (!file.startsWith(dir) || !Files.exists(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        try {
            return new UrlResource(file.toUri());
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
    }
}