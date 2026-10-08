package com.strider.user_profile.model.request;

public record StriderLogoutRequest(
        String userId,
        String refreshToken
) {
}
