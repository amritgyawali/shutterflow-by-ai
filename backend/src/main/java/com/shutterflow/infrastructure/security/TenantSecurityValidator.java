package com.shutterflow.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("tenantSecurity")
public class TenantSecurityValidator {

    /**
     * Custom SpEL method to validate if the authenticated user has access to the specified studioId.
     */
    public boolean hasAccessToStudio(Authentication authentication, String studioId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            // Check if the user is a SUPER ADMIN or belongs to the requested studio
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return true;
            }
            
            return studioId.equals(principal.getStudioId());
        }

        return false;
    }
}
