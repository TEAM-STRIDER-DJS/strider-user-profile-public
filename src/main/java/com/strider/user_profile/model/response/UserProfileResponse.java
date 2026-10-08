package com.strider.user_profile.model.response;

import lombok.Builder;

@Builder
public record UserProfileResponse(
        String userId,
        String profileId,
        String nickname,
        String profileImage,
        String profileThumbImage,
        String profileDesc,
        String rankId,
        int postCount,
        int followingCount,
        int followerCount,
        String followStatus,
        boolean isFollower,
        boolean isPublic,
        boolean isBlock
) {
}