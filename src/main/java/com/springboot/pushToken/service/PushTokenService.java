package com.springboot.pushToken.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.springboot.pushToken.entity.PushToken;
import com.springboot.pushToken.repository.PushTokenRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PushTokenService {
    private final PushTokenRepository pushTokenRepository;
    private final MemberRepository memberRepository;
    private final ExpoPushNotificationService expoPushNotificationService;

    public PushTokenService(PushTokenRepository pushTokenRepository, MemberRepository memberRepository, ExpoPushNotificationService expoPushNotificationService) {
        this.pushTokenRepository = pushTokenRepository;
        this.memberRepository = memberRepository;
        this.expoPushNotificationService = expoPushNotificationService;
    }

    // 푸시 토큰 등록
    @Transactional
    public void registerToken(Long memberId, String token) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        // 기존 토큰 있으면 비활성화
        pushTokenRepository.findByToken(token)
                .ifPresent(existingToken -> existingToken.setActive(false));

        // 새 토큰 저장
        PushToken pushToken = new PushToken();
        pushToken.setMember(member);
        pushToken.setToken(token);
        pushToken.setActive(true);

        pushTokenRepository.save(pushToken);
    }

    // 푸시 토큰 삭제 (isActive -> false)
    @Transactional
    public void deleteToken(String token) {
        pushTokenRepository.findByToken(token)
                .ifPresent(pushToken -> {
                    pushToken.setActive(false);
                    pushTokenRepository.save(pushToken);
                });
    }

    // 특정 회원의 활성화된 토큰 조회
    public List<String> getActiveTokensByMemberId(Long memberId) {
        return pushTokenRepository.findByMember_MemberIdAndIsActiveTrue(memberId).stream()
                .map(pushToken -> pushToken.getToken())
                .collect(Collectors.toList());
    }


    // 공지사항 알림 전송
    public void sendNoticeNotification(Long memberId, String title, String content) {
        List<String> tokens = getActiveTokensByMemberId(memberId);
        Map<String, Object> data = new HashMap<>();
        data.put("type", "notice");
        data.put("noticeId", title);

        expoPushNotificationService.sendPushNotifications(
                tokens,
                "새로운 공지사항이 등록되었습니다.",
                content,
                data
        );
    }

    // 일정 알림 전송
    public void sendScheduleNotification(Long memberId, String title, String content) {
        List<String> tokens = getActiveTokensByMemberId(memberId);
        Map<String, Object> data = new HashMap<>();
        data.put("type", "schedule");
        data.put("scheduleId", title);

        expoPushNotificationService.sendPushNotifications(
                tokens,
                "일정 알림",
                content,
                data
        );
    }

    // 분석 결과 알림 전송
    public void sendAnalysisNotification(Long memberId, String title, String content){
        List<String> tokens = getActiveTokensByMemberId(memberId);
        Map<String, Object> data = new HashMap<>();
        data.put("type", "report");
        data.put("reportId", title);

        expoPushNotificationService.sendPushNotifications(
                tokens,
                "분석 결과가 도착했습니다.",
                content,
                data
        );
    }

}
