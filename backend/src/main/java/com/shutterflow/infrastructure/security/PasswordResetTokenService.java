package com.shutterflow.infrastructure.security;

import com.shutterflow.core.common.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetTokenService {

    private final StringRedisTemplate redisTemplate;
    
    private static final String RESET_TOKEN_PREFIX = "pwd_reset_token:";
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(15);
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a secure reset token and stores it in Redis against the email.
     */
    public String generateAndStoreResetToken(String email) {
        byte[] randomBytes = new byte[96]; // 96 bytes base64 encoded is 128 chars
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        
        redisTemplate.opsForValue().set(RESET_TOKEN_PREFIX + token, email, RESET_TOKEN_TTL);
        log.info("Generated password reset token for email: {}", email);
        return token;
    }

    /**
     * Validates a reset token and retrieves the associated email.
     * Throws an exception if the token is invalid or expired.
     * Deletes the token after successful validation to ensure single-use.
     */
    public String validateAndConsumeToken(String token) {
        String key = RESET_TOKEN_PREFIX + token;
        String email = redisTemplate.opsForValue().get(key);
        
        if (email == null) {
            log.warn("Attempt to use invalid or expired password reset token");
            throw new AppException("Invalid or expired password reset token", HttpStatus.UNAUTHORIZED);
        }
        
        // Invalidate token
        redisTemplate.delete(key);
        return email;
    }
}
