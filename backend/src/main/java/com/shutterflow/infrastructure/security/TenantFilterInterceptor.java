package com.shutterflow.infrastructure.security;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantFilterInterceptor implements HandlerInterceptor {

    private final EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String studioId = principal.getStudioId();
            
            if (studioId != null) {
                try {
                    Session session = entityManager.unwrap(Session.class);
                    Filter filter = session.enableFilter("tenantFilter");
                    filter.setParameter("studioId", studioId);
                    log.debug("Enabled Hibernate tenantFilter with studioId: {} for request URI: {}", studioId, request.getRequestURI());
                } catch (Exception e) {
                    log.error("Failed to enable Hibernate multi-tenant filter", e);
                }
            }
        }
        
        return true;
    }
}
