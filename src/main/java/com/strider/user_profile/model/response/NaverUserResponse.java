package com.strider.user_profile.model.response;

public record NaverUserResponse(
        String resultCode,
        String message,
        NaverUserInfo naverUserInfo
) {
}
