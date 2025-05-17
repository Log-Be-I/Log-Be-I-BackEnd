package com.logbei.be.pushToken.controller;

import com.logbei.be.auth.utils.CustomPrincipal;
import com.logbei.be.pushToken.controller.PushTokenController;
import com.logbei.be.pushToken.dto.NotificationRequestDto;
import com.logbei.be.pushToken.dto.PushTokenDto;
import com.logbei.be.pushToken.service.PushTokenService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushTokenControllerTest {

    @Mock
    private PushTokenService pushTokenService;

    @InjectMocks
    private PushTokenController pushTokenController;

    @Test
    @DisplayName("푸시 토큰 등록 테스트")
    void registerToken() {
        // given
        CustomPrincipal customPrincipal = new CustomPrincipal("test@gmail.com", 1L);
        PushTokenDto request = new PushTokenDto();
        request.setToken("ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]");

        // when
        ResponseEntity<Void> response = pushTokenController.registerToken(customPrincipal, request);

        // then
        assertEquals(200, response.getStatusCodeValue());
        verify(pushTokenService).registerToken(eq(customPrincipal.getMemberId()), eq(request.getToken()));
    }

    @Test
    @DisplayName("푸시 토큰 삭제 테스트")
    void unregisterToken() {
        // given
        CustomPrincipal customPrincipal = new CustomPrincipal("test@gmail.com", 1L);
        PushTokenDto request = new PushTokenDto();
        request.setToken("ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]");

        // when
        ResponseEntity<Void> response = pushTokenController.unregisterToken(customPrincipal, request);

        // then
        assertEquals(200, response.getStatusCodeValue());
        verify(pushTokenService).deleteToken(eq(request.getToken()));
    }

    @Test
    @DisplayName("공지사항 알림 전송 테스트")
    void sendNoticeNotification() {
        // given
        CustomPrincipal customPrincipal = new CustomPrincipal("test@gmail.com", 1L);
        NotificationRequestDto request = new NotificationRequestDto();
        request.setTitle("테스트 공지사항");
        request.setContent("테스트 내용");

        // when
        ResponseEntity<Void> response = pushTokenController.sendNoticeNotification(customPrincipal, request);

        // then
        assertEquals(200, response.getStatusCodeValue());
        verify(pushTokenService).sendNoticeNotification(
                eq(customPrincipal.getMemberId()),
                eq(request.getTitle()),
                eq(request.getContent())
        );
    }
}