package com.tfmsp.backend.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

/** Minimal Cloudinary REST client: signed upload and signed private download. */
@Component
public class CloudinaryClient {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryClient.class);

    private final RestClient http = RestClient.builder().build();
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinaryClient(@Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    /** type = "upload" (public link) or "authenticated" (private). Returns the secure URL. */
    public String upload(byte[] data, String publicId, String type) {
        requireConfigured();
        long ts = System.currentTimeMillis() / 1000;
        String signature = sign("public_id=" + publicId + "&timestamp=" + ts + "&type=" + type);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return "upload";
            }
        });
        body.add("api_key", apiKey);
        body.add("timestamp", String.valueOf(ts));
        body.add("public_id", publicId);
        body.add("type", type);
        body.add("signature", signature);

        try {
            Map<String, Object> res = http.post()
                    .uri("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            return String.valueOf(res.get("secure_url"));
        } catch (RestClientException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "File storage error. Please try again.");
        }
    }

    /** Downloads a private (authenticated) image using a signed request. */
    public Resource download(String publicId, String format) {
        requireConfigured();
        long ts = System.currentTimeMillis() / 1000;
        String signature = sign("format=" + format + "&public_id=" + publicId
                + "&timestamp=" + ts + "&type=authenticated");

        URI uri = UriComponentsBuilder
                .fromUriString("https://api.cloudinary.com/v1_1/" + cloudName + "/image/download")
                .queryParam("api_key", apiKey)
                .queryParam("format", format)
                .queryParam("public_id", publicId)
                .queryParam("timestamp", ts)
                .queryParam("type", "authenticated")
                .queryParam("signature", signature)
                .build().encode().toUri();
        try {
            byte[] bytes = http.get().uri(uri).retrieve().body(byte[].class);
            return new ByteArrayResource(bytes == null ? new byte[0] : bytes);
        } catch (RestClientException e) {
            log.warn("Cloudinary download failed for {}: {}", publicId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
    }

    // Cloudinary signature: SHA-1 of "sorted params" + api secret
    private String sign(String paramsToSign) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] digest = sha1.digest((paramsToSign + apiSecret).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign Cloudinary request", e);
        }
    }

    private void requireConfigured() {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Cloud storage is not configured");
        }
    }
}