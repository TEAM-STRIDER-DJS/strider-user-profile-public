package com.strider.user_profile.model.entity;

import com.strider.user_profile.model.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "strider_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StriderUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "provider_id", nullable = false, length = 160)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 16)
    private AuthProvider provider;

    @Column(name = "password")
    private String password;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Builder.Default
    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "user_status", nullable = false)
    @Builder.Default
    private String userStatus = "ACTIVE";

    @Column(name = "is_agreed_marketing", nullable = false)
    @Builder.Default
    private Boolean isAgreedMarketing = false;

    @Column(name = "is_agreed_marketing_push")
    @Builder.Default
    private Boolean isAgreedMarketingPush = false;

    @Column(name = "is_agreed_marketing_email")
    @Builder.Default
    private Boolean isAgreedMarketingEmail = false;

    @Column(name = "is_agreed_marketing_sms")
    @Builder.Default
    private Boolean isAgreedMarketingSMS = true;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;
}
