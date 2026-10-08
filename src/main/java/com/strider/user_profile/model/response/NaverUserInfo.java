package com.strider.user_profile.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverUserInfo(
        String id,
        String email,
        String name,
        @JsonProperty("profile_image")
        String profileImage
) {
}
