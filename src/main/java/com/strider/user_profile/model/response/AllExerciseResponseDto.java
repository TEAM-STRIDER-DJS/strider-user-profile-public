package com.strider.user_profile.model.response;

import lombok.Builder;

import java.util.List;

@Builder
public record AllExerciseResponseDto(List<ExerciseItemResponse> exercises) {
}
