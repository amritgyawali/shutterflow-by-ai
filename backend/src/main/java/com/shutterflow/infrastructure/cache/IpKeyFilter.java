package com.shutterflow.infrastructure.cache;

import com.giffing.bucket4j.spring.boot.starter.context.KeyFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class IpKeyFilter implements KeyFilter<HttpServletRequest> {

    @Override
    public String key(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
