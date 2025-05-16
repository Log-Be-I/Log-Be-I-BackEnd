package com.logbei.be.pushToken.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.logbei.be.pushToken.dto.NotificationRequestDto;
import com.logbei.be.pushToken.dto.PushTokenDto;
import com.logbei.be.pushToken.service.PushTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/push")
public class PushTokenController {
    private final PushTokenService pushTokenService;

    public PushTokenController(PushTokenService pushTokenService) {
        this.pushTokenService = pushTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerToken(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @RequestBody PushTokenDto pushTokenDto
            ) {
        pushTokenService.registerToken(customPrincipal.getMemberId(), pushTokenDto.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unregister")
    public ResponseEntity<Void> unregisterToken(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @RequestBody PushTokenDto pushTokenDto
    ) {
        pushTokenService.deleteToken(pushTokenDto.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notice")
    public ResponseEntity<Void> sendNoticeNotification(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @RequestBody NotificationRequestDto requestDto
            ){
        pushTokenService.sendNoticeNotification(
                customPrincipal.getMemberId(),
                requestDto.getTitle(),
                requestDto.getContent()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/schedule")
    public ResponseEntity<Void> sendScheduleNotification(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @RequestBody NotificationRequestDto requestDto
    ){
        pushTokenService.sendNoticeNotification(
                customPrincipal.getMemberId(),
                requestDto.getTitle(),
                requestDto.getContent()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/report")
    public ResponseEntity<Void> sendReportNotification(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @RequestBody NotificationRequestDto requestDto
    ){
        pushTokenService.sendNoticeNotification(
                customPrincipal.getMemberId(),
                requestDto.getTitle(),
                requestDto.getContent()
        );

        return ResponseEntity.ok().build();
    }
}
