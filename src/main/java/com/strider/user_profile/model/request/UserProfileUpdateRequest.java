package com.strider.user_profile.model.request;

public record UserProfileUpdateRequest (
        String userId,
        String nickname,
        String profileDesc
) {
}