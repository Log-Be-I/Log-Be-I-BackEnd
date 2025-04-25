package com.springboot.log;

import com.amazonaws.services.s3.AmazonS3;
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
    private final String bucketName = "tour-bucket-name";

    // 매시 정각 (0초 0분 *시 *일 *월 *요일)
    @Scheduled(cron = "0 0 * * * *")
    public void transferLogsToS3() {

        // 현재 날짜에서 -1 day => 어제
        String key = "logs:" + LocalDate.now().minusDays(1);
        List<String> logs = redisTemplate.opsForList().range(key, 0, -1);

        if (logs != null && !logs.isEmpty()) {
            String logData = String.join("\n", logs);

            // S3 업로드
            String fileName = "logs/" + key + ".log";
            amazonS3.putObject(bucketName, fileName, logData);

            // Redis 로그 삭제
            redisTemplate.delete(key);

            log.info("✅ 로그 이관 완료: {}", fileName);
        }
    }
}
