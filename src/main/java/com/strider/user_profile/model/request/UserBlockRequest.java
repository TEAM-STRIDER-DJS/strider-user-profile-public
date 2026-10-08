package com.strider.user_profile.model.request;

public record UserBlockRequest(
        String blockerId,
        String targetType,
        String targetId,
        String reasonCode,
        String reasonText
) {
}