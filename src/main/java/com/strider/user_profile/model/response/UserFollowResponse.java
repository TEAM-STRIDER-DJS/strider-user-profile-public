package com.strider.user_profile.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserFollowResponse {
    private String userId;
    private String profileId;
    private String nickname;
    private String profileImage;
    private String profileThumbImage;
    private String profileDesc;
    private String rankId;
    private int postCount;
    private int followingCount;
    private int followerCount;
    private String followStatus;
    private boolean isFollower;
    private boolean isPublic;

    public static UserFollowResponse fromUserProfile(UserProfileResponse profile, String followStatus, boolean isFollower) {
        return UserFollowResponse.builder()
                .userId(profile.userId())
                .profileId(profile.profileId())
                .nickname(profile.nickname())
                .profileImage(profile.profileImage())
                .profileThumbImage(profile.profileThumbImage())
                .profileDesc(profile.profileDesc())
                .rankId(profile.rankId())
                .postCount(profile.postCount())
                .followingCount(profile.followingCount())
                .followerCount(profile.followerCount())
                .followStatus(followStatus)
                .isFollower(isFollower)
                .isPublic(profile.isPublic())
                .build();
    }
}