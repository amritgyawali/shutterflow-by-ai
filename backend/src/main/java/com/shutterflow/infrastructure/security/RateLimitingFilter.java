package com.shutterflow.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final CacheManager cacheManager;
    private final ConcurrentMap<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Value("${bucket4j.enabled:true}")
    private boolean enabled;

    public RateLimitingFilter(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        // Rate limit only auth pathways
        if (path.contains("/api/v1/auth/")) {
            String ip = getClientIP(request);
            Cache cache = cacheManager.getCache("rate-limit-cache");

            if (cache != null) {
                // Check if IP is currently blocked
                String blockKey = "block:" + ip;
                Long blockExpiry = cache.get(blockKey, Long.class);
                if (blockExpiry != null) {
                    if (System.currentTimeMillis() < blockExpiry) {
                        log.warn("Access block active: IP {} blocked. Remaining time: {}s", 
                                ip, (blockExpiry - System.currentTimeMillis()) / 1000);
                        sendTooManyRequestsError(response, "IP is temporarily blocked due to excessive authentication attempts. Try again in 30 minutes.");
                        return;
                    } else {
                        // Expiry passed, evict from block cache
                        cache.evict(blockKey);
                    }
                }

                // Check bucket limits
                Bucket bucket = ipBuckets.computeIfAbsent(ip, this::createNewBucket);
                if (!bucket.tryConsume(1)) {
                    // Exceeded rate limit! Block for 30 minutes
                    long blockDurationMs = Duration.ofMinutes(30).toMillis();
                    long expiryTime = System.currentTimeMillis() + blockDurationMs;
                    cache.put(blockKey, expiryTime);
                    log.error("SECURITY AUDIT VIOLATION: IP {} exceeded login/auth rate limits. Blocking for 30 minutes.", ip);
                    
                    sendTooManyRequestsError(response, "Too many requests. You have been blocked for 30 minutes.");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private Bucket createNewBucket(String ip) {
        // 5 requests per minute
        Refill refill = Refill.intervally(5, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(5, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendTooManyRequestsError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
            "{\"success\":false,\"message\":\"%s\",\"data\":null}", 
            message
        ));
    }
}
