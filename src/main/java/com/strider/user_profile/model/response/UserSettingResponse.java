package com.strider.user_profile.model.response;

import com.strider.user_profile.model.enums.SettingKey;
import com.strider.user_profile.model.enums.SettingType;
import com.strider.user_profile.model.enums.SettingValue;
import lombok.Builder;

import java.util.Map;

@Builder
public record UserSettingResponse(
        String userId,
        Map<SettingType, Map<SettingKey, SettingValue>> settings
) {
}