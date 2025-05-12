package com.springboot.log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.springboot.exception.BusinessLogicException;
import com.springboot.log.service.LogStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogPostTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate; // Redis 연동을 위한 템플릿을 Mock 객체로 생성
    @Mock
    private ValueOperations<String, String> valueOperations; // Redis key-value 연산을 위한 Mock
    @Mock
    private AmazonS3 amazonS3; // AWS S3 클라이언트를 Mock으로 생성
    @Mock
    private LogStorageService logStorageService; // 로그 저장 및 오류 처리 서비스 Mock

    @InjectMocks
    private LogPost logPost; // 위 Mock 객체들이 주입된 테스트 대상 클래스

    @BeforeEach
    void setUp() {
        // No unnecessary stubbing here; add only if needed in specific tests
    }

    /**
     * Redis에서 조회된 키가 없을 때의 동작 테스트
     * - keys() 호출 시 빈 Set 반환
     * - 오류 메시지 기록(logAndStoreWithError) 확인
     * - AWS S3 업로드는 호출되지 않아야 함
     */
    @Test
    void givenNoKeys_whenLogPostForS3_thenErrorLoggedAndReturn() {
        // given
        when(redisTemplate.keys("logs:*")).thenReturn(Collections.emptySet()); // 키 없음

        // when
        logPost.logPostForS3(Collections.singletonList("info"), 0, 1);

        // then
        verify(logStorageService, times(1))
                .logAndStoreWithError(eq("No logs found for post between {} and {}"), anyString(), any(), eq("post")); // 오류 로그 확인
        verifyNoInteractions(amazonS3); // S3 호출 없음
    }

    /**
     * Redis에 로그 키가 있고, 정상적으로 S3 업로드 및 Redis 삭제가 되는지 테스트
     * - 2일 전 키 생성
     * - valueOperations.get()에 로그 내용 반환
     * - putObject() 및 delete() 호출 확인
     * - 성공 로그 기록 확인
     */
    @Test
    void givenExistingLogKey_whenLogPostForS3_thenUploadAndDelete() {
        // 날짜 계산 및 키 생성
        LocalDate today = LocalDate.now();
        LocalDate twoDaysAgo = today.minusDays(2);
        String redisKey = "logs:" + twoDaysAgo + ".info"; // 예: logs:2025-05-10.info
        String s3Key = redisKey.replace("logs:", "logs/") + ".log"; // S3 경로: logs/2025-05-10.info.log

        // given
        when(redisTemplate.keys("logs:*")).thenReturn(Set.of(redisKey)); // 키 조회
        when(valueOperations.get(redisKey)).thenReturn("log-content"); // 값 조회

        // when
        logPost.logPostForS3(Collections.singletonList("info"), 0, 1);

        // then
        verify(amazonS3, times(1))
                .putObject(eq("logbe-i-log"), eq(s3Key), eq("log-content")); // S3 업로드 확인
        verify(redisTemplate, times(1)).delete(eq(redisKey)); // Redis 삭제 확인
        verify(logStorageService).logAndStoreWithError(
                eq("Posted {} log files to S3 between {} and {}"),
                eq("1"), eq("2025-05-11"), eq("2025-05-12"), eq("post")
        );
    }

    /**
     * S3 업로드 중 AmazonServiceException 발생 시
     * - BusinessLogicException 던짐
     * - 오류 로그(logAndStoreWithError) 최소 한 번 호출 확인
     */
    @Test
    void givenAmazonServiceException_whenUpload_thenBusinessLogicException() {
        LocalDate today = LocalDate.now();
        LocalDate twoDaysAgo = today.minusDays(2);
        String redisKey = "logs:" + twoDaysAgo + ".info";

        // given
        when(redisTemplate.keys("logs:*")).thenReturn(Set.of(redisKey));
        when(valueOperations.get(redisKey)).thenReturn("content");
        doThrow(new AmazonServiceException("S3 failure"))
                .when(amazonS3).putObject(anyString(), anyString(), anyString()); // 업로드 실패 시 예외 발생

        // then
        assertThrows(BusinessLogicException.class,
                () -> logPost.logPostForS3(Collections.singletonList("info"), 0, 1)
        ); // BusinessLogicException 발생 확인
        verify(logStorageService, atLeastOnce())
                .logAndStoreWithError(contains("S3 업로드 실패"), any(), any(), any()); // 오류 로그 기록 확인
    }
}
