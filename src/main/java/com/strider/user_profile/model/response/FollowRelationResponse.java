package com.strider.user_profile.model.response;

import lombok.Builder;

@Builder
public record FollowRelationResponse(
        String userId, String followStatus, boolean isFollower
) {
}

