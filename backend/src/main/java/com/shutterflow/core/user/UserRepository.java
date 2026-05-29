package com.shutterflow.core.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStudioId(String studioId);

    List<User> findByStudioIdAndRole(String studioId, UserRole role);

    long countByStudioIdAndRoleIn(String studioId, List<UserRole> roles);

    Optional<User> findByEmailAndEnabledTrue(String email);

    List<User> findByStudioIdAndEnabledTrue(String studioId);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts, u.accountNonLocked = :locked, u.lockExpiresAt = :lockExpires WHERE u.id = :userId")
    void updateLoginAttempts(@Param("userId") String userId,
                            @Param("attempts") int attempts,
                            @Param("locked") boolean locked,
                            @Param("lockExpires") LocalDateTime lockExpires);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void recordSuccessfulLogin(@Param("userId") String userId, @Param("loginTime") LocalDateTime loginTime);

    @Query("SELECT u FROM User u WHERE u.accountNonLocked = false AND u.lockExpiresAt < :now")
    List<User> findExpiredLockedAccounts(@Param("now") LocalDateTime now);
}
