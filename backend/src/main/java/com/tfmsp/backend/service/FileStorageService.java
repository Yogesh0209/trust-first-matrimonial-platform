package com.tfmsp.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
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
    // local names: <uuid>.jpg      cloud documents: b<bureauId>-<uuid>.jpg
    private static final Pattern SAFE_NAME =
            Pattern.compile("^(b\\d+-)?[a-f0-9\\-]{36}\\.(jpg|png|webp)$");
    private static final Pattern CLOUD_DOC =
            Pattern.compile("^b(\\d+)-([a-f0-9\\-]{36})\\.(jpg|png|webp)$");

    private final Path photoDir;
    private final Path documentDir;
    private final boolean cloudMode;
    private final CloudinaryClient cloud;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String dir,
            @Value("${app.storage:local}") String mode,
            CloudinaryClient cloud) throws IOException {
        Path root = Paths.get(dir).toAbsolutePath().normalize();
        this.photoDir = root.resolve("photos");
        this.documentDir = root.resolve("documents");
        Files.createDirectories(photoDir);
        Files.createDirectories(documentDir);
        this.cloudMode = "cloudinary".equalsIgnoreCase(mode);
        this.cloud = cloud;
    }

    public String savePhoto(MultipartFile file, Long bureauId) {
        if (cloudMode) {
            return saveToCloud(file, bureauId, false);
        }
        return save(file, photoDir, "/api/files/photos/");
    }

    public String saveDocument(MultipartFile file, Long bureauId) {
        if (cloudMode) {
            return saveToCloud(file, bureauId, true);
        }
        return save(file, documentDir, "/api/files/documents/");
    }

    public Resource loadPhoto(String name) {
        return load(photoDir, name);
    }

    public Resource loadDocument(String name) {
        if (cloudMode) {
            Matcher m = CLOUD_DOC.matcher(name);
            if (m.matches()) {
                return cloud.download("milan/b" + m.group(1) + "/documents/" + m.group(2), m.group(3));
            }
        }
        return load(documentDir, name);
    }

    // ---- cloud ----
    private String saveToCloud(MultipartFile file, Long bureauId, boolean isDocument) {
        String ext = validate(file);
        String id = UUID.randomUUID().toString();
        String bureau = "b" + (bureauId == null ? 0 : bureauId);
        try {
            byte[] bytes = file.getBytes();
            if (!isDocument) {
                // public image, the full https link is stored
                return cloud.upload(bytes, "milan/" + bureau + "/photos/" + id, "upload");
            }
            // private image, only reachable through our backend
            cloud.upload(bytes, "milan/" + bureau + "/documents/" + id, "authenticated");
            return "/api/files/documents/" + bureau + "-" + id + "." + ext;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read the file");
        }
    }

    // ---- local disk ----
    private String save(MultipartFile file, Path dir, String urlPrefix) {
        String ext = validate(file);
        try {
            String name = UUID.randomUUID() + "." + ext;
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dir.resolve(name));
            }
            return urlPrefix + name;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save the file");
        }
    }

    /** Checks size, type and real file content. Returns the file extension. */
    private String validate(MultipartFile file) {
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
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read the file");
        }
        return ext;
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