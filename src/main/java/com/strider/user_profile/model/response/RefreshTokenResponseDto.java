package com.strider.user_profile.model.response;

import lombok.Builder;

@Builder
public record RefreshTokenResponseDto(String accessToken) {
}
