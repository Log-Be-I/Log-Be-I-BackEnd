package com.springboot.log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.log.service.LogStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogPost {

    private final RedisTemplate<String, String> redisTemplate;
    private final AmazonS3 amazonS3;
    private final String bucketName = "logbe-i-log";
    private final LogStorageService logStorageService;
    private boolean isHandlingError = false;

    public void logPostForS3(List<String> logNames, int monthRange, int dayRange) {
        String logName = "post";
        int retryCount = 0;
        // 현재 날짜에서 데이터가 존재하는 month or day 만큼 이전 데이터 파싱
        LocalDate rangeDateAgo = monthRange != 0
                ? LocalDate.now().minusMonths(monthRange)
                : LocalDate.now().minusDays(dayRange);
        // 오늘 날짜
        LocalDate today = LocalDate.now();

        // 1. 패턴으로 Redis에서 키 조회
        Set<String> keys = redisTemplate.keys("logs:*");

        // 조건에 맞는 키만 필터링
        List<String> postToKey = keys.stream()
                .filter(key -> {
                    try {
                        String[] parts = key.replace("logs:", "").split("\\.");
                        if (parts.length < 2) return false;

                        String datePart = parts[0]; // 2025-04-01
                        String namePart = parts[1]; // news, error, gpt 등
                        LocalDate fileDate = LocalDate.parse(datePart);

                        // 각 월 1일마다 실행으로 1일전 데이터 && 이름이 리스트에 포함된 경우만 true
                        return fileDate.isBefore(LocalDate.now().minusDays(1)) && logNames.contains(namePart);
                    } catch (Exception e) {
                        return false; // 날짜 파싱 실패한 파일은 무시
                    }
                })
                .collect(Collectors.toList());
        // 만약 등록할 redis 데이터 리스트가 비어있다면
        if (postToKey.isEmpty()) {
            // 해당 기간 로그 데이터를 찾을 수 없다는 로그 남기기
            logStorageService.logAndStoreWithError("No logs found for post between {} and {}", rangeDateAgo.toString(), today, logName);
            return;
        }
        try {
            // S3에 Redis 로그 데이터 하나씩 올리기
            for (String key : postToKey) {
                String value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    while (true) { // 재시도 가능하게 루프
                        try {
                            amazonS3.putObject(bucketName, key.replace("logs:", "logs/") + ".log", value);
                            // S3 업로드 완료하면 해당 로그 데이터 redis 에서 삭제
                            redisTemplate.delete(key);
                            break; // 성공하면 루프 탈출

                        } catch (AmazonServiceException e) {
                            retryCount++;
                            logStorageService.logAndStoreWithError("S3 업로드 실패 - 재시도 {}회 (최대 {}회)", logName, String.valueOf(retryCount), String.valueOf(3));
                            if (retryCount >= 3) {
                                logStorageService.logAndStoreWithError("S3 업로드 최종 실패", logName, e.getMessage(), e);
                                throw new BusinessLogicException(ExceptionCode.S3_POST_FAILED);
                            }
                            try {
                                Thread.sleep(2000); // 2초 대기
                            } catch (InterruptedException interruptedException) {
                                Thread.currentThread().interrupt();
                                throw new BusinessLogicException(ExceptionCode.S3_POST_FAILED);
                            }
                        }
                    }
                }
            }
            // 성공시 로그 남기기
            logStorageService.logAndStoreWithError(
                    "Posted {} log files to S3 between {} and {}",
                    String.valueOf(postToKey.size()),
                    rangeDateAgo.toString(),
                    today.toString(),
                    logName);

        } catch (AmazonServiceException e) {
            // 실패시 로그 남기기
            logStorageService.logAndStoreWithError("Failed to post log files to S3: {}", e.getMessage(), logName);
        }
    }
}
