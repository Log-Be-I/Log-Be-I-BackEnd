package com.springboot.log.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 불필요 스텁 무시
class LogStorageServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;     // RedisTemplate Mock
    @Mock
    private ListOperations<String, String> listOperations;   // Redis list 연산용 Mock

    @Spy
    @InjectMocks
    private LogStorageService logStorageService;              // 테스트 대상 클래스 (Spy)

    /**
     * storeInfoLog: 정상적으로 Redis에 rightPush가 호출되는지 검증
     */
    @Test
    void storeInfoLog_success() {
        // given
        when(redisTemplate.opsForList()).thenReturn(listOperations); // 필요한 시점에만 stub
        String message = "로그 메세지";
        String partName = "module";
        LocalDate today = LocalDate.now();
        String expectedKey = "logs:" + today + "." + partName;

        // when
        logStorageService.storeInfoLog(message, partName);

        // then
        verify(listOperations, times(1)).rightPush(expectedKey, message);
    }

    /**
     * logAndStoreWithError: 포맷팅된 메시지가 storeInfoLog에 전달되는지 검증
     */
    @Test
    void logAndStoreWithError_callsStoreInfoLogWithFormattedMsg() {
        // given
        String formatMessage = "에러 발생: {}";
        String part = "err";
        doNothing().when(logStorageService).storeInfoLog(anyString(), eq(part)); // 직접 stub

        // when
        logStorageService.logAndStoreWithError(formatMessage, part, "오류코드");

        // then
        verify(logStorageService, times(1)).storeInfoLog("에러 발생: 오류코드", part);
    }

    /**
     * logAndStore: 단순 메시지가 storeInfoLog에 전달되는지 검증
     */
    @Test
    void logAndStore_callsStoreInfoLogDirectly() {
        // given
        String message = "정보 저장";
        String part = "info";
        doNothing().when(logStorageService).storeInfoLog(message, part);

        // when
        logStorageService.logAndStore(message, part);

        // then
        verify(logStorageService).storeInfoLog(message, part);
    }

    /**
     * logErrorAndStore: 예외 메시지가 포맷팅되어 storeInfoLog에 전달되는지 검증
     */
    @Test
    void logErrorAndStore_callsStoreInfoLogWithErrorFormat() {
        // given
        String msg = "치명적 에러";
        String part = "fatal";
        Exception e = new RuntimeException("원인");
        doNothing().when(logStorageService).storeInfoLog(anyString(), eq(part));
        String expected = "치명적 에러 - reason: 원인";

        // when
        logStorageService.logErrorAndStore(msg, part, e);

        // then
        verify(logStorageService).storeInfoLog(expected, part);
    }
}
