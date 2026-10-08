package com.strider.user_profile.model.entity;

import com.strider.user_profile.model.enums.SettingKey;
import com.strider.user_profile.model.enums.SettingType;
import com.strider.user_profile.model.enums.SettingValue;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_setting",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "setting_type", "setting_key"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_type", nullable = false, length = 50)
    private SettingType settingType; // FEED, FOLLOW, MESSAGE

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_key", nullable = false, length = 100)
    private SettingKey settingKey;   // FEED_LIKE, FEED_COMMENT 등

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_value", nullable = false, length = 100)
    private SettingValue settingValue; // OFF, ON, FOLLOWING, ALL 등

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}