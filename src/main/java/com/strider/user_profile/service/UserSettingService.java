package com.strider.user_profile.service;

import com.strider.user_profile.model.entity.UserSetting;
import com.strider.user_profile.model.enums.SettingKey;
import com.strider.user_profile.model.enums.SettingType;
import com.strider.user_profile.model.enums.SettingValue;
import com.strider.user_profile.model.request.UserSettingUpdateRequest;
import com.strider.user_profile.model.response.UserSettingResponse;
import com.strider.user_profile.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSettingService {
    private final UserSettingRepository userSettingRepository;

    public UserSettingResponse getUserSettings(String userId) {
        List<UserSetting> settings = userSettingRepository.findByUserId(userId);

        Map<SettingType, Map<SettingKey, SettingValue>> result = new EnumMap<>(SettingType.class);

        for (UserSetting s : settings) {
            result.computeIfAbsent(s.getSettingType(), k -> new EnumMap<>(SettingKey.class))
                    .put(s.getSettingKey(), s.getSettingValue());
        }

        return UserSettingResponse.builder()
                .userId(userId)
                .settings(result)
                .build();
    }

    @Transactional
    public void updateUserSettings(String userId, UserSettingUpdateRequest request) {
        List<UserSetting> currentSettings = userSettingRepository.findByUserId(userId);
        Map<String, UserSetting> settingMap = new HashMap<>();
        for (UserSetting s : currentSettings) {
            String key = s.getSettingType().name() + "_" + s.getSettingKey().name();
            settingMap.put(key, s);
        }

        request.settings().forEach((typeStr, keyValues) -> {
            SettingType type = SettingType.valueOf(typeStr.toUpperCase());
            keyValues.forEach((keyStr, valueStr) -> {
                SettingKey key = SettingKey.valueOf(keyStr.toUpperCase());
                SettingValue value = SettingValue.valueOf(valueStr.toUpperCase());

                String mapKey = type.name() + "_" + key.name();
                if (settingMap.containsKey(mapKey)) {
                    UserSetting s = settingMap.get(mapKey);
                    s.setSettingValue(value);
                } else {
                    UserSetting s = UserSetting.builder()
                            .userId(userId)
                            .settingType(type)
                            .settingKey(key)
                            .settingValue(value)
                            .build();
                    userSettingRepository.save(s);
                }
            });
        });
    }
}