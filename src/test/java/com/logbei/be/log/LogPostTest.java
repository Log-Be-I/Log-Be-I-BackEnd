package com.logbei.be.log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.logbei.be.exception.BusinessLogicException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LogPostTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private AmazonS3 amazonS3;
    @Mock
    private LogStorageService logStorageService;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private LogPost logPost;

    private final String logName = "post";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    /**
     * Redis에 로그 키가 없을 때,
     * logAndStoreWithError("No logs found...")가 호출되는지 검증
     */
    @Test
    void givenEmptyRedisKeys_whenLogPostForS3_thenLogNoLogsFound() {
        // given
        when(redisTemplate.keys("logs:*")).thenReturn(Collections.emptySet());
        int monthRange = 0;
        int dayRange = 5;
        LocalDate today = LocalDate.now();
        LocalDate rangeDateAgo = today.minusDays(dayRange);

        // when
        logPost.logPostForS3(Collections.singletonList(logName), monthRange, dayRange);

        // then: No logs branch uses LocalDate today
        verify(logStorageService).logAndStoreWithError(
                eq("No logs found for post between {} and {}"),
                eq(rangeDateAgo.toString()),
                eq(today),
                eq(logName)
        );
        verifyNoInteractions(amazonS3);
    }

    /**
     * Redis에 키는 존재하지만 값이 null일 때,
     * S3 업로드 호출 없이 건너뛰는지 검증
     */
    @Test
    void givenRedisKeyWithNullValue_whenLogPostForS3_thenSkipUpload() {
        // given
        LocalDate fileDate = LocalDate.now().minusDays(2);
        String key = "logs:" + fileDate + ".post";
        Set<String> keys = new HashSet<>(Collections.singletonList(key));
        when(redisTemplate.keys("logs:*")).thenReturn(keys);
        when(valueOperations.get(key)).thenReturn(null);

        // when
        logPost.logPostForS3(Collections.singletonList(logName), 0, 2);

        // then: skip upload when value is null
        verify(valueOperations).get(key);
        verifyNoInteractions(amazonS3);
    }

    /**
     * Redis에서 유효한 값이 반환될 때,
     * S3에 업로드 성공 후 Redis delete 및 성공 로그 호출을 검증
     */
    @Test
    void givenValidRedisKey_whenUploadSuccess_thenDeleteKeyAndLogSuccess() {
        // given
        LocalDate fileDate = LocalDate.now().minusDays(2);
        String key = "logs:" + fileDate + ".post";
        String value = "log content";
        when(redisTemplate.keys("logs:*")).thenReturn(Collections.singleton(key));
        when(valueOperations.get(key)).thenReturn(value);
        when(amazonS3.putObject(anyString(), anyString(), anyString()))
                .thenReturn(new PutObjectResult());

        // when
        logPost.logPostForS3(Collections.singletonList(logName), 0, 2);

        // then: upload invoked and key deleted
        verify(amazonS3).putObject(
                eq("logbe-i-log"),
                eq(key.replace("logs:", "logs/") + ".log"),
                eq(value)
        );
        verify(redisTemplate).delete(key);

        // then: success log uses String today
        LocalDate today = LocalDate.now();
        LocalDate rangeDateAgo = today.minusDays(2);
        verify(logStorageService).logAndStoreWithError(
                eq("Posted {} log files to S3 between {} and {}"),
                eq("1"),
                eq(rangeDateAgo.toString()),
                eq(today.toString()),
                eq(logName)
        );
    }

    /**
     * S3 업로드 도중 예외가 발생하면,
     * 재시도 후 성공하는지 (총 3회 호출, 두 번 재시도 로그) 검증
     */
    @Test
    void givenAmazonServiceException_thenRetryAndSucceed() {
        // given
        LocalDate fileDate = LocalDate.now().minusDays(2);
        String key = "logs:" + fileDate + ".post";
        when(redisTemplate.keys("logs:*")).thenReturn(Collections.singleton(key));
        when(valueOperations.get(key)).thenReturn("log data");
        AmazonServiceException e1 = new AmazonServiceException("err1");
        AmazonServiceException e2 = new AmazonServiceException("err2");
        when(amazonS3.putObject(anyString(), anyString(), anyString()))
                .thenThrow(e1)
                .thenThrow(e2)
                .thenReturn(new PutObjectResult());

        // when
        logPost.logPostForS3(Collections.singletonList(logName), 0, 2);

        // then: retried thrice
        verify(amazonS3, times(3)).putObject(anyString(), anyString(), anyString());
        // retry logs
        verify(logStorageService, atLeast(2)).logAndStoreWithError(
                startsWith("S3 업로드 실패 - 재시도"),
                any(), any(), any()
        );
        // final success log
        LocalDate today = LocalDate.now();
        LocalDate rangeDateAgo = today.minusDays(2);
        verify(logStorageService).logAndStoreWithError(
                eq("Posted {} log files to S3 between {} and {}"),
                eq("1"),
                eq(rangeDateAgo.toString()),
                eq(today.toString()),
                eq(logName)
        );
    }

    /**
     * S3 업로드 실패가 3회 연속 발생하면,
     * BusinessLogicException 발생 및 최종 실패 로그 호출을 검증
     */
    @Test
    void givenPersistentAmazonServiceException_thenThrowBusinessLogicException() {
        // given
        LocalDate fileDate = LocalDate.now().minusDays(2);
        String key = "logs:" + fileDate + ".post";
        when(redisTemplate.keys("logs:*")).thenReturn(Collections.singleton(key));
        when(valueOperations.get(key)).thenReturn("log data");
        AmazonServiceException fatal = new AmazonServiceException("fatal error");
        when(amazonS3.putObject(anyString(), anyString(), anyString()))
                .thenThrow(fatal, fatal, fatal);

        // when & then: should throw after 3 retries
        assertThrows(BusinessLogicException.class,
                () -> logPost.logPostForS3(Collections.singletonList(logName), 0, 2));
        verify(amazonS3, times(3)).putObject(anyString(), anyString(), anyString());
        // final failure log
        verify(logStorageService).logAndStoreWithError(
                eq("S3 업로드 최종 실패"),
                eq(logName),
                anyString(),
                any(AmazonServiceException.class)
        );
    }
}
