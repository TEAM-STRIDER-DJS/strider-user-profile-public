package com.strider.user_profile.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exercise_kind")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseKind {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "name_en", nullable = false, unique = true)
    private String name_en;

    @Column(name = "name_ko", nullable = false, unique = true)
    private String name_ko;

    @Column(name = "kind", nullable = false)
    private String kind;
}
