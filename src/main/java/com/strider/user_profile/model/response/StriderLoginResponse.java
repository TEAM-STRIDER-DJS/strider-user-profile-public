package com.strider.user_profile.model.response;

import lombok.Builder;

@Builder
public record StriderLoginResponse(
        String userId,
        String token,
        String refreshToken
) {
}
