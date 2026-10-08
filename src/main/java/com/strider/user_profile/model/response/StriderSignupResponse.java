package com.strider.user_profile.model.response;

import lombok.Builder;

@Builder
public record StriderSignupResponse(
        String id,
        String userId,
        String userStatus
) {
}
