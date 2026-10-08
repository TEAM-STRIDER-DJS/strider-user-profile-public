package com.strider.user_profile.model.request;

public record StriderSignupRequest(
        String id,      // Strider login id - matched with providerId of User Entity
        String password,
        String phoneNumber,
        Boolean marketing,
        Boolean marketing_push,
        Boolean marketing_email,
        Boolean marketing_sms
) {
}
