package com.strider.user_profile.model.response;

import lombok.Builder;

@Builder
public record SocialSignupResponse(
        String id,
        String userId,
        String userStatus,
        String accessToken,
        String refreshToken
) {
}
