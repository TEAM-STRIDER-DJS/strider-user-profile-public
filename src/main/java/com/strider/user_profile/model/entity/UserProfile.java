package com.strider.user_profile.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "profile_id", nullable = false, unique = true)
    private String profileId;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "profile_thumb_image")
    private String profileThumbImage;

    @Column(name = "profile_desc")
    private String profileDesc;

    @Column(name = "rank_id")
    private String rankId;

    @Column(name = "profile_image_S3_key")
    private String profileImageS3Key;

    @Column(name = "profile_thumb_S3_key")
    private String profileThumbS3Key;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
