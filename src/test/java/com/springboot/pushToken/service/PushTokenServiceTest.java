package com.springboot.pushToken.service;

import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.springboot.pushToken.entity.PushToken;
import com.springboot.pushToken.repository.PushTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushTokenServiceTest {

    @Mock
    private PushTokenRepository pushTokenRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ExpoPushNotificationService expoPushNotificationService;

    @InjectMocks
    private PushTokenService pushTokenService;

    @Test
    @DisplayName("푸시 토큰 등록 테스트")
    void registerToken() {
        // given
        Long memberId = 1L;
        String token = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]";
        Member member = new Member();
        member.setMemberId(memberId);

        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(member));
        when(pushTokenRepository.findByTokenAndIsActive(token, true))
                .thenReturn(Optional.empty());

        // when
        pushTokenService.registerToken(memberId, token);

        // then
        verify(pushTokenRepository).save(any(PushToken.class));
    }

    @Test
    @DisplayName("푸시 토큰 삭제 테스트")
    void deleteToken() {
        // given
        String token = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]";
        PushToken pushToken =  new PushToken();
        pushToken.setToken(token);
        pushToken.setActive(true);

        when(pushTokenRepository.findByTokenAndIsActive(token, true))
                .thenReturn(Optional.of(pushToken));

        // when
        pushTokenService.deleteToken(token);

        // then
        verify(pushTokenRepository).save(argThat(eachToken -> !eachToken.isActive()));
    }

    @Test
    @DisplayName("회원의 활성화된 토큰 조회 테스트")
    void getActiveTokensByMemberId() {
        // given
        Long memberId = 1L;
        PushToken t1 = new PushToken();
        t1.setToken("token1");
        t1.setActive(true);

        PushToken t2 = new PushToken();
        t2.setToken("token2");
        t2.setActive(true);

        List<PushToken> tokens = List.of(
                t1, t2
        );

        when(pushTokenRepository.findByMember_MemberIdAndIsActiveTrue(memberId))
                .thenReturn(tokens);

        // when
        List<String> activeTokens = pushTokenService.getActiveTokensByMemberId(memberId);

        // then
        assertEquals(2, activeTokens.size());
        assertTrue(activeTokens.contains("token1"));
        assertTrue(activeTokens.contains("token2"));
    }

    @Test
    @DisplayName("공지사항 알림 전송 테스트")
    void sendNoticeNotification() {
        // given
        Long memberId = 1L;
        String title = "테스트 공지사항";
        String content = "테스트 내용";
        List<String> tokens = List.of("ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]");
        PushToken t1 = new PushToken();
        t1.setToken(tokens.get(0));
        when(pushTokenRepository.findByMember_MemberIdAndIsActiveTrue(memberId))
                .thenReturn(List.of(t1));

        // when
        pushTokenService.sendNoticeNotification(memberId, title, content);

        // then
        verify(expoPushNotificationService).sendPushNotifications(
                eq(tokens),
                eq("새로운 공지사항이 등록되었습니다."),  // 실제 서비스의 제목으로 수정
                eq(content),
                argThat(data ->
                        data.containsKey("type") &&
                                data.get("type").equals("notice") &&
                                data.containsKey("noticeId") &&
                                data.get("noticeId").equals(title)
                )
        );
    }

    @Test
    @DisplayName("분석 결과 알림 전송 테스트")
    void sendAnalysisNotification() {
        // given
        Long memberId = 1L;
        String title = "테스트 분석 결과";
        String content = "테스트 분석 내용";
        List<String> tokens = List.of("ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]");
        PushToken t1 = new PushToken();
        t1.setToken(tokens.get(0));

        when(pushTokenRepository.findByMember_MemberIdAndIsActiveTrue(memberId))
                .thenReturn(List.of(t1));

        // when
        pushTokenService.sendAnalysisNotification(memberId, title, content);

        // then
        verify(expoPushNotificationService).sendPushNotifications(
                eq(tokens),
                eq("분석 결과가 도착했습니다."),  // 실제 서비스의 제목으로 수정
                eq(content),
                argThat(data ->
                        data.containsKey("type") &&
                                data.get("type").equals("report") &&
                                data.containsKey("reportId") &&
                                data.get("reportId").equals(title)
                )
        );
    }
}