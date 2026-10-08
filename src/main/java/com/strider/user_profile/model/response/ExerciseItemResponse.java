package com.strider.user_profile.model.response;

import lombok.Builder;

@Builder
public record ExerciseItemResponse(
        String id,
        String nameKo,
        String nameEn,
        String kind
) {}
