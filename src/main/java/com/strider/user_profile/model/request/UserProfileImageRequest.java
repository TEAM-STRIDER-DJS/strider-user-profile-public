package com.strider.user_profile.model.request;


public record UserProfileImageRequest(
        String userId,
        String profileImage,
        String profileThumbImage,
        String profileImageS3Key,
        String profileThumbS3Key
) {
}