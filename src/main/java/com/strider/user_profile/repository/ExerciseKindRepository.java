package com.strider.user_profile.repository;

import com.strider.user_profile.model.entity.ExerciseKind;
import com.strider.user_profile.model.response.ExerciseItemResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ExerciseKindRepository extends JpaRepository<ExerciseKind, String> {
    @Query("SELECT e.name_ko FROM ExerciseKind e")
    List<String> findAllNameKo();

    @Query("SELECT new com.strider.user_profile.model.response.ExerciseItemResponse(e.id, e.name_ko, e.name_en, e.kind) FROM ExerciseKind e")
    List<ExerciseItemResponse> findAllExerciseItems();
}
