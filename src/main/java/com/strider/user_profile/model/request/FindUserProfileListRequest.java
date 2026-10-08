package com.strider.user_profile.model.request;

import java.util.List;

public record FindUserProfileListRequest(
        List<String> userIds
) {}
