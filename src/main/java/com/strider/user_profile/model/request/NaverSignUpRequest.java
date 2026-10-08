package com.strider.user_profile.model.request;

import jakarta.validation.constraints.NotBlank;

public record NaverSignUpRequest(
        @NotBlank String accessToken,
        String phoneNumber,
        Boolean marketing,
        Boolean marketing_push,
        Boolean marketing_email,
        Boolean marketing_sms
) {
}
