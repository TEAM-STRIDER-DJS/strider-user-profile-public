package com.strider.user_profile.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoProfile(
        String nickname,
        @JsonProperty("profile_image_url")
        String profileImageUrl
) {}
