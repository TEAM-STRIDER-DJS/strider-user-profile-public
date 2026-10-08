package com.strider.user_profile.model.request;

import jakarta.validation.constraints.NotBlank;

public record AppleLogInRequest(
        @NotBlank String identityToken
) {
}
