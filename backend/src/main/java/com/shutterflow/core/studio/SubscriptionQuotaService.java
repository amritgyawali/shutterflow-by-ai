package com.shutterflow.core.studio;

import com.shutterflow.core.common.AppException;
import com.shutterflow.core.user.UserRepository;
import com.shutterflow.core.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionQuotaService {

    private final StudioRepository studioRepository;
    private final UserRepository userRepository;

    /**
     * Checks if the studio can add another photographer based on its subscription tier.
     */
    public void validatePhotographerQuota(String studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new AppException("Studio not found", HttpStatus.NOT_FOUND));

        PlanTier tier = studio.getPlanTier();
        if (tier == PlanTier.STUDIO) {
            return; // Unlimited
        }

        // Count existing users with PHOTOGRAPHER role (or other team roles, e.g. SECOND_SHOOTER) belonging to this studio
        long teamCount = userRepository.countByStudioIdAndRoleIn(
                studioId, 
                java.util.List.of(UserRole.PHOTOGRAPHER, UserRole.SECOND_SHOOTER)
        );

        int maxPhotographers = (tier == PlanTier.STARTER) ? 1 : 3;

        if (teamCount >= maxPhotographers) {
            log.warn("Subscription quota breach attempt: Studio {} ({}) has reached the limit of {} photographers.", 
                    studioId, tier, maxPhotographers);
            throw new AppException(
                    String.format("You have reached the photographer limit (%d) for the %s plan. Please upgrade to add more team members.", 
                            maxPhotographers, tier), 
                    HttpStatus.FORBIDDEN
            );
        }
    }
}
