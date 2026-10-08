package com.strider.user_profile.repository;

import com.strider.user_profile.model.entity.StriderUser;
import com.strider.user_profile.model.entity.UserFollow;
import com.strider.user_profile.model.response.FollowRelationResponse;
import com.strider.user_profile.model.response.UserProfileResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserFollowRepository extends JpaRepository<UserFollow, String> {
    @Query("""
        SELECT COUNT(uf)
        FROM UserFollow uf
        WHERE uf.followingId = :targetUserId
        AND uf.followerId != :currentUserId
        AND uf.isAccepted = true
    """)
    int countFollowerExcludeCurrent(String targetUserId, String currentUserId);

    @Query("""
        SELECT COUNT(uf)
        FROM UserFollow uf
        WHERE uf.followerId = :targetUserId
        AND uf.followingId != :currentUserId
        AND uf.isAccepted = true
    """)
    int countFollowingExcludeCurrent(String targetUserId, String currentUserId);

    void deleteByFollowerIdAndFollowingId(String followerId, String followingId);

    // 내가 팔로잉한 사람
    @Query("""
        SELECT u FROM StriderUser u
        JOIN UserFollow uf ON uf.followingId = u.userId
        WHERE uf.followerId = :userId
        AND uf.isAccepted = true
        ORDER BY uf.createdAt DESC
    """)
    List<StriderUser> findFollowing(@Param("userId") String userId);

    // 나를 팔로우한 사람
    @Query("""
        SELECT u FROM StriderUser u
        JOIN UserFollow uf ON uf.followerId = u.userId
        WHERE uf.followingId = :userId
        AND uf.isAccepted = true
        ORDER BY uf.createdAt DESC
    """)
    List<StriderUser> findFollowers(@Param("userId") String userId);

    @Query("""
        SELECT f.followingId
        FROM UserFollow f
        WHERE f.followerId = :currentUserId
          AND f.followingId IN :targetUserIds
          AND f.isAccepted = true
    """)
    List<String> findFollowingIdsByCurrentUser(@Param("currentUserId") String currentUserId,
                                               @Param("targetUserIds") List<String> targetUserIds);

    @Query("""
        SELECT f.followerId
        FROM UserFollow f
        WHERE f.followingId = :currentUserId
          AND f.followerId IN :targetUserIds
          AND f.isAccepted = true
    """)
    List<String> findFollowerIdsByCurrentUser(@Param("currentUserId") String currentUserId,
                                              @Param("targetUserIds") List<String> targetUserIds);

    // 팔로우 여부 확인
    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);

    // 팔로우 관계 단건 조회 (수락/거절용)
    java.util.Optional<UserFollow> findByFollowerIdAndFollowingId(String followerId, String followingId);

    @Query("""
        SELECT new com.strider.user_profile.model.response.FollowRelationResponse(
            u.userId,
            CASE
                WHEN f1.isAccepted = true THEN 'FOLLOWING'
                WHEN f1.isAccepted = false THEN 'REQUESTED'
                ELSE 'NOT_FOLLOWING'
            END,
            CASE
                WHEN f2.isAccepted = true THEN true
                ELSE false
            END
        )
        FROM StriderUser u
        LEFT JOIN UserFollow f1
            ON f1.followerId = :currentUserId
            AND f1.followingId = u.userId
        LEFT JOIN UserFollow f2
            ON f2.followerId = u.userId
            AND f2.followingId = :currentUserId
            AND f2.isAccepted = true
        WHERE u.userId IN :targetUserIds
        """)
    List<FollowRelationResponse> findFollowRelations(@Param("currentUserId") String currentUserId,
                                                     @Param("targetUserIds") List<String> targetUserIds);
}