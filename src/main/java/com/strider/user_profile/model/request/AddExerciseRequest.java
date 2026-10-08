package com.strider.user_profile.model.request;

public record AddExerciseRequest(
        String name_ko,
        String name_en,
        String kind
) {
}
