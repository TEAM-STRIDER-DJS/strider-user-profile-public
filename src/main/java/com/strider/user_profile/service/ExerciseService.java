package com.strider.user_profile.service;

import com.strider.user_profile.model.entity.ExerciseKind;
import com.strider.user_profile.model.request.AddExerciseRequest;
import com.strider.user_profile.model.response.AllExerciseResponseDto;
import com.strider.user_profile.model.response.ExerciseItemResponse;
import com.strider.user_profile.repository.ExerciseKindRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseKindRepository exerciseKindRepository;
    public AllExerciseResponseDto getAllKoreanNameOfExercises(HttpHeaders headers) {
        List<ExerciseItemResponse> exercises = exerciseKindRepository.findAllExerciseItems();
        return AllExerciseResponseDto.builder()
                .exercises(exercises)
                .build();
    }

    public void addExercises(HttpHeaders headers, AddExerciseRequest request) {
        exerciseKindRepository.save(ExerciseKind.builder()
                .kind(request.kind())
                .name_ko(request.name_ko())
                .name_en(request.name_en())
                .build());
    }
}
