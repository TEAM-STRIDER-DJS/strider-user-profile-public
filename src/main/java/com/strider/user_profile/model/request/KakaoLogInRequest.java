package com.strider.user_profile.model.request;

import jakarta.validation.constraints.NotBlank;

public record KakaoLogInRequest(
        @NotBlank String accessToken,
        String refreshToken,
        Long expiresIn,
        Long refreshTokenExpiresIn
) {
}
