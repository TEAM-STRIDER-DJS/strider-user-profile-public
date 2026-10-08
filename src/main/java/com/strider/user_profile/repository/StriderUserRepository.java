package com.strider.user_profile.repository;

import com.strider.user_profile.model.enums.AuthProvider;
import com.strider.user_profile.model.entity.StriderUser;
import com.strider.user_profile.model.response.UserProfileResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StriderUserRepository extends JpaRepository<StriderUser, String> {

    Optional<StriderUser> findByProviderAndProviderId(AuthProvider provider, String providerId);
    boolean existsByProviderId(String providerId);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<StriderUser> findByPhoneNumber(String phoneNumber);

    // 내가 팔로우하는 사람 목록 (Following)
    @Query("""
        SELECT new com.strider.user_profile.model.response.UserProfileResponse(
            u.userId,
            u.profileId,
            u.nickname,
            u.profileImage,
            u.profileThumbImage,
            u.profileDesc,
            u.rankId,
            0,
            CAST((SELECT COUNT(f1) FROM UserFollow f1 WHERE f1.followerId = u.userId) AS int),
            CAST((SELECT COUNT(f2) FROM UserFollow f2 WHERE f2.followingId = u.userId) AS int),
            CASE
                WHEN EXISTS (
                    SELECT 1 FROM UserFollow f
                    WHERE f.followerId = :currentUserId
                    AND f.followingId = u.userId
                    AND f.isAccepted = true
                    ) THEN 'FOLLOWING'
                WHEN EXISTS (
                    SELECT 1 FROM UserFollow f
                    WHERE f.followerId = :currentUserId
                    AND f.followingId = u.userId
                    AND f.isAccepted = false
                    ) THEN 'REQUESTED'
                ELSE 'NOT_FOLLOWING'
            END,
            EXISTS (
                    SELECT 1 FROM UserFollow f4
                    WHERE f4.followerId = u.userId
                    AND f4.followingId = :currentUserId
                    ),
            s.isPublic,
            EXISTS (
                    SELECT 1 FROM UserBlock b1
                    WHERE b1.blockerId = :currentUserId
                    AND b1.targetId = u.userId
                    AND b1.targetType = 'USER'
                    AND b1.status = 'ACTIVE'
            )
        )
        FROM UserProfile u
        JOIN UserFollow f ON f.followingId = u.userId
        JOIN StriderUser s ON s.userId = u.userId
        WHERE f.followerId = :userId
        and u.userId != :currentUserId
        ORDER BY f.createdAt DESC
    """)
    List<UserProfileResponse> findUserProfilesByFollowing(@Param("userId") String userId, @Param("currentUserId") String currentUserId);

    // 나를 팔로우하는 사람 목록 (Followers)
    @Query("""
        SELECT new com.strider.user_profile.model.response.UserProfileResponse(
            u.userId,
            u.userId,
            u.nickname,
            u.profileImage,
            u.profileThumbImage,
            u.profileDesc,
            u.rankId,
            0,
            CAST((SELECT COUNT(f1) FROM UserFollow f1 WHERE f1.followerId = u.userId) AS int),
            CAST((SELECT COUNT(f2) FROM UserFollow f2 WHERE f2.followingId = u.userId) AS int),
            CASE
                WHEN EXISTS (
                    SELECT 1 FROM UserFollow f
                    WHERE f.followerId = :currentUserId
                    AND f.followingId = u.userId
                    AND f.isAccepted = true
                    ) THEN 'FOLLOWING'
                WHEN EXISTS (
                    SELECT 1 FROM UserFollow f
                    WHERE f.followerId = :currentUserId
                    AND f.followingId = u.userId
                    AND f.isAccepted = false
                    ) THEN 'REQUESTED'
                ELSE 'NOT_FOLLOWING'
            END,
            EXISTS (
                    SELECT 1 FROM UserFollow f4
                    WHERE f4.followerId = u.userId
                    AND f4.followingId = :currentUserId
                    ),
            s.isPublic,
            EXISTS (
                    SELECT 1 FROM UserBlock b1
                    WHERE b1.blockerId = :currentUserId
                    AND b1.targetId = u.userId
                    AND b1.targetType = 'USER'
                    AND b1.status = 'ACTIVE'
            )
        )
        FROM UserProfile u
        JOIN UserFollow uf ON uf.followerId = u.userId
        JOIN StriderUser s ON s.userId = u.userId
        WHERE uf.followingId = :userId
        and u.userId != :currentUserId
        ORDER BY uf.createdAt DESC
    """)
    List<UserProfileResponse> findUserProfilesByFollowers(@Param("userId") String userId, @Param("currentUserId") String currentUserId);

    Optional<StriderUser> findByUserId(String userId);
}