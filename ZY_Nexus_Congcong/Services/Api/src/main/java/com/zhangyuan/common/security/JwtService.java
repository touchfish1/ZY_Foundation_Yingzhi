package com.zhangyuan.common.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JWT 令牌生成与验证服务，使用 HMAC-SHA256 签名算法。
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final SecurityProperties properties;
    private final ObjectMapper objectMapper;

    public JwtService(SecurityProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 为用户生成 JWT 令牌。
     *
     * @param user 认证用户信息
     * @return JWT 令牌字符串
     */
    public String generateToken(AuthUser user) {
        log.info("Generating JWT token for user: {}", user.getUsername());
        Instant now = Instant.now();
        // 构建 JWT 头部
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        // 构建 JWT 载荷
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getUsername());
        payload.put("uid", user.getId());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plusSeconds(properties.getJwtExpiresSeconds()).getEpochSecond());

        String unsigned = encodeJson(header) + "." + encodeJson(payload);
        return unsigned + "." + sign(unsigned);
    }

    /**
     * 验证 JWT 令牌并提取用户名。
     *
     * @param token JWT 令牌字符串
     * @return 包含用户名的 Optional，验证失败返回 Optional.empty()
     */
    public Optional<String> validateAndGetUsername(String token) {
        try {
            // 拆分令牌：header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("Invalid JWT token format");
                return Optional.empty();
            }

            String unsigned = parts[0] + "." + parts[1];
            // 常量时间比较签名，防止时序攻击
            if (!constantTimeEquals(sign(unsigned), parts[2])) {
                log.warn("JWT signature verification failed");
                return Optional.empty();
            }

            // 解析载荷并检查过期时间
            Map<String, Object> payload = objectMapper.readValue(URL_DECODER.decode(parts[1]), new TypeReference<>() {
            });
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= exp) {
                log.warn("JWT token expired");
                return Optional.empty();
            }

            String username = (String) payload.get("sub");
            log.debug("JWT token validated for user: {}", username);
            return Optional.ofNullable(username);
        } catch (Exception e) {
            log.error("JWT validation failed", e);
            return Optional.empty();
        }
    }

    /**
     * 将 Map 编码为 Base64URL 格式的 JSON 字符串。
     */
    private String encodeJson(Map<String, Object> value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to encode JWT", e);
        }
    }

    /**
     * 使用 HMAC-SHA256 对指定值进行签名。
     */
    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign JWT", e);
        }
    }

    /**
     * 常量时间比较两个字符串，防止时序攻击。
     */
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
