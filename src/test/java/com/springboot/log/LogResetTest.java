package com.springboot.log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.log.service.LogStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogResetTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate; // RedisTemplate Mock
    @Mock
    private AmazonS3 amazonS3;             // S3 클라이언트 Mock
    @Mock
    private LogStorageService logStorageService; // 로그 저장 서비스 Mock

    @InjectMocks
    private LogReset logReset;             // 테스트 대상 클래스

    private final String bucket = "logbe-i-log";

    @BeforeEach
    void setUp() {
        // no setup needed for RedisTemplate since listObjects doesn't use RedisTemplate
    }

    /**
     * S3에 삭제할 객체가 없을 때의 동작 테스트
     * - amazonS3.listObjects()가 빈 ObjectListing 반환
     * - logAndStoreWithError 호출 확인
     * - deleteObjects 호출이 없어야 함
     */
    @Test
    void givenNoObjects_whenLogResetForS3_thenErrorLoggedAndReturn() {
        LocalDate fixedDate = LocalDate.of(2025, 5, 13);

        try (MockedStatic<LocalDate> mockDate = mockStatic(LocalDate.class)) {
            mockDate.when(LocalDate::now).thenReturn(fixedDate);

            // given
            ObjectListing listing = new ObjectListing();
            when(amazonS3.listObjects(bucket, "logs/")).thenReturn(listing);
            LocalDate fromDate = fixedDate.minusDays(1);
            LocalDate toDate = fixedDate.minusDays(0);

            // when
            logReset.logResetForS3(List.of("info"), 0, 1);

            // then
            verify(logStorageService).logAndStoreWithError(
                    eq("No logs found for deletion between {} and {}"),
                    eq(fromDate.toString()),      // fixedDate.minusDays(2)
                    eq(toDate),      // fixedDate.minusDays(1)
                    eq("reset")
            );
        }
    }

    /**
     * 조건에 맞는 S3 객체가 있을 때 정상 삭제 테스트
     * - 2일 전 파일 키 생성
     * - deleteObjects 요청이 호출되는지 확인
     * - 성공 로그 기록 확인
     */
    @Test
    void givenMatchingObjects_whenLogResetForS3_thenDeleteAndLogSuccess() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate twoDaysAgo = today.minusDays(2);
        String keyName = "logs/" + twoDaysAgo + ".info.log";

        // ObjectListing에 요약정보 추가
        ObjectListing listing = new ObjectListing();
        S3ObjectSummary summary = new S3ObjectSummary();
        summary.setKey(keyName);
        listing.getObjectSummaries().add(summary);
        when(amazonS3.listObjects(bucket, "logs/")).thenReturn(listing);

        // when
        logReset.logResetForS3(List.of("info"), 0, 1);

        // then
        // deleteObjects 호출 확인
        verify(amazonS3).deleteObjects(argThat(req -> {
            List<DeleteObjectsRequest.KeyVersion> keys = req.getKeys();
            return keys.size() == 1 && keys.get(0).getKey().equals(keyName);
        }));
        // 성공 로그 호출 확인
        verify(logStorageService).logAndStoreWithError(
                eq("Deleted {} log files from last 6 months"),
                eq("1"), eq("reset")
        );
    }

    /**
     * amazonS3.deleteObjects가 지속 실패할 때 예외 발생 및 오류 로그 테스트
     * - deleteObjects 호출 시 항상 예외 던지도록 설정
     * - BusinessLogicException 발생
     * - 최종 실패 로그 호출 확인
     */
    @Test
    void givenAmazonExceptionAlways_whenLogResetForS3_thenBusinessLogicException() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate twoDaysAgo = today.minusDays(2);
        String keyName = "logs/" + twoDaysAgo + ".info.log";
        ObjectListing listing = new ObjectListing();
        S3ObjectSummary summary = new S3ObjectSummary();
        summary.setKey(keyName);
        listing.getObjectSummaries().add(summary);
        when(amazonS3.listObjects(bucket, "logs/")).thenReturn(listing);
        doThrow(new AmazonServiceException("fail"))
                .when(amazonS3).deleteObjects(any(DeleteObjectsRequest.class));

        // then
        BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                () -> logReset.logResetForS3(List.of("info"), 0, 1)
        );
        assertEquals(ExceptionCode.S3_DELETE_FAILED, ex.getExceptionCode());
        // 최종 실패 로그 확인 (atLeastOnce로 재시도 로그 무관하게 검사)
        verify(logStorageService, atLeastOnce()).logAndStoreWithError(
                contains("S3 삭제 최종 실패"), any(), any(), any()
        );
    }
}
