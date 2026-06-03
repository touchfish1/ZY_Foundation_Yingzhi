package com.zhangyuan.common.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class JwtService {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final SecurityProperties properties;
    private final ObjectMapper objectMapper;

    public JwtService(SecurityProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String generateToken(AuthUser user) {
        Instant now = Instant.now();
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getUsername());
        payload.put("uid", user.getId());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plusSeconds(properties.getJwtExpiresSeconds()).getEpochSecond());

        String unsigned = encodeJson(header) + "." + encodeJson(payload);
        return unsigned + "." + sign(unsigned);
    }

    public Optional<String> validateAndGetUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }

            String unsigned = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(unsigned), parts[2])) {
                return Optional.empty();
            }

            Map<String, Object> payload = objectMapper.readValue(URL_DECODER.decode(parts[1]), new TypeReference<>() {
            });
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= exp) {
                return Optional.empty();
            }

            return Optional.ofNullable((String) payload.get("sub"));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to encode JWT", e);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign JWT", e);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        if (leftBytes.length != rightBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < leftBytes.length; i++) {
            result |= leftBytes[i] ^ rightBytes[i];
        }
        return result == 0;
    }
}
