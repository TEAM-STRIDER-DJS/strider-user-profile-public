package com.strider.user_profile.model.request;

public record UpdatePasswordRequest(
        String userId,      // Strider login id - matched with providerId of User Entity
        String currentPassword,
        String newPassword
) {
}
