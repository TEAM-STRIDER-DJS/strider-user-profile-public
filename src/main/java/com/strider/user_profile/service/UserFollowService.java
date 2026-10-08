package com.strider.user_profile.service;

import com.strider.strider_common_lib.error.StriderErrorCodes;
import com.strider.strider_common_lib.exception.StriderException;
import com.strider.user_profile.model.entity.StriderUser;
import com.strider.user_profile.model.entity.UserFollow;
import com.strider.user_profile.model.response.FollowRelationResponse;
import com.strider.user_profile.model.response.UserFollowResponse;
import com.strider.user_profile.model.response.UserProfileResponse;
import com.strider.user_profile.repository.StriderUserRepository;
import com.strider.user_profile.repository.UserFollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserFollowService {
    private final UserFollowRepository userFollowRepository;
    private final StriderUserRepository striderUserRepository;
    private final com.strider.user_profile.kafka.producer.NotificationEventProducer notificationEventProducer;

    // 내가 팔로우하는 사람 목록 (Following)
    @Transactional(readOnly = true)
    public List<UserFollowResponse> getFollowingList(String targetUserId, String currentUserId) {
        // 1. 팔로잉 대상 유저 프로필 조회
        List<UserProfileResponse> profiles = striderUserRepository.findUserProfilesByFollowing(targetUserId, currentUserId);
        List<String> targetUserIds = profiles.stream()
                .map(UserProfileResponse::userId)
                .toList();

        if (targetUserIds.isEmpty()) return List.of();

        // 2. 현재 사용자가 팔로우/팔로우한 유저 관계 조회
        Map<String, FollowRelationResponse> relationMap =
                userFollowRepository.findFollowRelations(currentUserId, targetUserIds)
                        .stream()
                        .collect(Collectors.toMap(
                                FollowRelationResponse::userId,
                                Function.identity()
                        ));

        // 3. UserFollowResponse 매핑
        return profiles.stream()
                .map(profile -> {
                    FollowRelationResponse relation =
                            relationMap.get(profile.userId());

                    return UserFollowResponse.fromUserProfile(
                            profile,
                            relation != null ? relation.followStatus() : "NOT_FOLLOWING",
                            relation != null && relation.isFollower()
                    );

                })
                .collect(Collectors.toList());
    }

    // 나를 팔로우하는 사람 목록 (Followers)
    @Transactional(readOnly = true)
    public List<UserFollowResponse> getFollowerList(String targetUserId, String currentUserId) {
        // 1. 팔로워 대상 유저 프로필 조회
        List<UserProfileResponse> profiles = striderUserRepository.findUserProfilesByFollowers(targetUserId, currentUserId);
        List<String> targetUserIds = profiles.stream()
                .map(UserProfileResponse::userId)
                .toList();

        if (targetUserIds.isEmpty()) return List.of();

        // 2. 현재 사용자가 팔로우/팔로우한 유저 관계 조회
        Map<String, FollowRelationResponse> relationMap =
                userFollowRepository.findFollowRelations(currentUserId, targetUserIds)
                        .stream()
                        .collect(Collectors.toMap(
                                FollowRelationResponse::userId,
                                Function.identity()
                        ));

        // 3. UserFollowResponse 매핑
        return profiles.stream()
                .map(profile -> {
                    FollowRelationResponse relation =
                            relationMap.get(profile.userId());

                    return UserFollowResponse.fromUserProfile(
                            profile,
                            relation != null ? relation.followStatus() : "NOT_FOLLOWING",
                            relation != null && relation.isFollower()
                    );

                })
                .collect(Collectors.toList());
    }


    // 팔로우
    @Transactional
    public void follow(String followerId, String followingId) {
        if (userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new StriderException(StriderErrorCodes.BAD_REQUEST);
        }

        // 비공계 계정 여부 확인
        StriderUser followingUser = striderUserRepository.findByUserId(followingId)
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        boolean isPublic = followingUser.getIsPublic();

        userFollowRepository.save(UserFollow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .isAccepted(isPublic)
                .build());

        // 알림 발행: 공개 계정 → 즉시 팔로우(L1), 비공개 계정 → 팔로우 요청(L2)
        // resourceId = 팔로워 id (앱에서 팔로워 프로필로 이동용)
        if (isPublic) {
            notificationEventProducer.send(new com.strider.user_profile.kafka.event.NotificationEvent(
                    com.strider.user_profile.kafka.event.NotificationEvent.TYPE_FOLLOW,
                    followingId,
                    followerId,
                    followerId,
                    "회원님을 팔로우했어요!",
                    java.time.LocalDateTime.now()
            ));
        } else {
            notificationEventProducer.send(new com.strider.user_profile.kafka.event.NotificationEvent(
                    com.strider.user_profile.kafka.event.NotificationEvent.TYPE_FOLLOW_REQUEST,
                    followingId,
                    followerId,
                    followerId,
                    "회원님에게 팔로우를 요청했어요!",
                    java.time.LocalDateTime.now()
            ));
        }
    }

    // 언팔로우
    @Transactional
    public void unfollow(String followerId, String followingId) {
        userFollowRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }

    // 팔로우 요청 수락 (userId = 요청 받은 사람, followerId = 요청한 사람)
    @Transactional
    public void acceptFollowRequest(String userId, String followerId) {
        UserFollow follow = userFollowRepository
                .findByFollowerIdAndFollowingId(followerId, userId)
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        // 이미 수락된 요청
        if (Boolean.TRUE.equals(follow.getIsAccepted())) {
            throw new StriderException(StriderErrorCodes.BAD_REQUEST);
        }

        follow.setIsAccepted(true);

        // L3: 팔로우 요청 수락 알림 → 요청자에게
        notificationEventProducer.send(new com.strider.user_profile.kafka.event.NotificationEvent(
                com.strider.user_profile.kafka.event.NotificationEvent.TYPE_FOLLOW_ACCEPTED,
                followerId,
                userId,
                userId,
                "회원님의 팔로우 요청을 수락했어요!",
                java.time.LocalDateTime.now()
        ));
    }

    // 팔로우 요청 거절 (userId = 요청 받은 사람, followerId = 요청한 사람)
    @Transactional
    public void rejectFollowRequest(String userId, String followerId) {
        UserFollow follow = userFollowRepository
                .findByFollowerIdAndFollowingId(followerId, userId)
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        userFollowRepository.delete(follow);
    }

    public List<UserFollowResponse> getFollowRelation(String targetUserId, String currentUserId) {
        // 1. 내가 팔로잉한 사람 목록
        List<UserProfileResponse> followingProfiles = striderUserRepository.findUserProfilesByFollowing(targetUserId,currentUserId);

        // 2. 나를 팔로우한 사람 목록
        List<UserProfileResponse> followerProfiles = striderUserRepository.findUserProfilesByFollowers(targetUserId,currentUserId);

        // 3. 두 리스트 합치고 중복 제거 (userId 기준)
        Map<String, UserProfileResponse> combinedProfiles = new LinkedHashMap<>();

        followingProfiles.forEach(profile -> combinedProfiles.put(profile.userId(), profile));
        followerProfiles.forEach(profile -> combinedProfiles.put(profile.userId(), profile));

        if (combinedProfiles.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. 한 번에 팔로우 상태 조회 (N+1 방지)
        List<String> userIds = new ArrayList<>(combinedProfiles.keySet());

        // 5. 현재 사용자가 팔로우/팔로우한 유저 관계 조회
        Map<String, FollowRelationResponse> relationMap =
                userFollowRepository.findFollowRelations(currentUserId, userIds)
                        .stream()
                        .collect(Collectors.toMap(
                                FollowRelationResponse::userId,
                                Function.identity()
                        ));

        // 6. UserFollowResponse 매핑
        return combinedProfiles.values().stream()
                .map(profile -> {
                    FollowRelationResponse relation =
                            relationMap.get(profile.userId());

                    return UserFollowResponse.fromUserProfile(
                            profile,
                            relation != null ? relation.followStatus() : "NOT_FOLLOWING",
                            relation != null && relation.isFollower()
                    );

                })
                .collect(Collectors.toList());
    }
}