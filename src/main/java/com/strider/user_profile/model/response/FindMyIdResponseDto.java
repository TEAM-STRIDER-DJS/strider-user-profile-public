package com.strider.user_profile.model.response;

import lombok.Builder;

@Builder
public record FindMyIdResponseDto(
        String provider, String maskedProviderId
) {
}
