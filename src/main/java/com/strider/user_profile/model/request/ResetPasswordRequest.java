package com.strider.user_profile.model.request;

public record ResetPasswordRequest(String phoneNumber, String password) {
}
