package com.strider.user_profile.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Column(name = "reason_code", nullable = false)
    private String reasonCode;

    @Column(name = "reason_text")
    private String reasonText;

    @Column(name = "status")
    private String status;

    @Column(name = "action_taken")
    private String actionTaken;

    @Column(name = "reviewed_at")
    @CreationTimestamp
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}