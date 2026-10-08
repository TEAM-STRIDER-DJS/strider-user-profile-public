package com.strider.user_profile.kafka.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * strider-notification 으로 발행하는 알림 이벤트.
 *
 * 필드명/형식은 strider-notification 의 NotificationEvent 와 정확히 일치해야 한다
 * (consumer 가 타입헤더를 무시하고 JSON 필드명으로 역직렬화함).
 *
 * eventType 은 strider-notification 의 NotificationType enum 이름과 일치해야 한다.
 *   사용 값: FOLLOW, FOLLOW_REQUEST (수락 알림 FOLLOW_ACCEPTED 는 수락 기능 추가 후)
 */
public record NotificationEvent(
        String eventType,
        String receiverUserId,
        String actorUserId,
        String resourceId,
        String content,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {
    public static final String TYPE_FOLLOW = "FOLLOW";
    public static final String TYPE_FOLLOW_REQUEST = "FOLLOW_REQUEST";
    public static final String TYPE_FOLLOW_ACCEPTED = "FOLLOW_ACCEPTED";
}
