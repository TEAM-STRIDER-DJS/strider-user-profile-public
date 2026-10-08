package com.strider.user_profile.model.request;

import java.util.Map;

public record UserSettingUpdateRequest(
        Map<String, Map<String, String>> settings
) {
}