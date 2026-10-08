package com.strider.user_profile.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    @Value("${solapi.api.key}")
    private String apiKey;

    @Value("${solapi.api.secret}")
    private String apiSecret;

    @Value("${solapi.sms.sender}")
    private String senderPhone;

    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.solapi.com");
    }

    public void sendSms(String to, String content) {
        try {
            Message message = new Message();
            message.setFrom(senderPhone);
            message.setTo(to);
            message.setText(content);
            messageService.send(message);

            log.info("SMS 전송 성공 - 수신자: {}, 내용: {}", to, content);
        } catch (Exception e) {
            log.error("SMS 전송 실패 - 수신자: {}, 에러: {}", to, e.getMessage());
            throw new RuntimeException("문자 전송에 실패했습니다.");
        }
    }
}
