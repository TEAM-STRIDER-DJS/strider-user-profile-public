package com.strider.user_profile.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "issued_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime issuedAt;

    public void updateRefreshToken(String newRefreshToken){
        this.refreshToken = newRefreshToken;
    }
}
