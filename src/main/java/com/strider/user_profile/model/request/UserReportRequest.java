package com.strider.user_profile.model.request;

public record UserReportRequest(
        String reasonCode,
        String reasonText
) {
}