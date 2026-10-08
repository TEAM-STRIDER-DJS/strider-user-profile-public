package com.strider.user_profile.model.request;

public record StriderUserDeleteRequest(
        String userId,
        String refreshToken
) {
}
