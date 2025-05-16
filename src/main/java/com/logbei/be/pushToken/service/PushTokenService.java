package com.logbei.be.pushToken.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.logbei.be.pushToken.entity.PushToken;
import com.logbei.be.pushToken.repository.PushTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
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
        log.info("푸시 토큰 등록 시작 - 회원ID: {}, 토큰: {}", memberId, token);
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        // 기존 토큰 있으면 비활성화
        pushTokenRepository.findByTokenAndIsActive(token, true)
                .ifPresent(existingToken -> {
                    log.info("기존 토큰 비활성화 - 토큰: {}", token);
                    existingToken.setActive(false);
                });

        // 새 토큰 저장
        PushToken pushToken = new PushToken();
        pushToken.setMember(member);
        pushToken.setToken(token);
        pushToken.setActive(true);

        pushTokenRepository.save(pushToken);
        log.info("새 푸시 토큰 등록 완료 - 회원ID: {}, 토큰: {}", memberId, token);
    }

    // 푸시 토큰 삭제 (isActive -> false)
    @Transactional
    public void deleteToken(String token) {
        log.info("푸시 토큰 삭제 시작 - 토큰: {}", token);
        pushTokenRepository.findByTokenAndIsActive(token, true)
                .ifPresent(pushToken -> {
                    pushToken.setActive(false);
                    pushTokenRepository.save(pushToken);
                    log.info("푸시 토큰 삭제 완료 - 토큰: {}", token);
                });
    }

    // 특정 회원의 활성화된 토큰 조회
    public List<String> getActiveTokensByMemberId(Long memberId) {
        List<String> tokens = pushTokenRepository.findByMember_MemberIdAndIsActiveTrue(memberId).stream()
                .map(PushToken::getToken)
                .collect(Collectors.toList());
        log.info("활성화된 푸시 토큰 조회 - 회원ID: {}, 토큰 수: {}", memberId, tokens.size());
        return tokens;
    }

    // 공지사항 알림 전송
    public void sendNoticeNotification(Long memberId, String title, String content) {
        log.info("공지사항 알림 전송 시작 - 회원ID: {}, 제목: {}", memberId, title);
        List<String> tokens = getActiveTokensByMemberId(memberId);
        
        if (tokens.isEmpty()) {
            log.warn("활성화된 푸시 토큰이 없습니다 - 회원ID: {}", memberId);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("type", "notice");
        data.put("noticeId", title);

        expoPushNotificationService.sendPushNotifications(
                tokens,
                "새로운 공지사항이 등록되었습니다.",
                content,
                data
        );
        log.info("공지사항 알림 전송 완료 - 회원ID: {}, 토큰 수: {}", memberId, tokens.size());
    }

    // 일정 알림 전송
    public void sendScheduleNotification(Long memberId, String title, String content) {
        log.info("일정 알림 전송 시작 - 회원ID: {}, 제목: {}", memberId, title);
        List<String> tokens = getActiveTokensByMemberId(memberId);
        
        if (tokens.isEmpty()) {
            log.warn("활성화된 푸시 토큰이 없습니다 - 회원ID: {}", memberId);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("type", "schedule");
        data.put("scheduleId", title);

        expoPushNotificationService.sendPushNotifications(
                tokens,
                "일정 알림",
                content,
                data
        );
        log.info("일정 알림 전송 완료 - 회원ID: {}, 토큰 수: {}", memberId, tokens.size());
    }

    // 분석 결과 알림 전송
    public void sendAnalysisNotification(Long memberId, String title, String content){
        log.info("분석 결과 알림 전송 시작 - 회원ID: {}, 제목: {}", memberId, title);
        List<String> tokens = getActiveTokensByMemberId(memberId);
        
        if (tokens.isEmpty()) {
            log.warn("활성화된 푸시 토큰이 없습니다 - 회원ID: {}", memberId);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("type", "report");
        data.put("reportId", title);

        expoPushNotificationService.sendPushNotifications(
                tokens,
                "분석 결과가 도착했습니다.",
                content,
                data
        );
        log.info("분석 결과 알림 전송 완료 - 회원ID: {}, 토큰 수: {}", memberId, tokens.size());
    }
}
