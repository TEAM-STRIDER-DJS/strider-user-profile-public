package com.strider.user_profile.controller;

import com.strider.strider_common_lib.response.StriderResponse;
import com.strider.user_profile.model.request.AddExerciseRequest;
import com.strider.user_profile.model.response.AllExerciseResponseDto;
import com.strider.user_profile.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/profile/exercises")
public class ExerciseRestController {
    private final ExerciseService exerciseService;

    @GetMapping("")
    public ResponseEntity<StriderResponse<AllExerciseResponseDto>> getAllExercises(
            @RequestHeader HttpHeaders headers) {
        AllExerciseResponseDto response = exerciseService.getAllKoreanNameOfExercises(headers);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, AllExerciseResponseDto.class));
    }

    @PostMapping("")
    public ResponseEntity<StriderResponse<Void>> addExercise(
            @RequestHeader HttpHeaders headers, @RequestBody AddExerciseRequest request
    ) {
        exerciseService.addExercises(headers, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }
}
