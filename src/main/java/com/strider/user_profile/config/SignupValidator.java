package com.strider.user_profile.config;

import com.strider.strider_common_lib.error.StriderErrorCodes;
import com.strider.strider_common_lib.exception.StriderException;

import java.util.regex.Pattern;

public class SignupValidator {

    private static final Pattern ID_PATTERN = Pattern.compile("^[a-z0-9]{6,}$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$");

    public static void validate(String providerId, String password) {
        if (!ID_PATTERN.matcher(providerId).matches()) {
            throw new StriderException(StriderErrorCodes.BAD_REQUEST, "Invalid ID pattern");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new StriderException(StriderErrorCodes.BAD_REQUEST, "Invalid Password pattern");
        }
    }
}

