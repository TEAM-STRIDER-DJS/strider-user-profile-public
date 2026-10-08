package com.strider.user_profile.model.request;

public record VerifyCodeRequest(
        String phoneNumber,
        String inputCode
) {
}
