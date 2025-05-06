package com.springboot.log.scheduler;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.springboot.log.service.LogStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogTransferScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final AmazonS3 amazonS3;
    private final String bucketName = "logbe-i-log";
    private final LogStorageService logStorageService;

    // 하루 1회(24시간) 이벤트 발생(0초 0분 0시 *일 *월 *요일)
//    @Scheduled(cron = "0 0 * * * *")
    @Scheduled(cron = "0 0 0 * * *")
    public void transferLogsToS3() {

        String logName = "S3_upload";

        // 현재 날짜에서 -1 day => 어제
        String key = "logs:" + LocalDate.now().minusDays(1);
//        String key = "logs:" + LocalDate.now();
        // redis 에서 List 형태로 첫 기록부터 마지막 기록까지 다 가져오겠다는 의미
        List<String> logs = redisTemplate.opsForList().range(key, 0, -1);

        // 뽑아온 log 목록이 null 아니거나 비어있지 않다면
        if (logs != null && !logs.isEmpty()) {
            // log 리스트를 하나의 문자열로 합치는 코드
            String logData = String.join("\n", logs);

            // S3 업로드
            // 로그 파일의 경로와 이름을 지정하는 코드
            String fileName = "logs/" + key + ".log";
            try {
                // S3에 실제 로그 데이터를 업로드하는 코드 (버킷 이름, 파일 경로, 저장하려는 로그 문자열)
                amazonS3.putObject(bucketName, fileName, logData);
            } catch (AmazonServiceException e) {
                logStorageService.logAndStore("S3 upload failed" , logName);
                return;
            }

            // Redis 로그 삭제
            // key 값으로 들어오느 값과 같은 키를 가진 데이터 전부 삭제
            redisTemplate.delete(key);

            // 파일 경로와 함께 이관 로그 찍기
            logStorageService.logAndStoreWithError("Log transfer completed: {}", fileName, logName);
        }
    }

    // 하루 1회(24시간) 이벤트 발생(0초 0분 0시 *일 *월 *요일)
    @Scheduled(cron = "0 0 0 1 1 *")
    public void transferGPTLogsToS3() {

        String logName = "GPT";

        // 현재 날짜에서 -1 day => 어제
        String key = "logs:" + LocalDate.now().minusMonths(1);
//        String key = "logs:" + LocalDate.now();
        // redis 에서 List 형태로 첫 기록부터 마지막 기록까지 다 가져오겠다는 의미
        List<String> logs = redisTemplate.opsForList().range(key, 0, -1);

        // 뽑아온 log 목록이 null 아니거나 비어있지 않다면
        if (logs != null && !logs.isEmpty()) {
            // log 리스트를 하나의 문자열로 합치는 코드
            String logData = String.join("\n", logs);

            // S3 업로드
            // 로그 파일의 경로와 이름을 지정하는 코드
            String fileName = "logs/" + key + ".log";
            try {
                // S3에 실제 로그 데이터를 업로드하는 코드 (버킷 이름, 파일 경로, 저장하려는 로그 문자열)
                amazonS3.putObject(bucketName, fileName, logData);
            } catch (AmazonServiceException e) {
                logStorageService.logAndStore("S3 upload failed" , logName);
                return;
            }

            // Redis 로그 삭제
            // key 값으로 들어오느 값과 같은 키를 가진 데이터 전부 삭제
            redisTemplate.delete(key);

            // 파일 경로와 함께 이관 로그 찍기
            logStorageService.logAndStoreWithError("Log transfer completed: {}", fileName, logName);
        }
    }

}
