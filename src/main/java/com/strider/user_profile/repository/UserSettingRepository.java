package com.strider.user_profile.repository;

import com.strider.user_profile.model.entity.UserSetting;
import com.strider.user_profile.model.enums.SettingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSettingRepository extends JpaRepository<UserSetting, String> {
    List<UserSetting> findByUserId(String userId);
    List<UserSetting> findByUserIdAndSettingType(String userId, SettingType type);
}
