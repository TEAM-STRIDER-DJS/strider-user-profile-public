package com.strider.user_profile.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_follow",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}),
        indexes = {
                @Index(name = "idx_follower", columnList = "follower_id"),
                @Index(name = "idx_following", columnList = "following_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "follower_id", nullable = false)
    private String followerId;

    @Column(name = "following_id", nullable = false)
    private String followingId;

    @Column(name = "is_accepted", nullable = false)
    @Builder.Default
    private Boolean isAccepted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}