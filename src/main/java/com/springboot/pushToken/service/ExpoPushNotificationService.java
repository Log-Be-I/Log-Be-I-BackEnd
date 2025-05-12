package com.springboot.pushToken.service;

import com.springboot.pushToken.dto.ExpoPushMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ExpoPushNotificationService {
    private final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private final RestTemplate restTemplate;

    public ExpoPushNotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 단일 알림
    public void sendPushNotification(String token, String title, String body, Map<String, Object> data){
        log.info("푸시 알림 전송 시작 - 토큰: {}, 제목: {}, 내용: {}", token, title, body);
        
        ExpoPushMessageDto message = new ExpoPushMessageDto();
        message.setTo(token);
        message.setTitle(title);
        message.setBody(body);
        message.setData(data);
        message.setSound("default");
        message.setPriority("high");

        try {
            String response = restTemplate.postForEntity(EXPO_PUSH_URL, message, String.class).getBody();
            log.info("푸시 알림 전송 성공 - 토큰: {}, 응답: {}", token, response);
        } catch (Exception e){
            log.error("푸시알림 전송에 실패했습니다. 토큰: {}, 에러: {}", token, e.getMessage(), e);
        }
    }

    // 여러 토큰에 알림
    public void sendPushNotifications(List<String> tokens, String title, String body, Map<String, Object> data) {
        log.info("다중 푸시 알림 전송 시작 - 토큰 수: {}, 제목: {}", tokens.size(), title);
        tokens.forEach(token -> sendPushNotification(token, title, body, data));
        log.info("다중 푸시 알림 전송 완료");
    }
}
