package com.strider.user_profile.model.response;

import lombok.Builder;

@Builder
public record SimpleUserProfileResponse(
        String userId,
        String profileId,
        String nickname,
        String profileImage,
        String profileThumbImage,
        String rankId
) {
}