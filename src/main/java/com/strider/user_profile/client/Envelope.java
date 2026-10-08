package com.strider.user_profile.client;

/**
 * 타 서비스 응답 래퍼 ({ data, meta }) 역직렬화용
 */
public record Envelope<T>(
        T data,
        Meta meta
) {
    public record Meta(
            String status,
            Integer code,
            String message
    ) {}
}
