package com.springboot.log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.log.service.LogStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogReset {

    private final RedisTemplate<String, String> redisTemplate;
    private final AmazonS3 amazonS3;
    private final String bucketName = "logbe-i-log";
    private final LogStorageService logStorageService;

    // 6개월 단위로 이벤트 발생(0초 0분 0시 *일 *월 *요일)
//    @Scheduled(cron = "0 0 0 1 1,7 *")
    public void logResetForS3(List<String> logNames, int monthRange, int dayRange) {
        String logName = "reset";

        // 현재 날짜에서 데이터가 존재하는 month or day 만큼 이전 데이터 파싱
        LocalDate rangeDateAgo = monthRange != 0
                ? LocalDate.now().minusMonths(monthRange)
                : LocalDate.now().minusDays(dayRange);
        // 오늘 날짜
        LocalDate today = LocalDate.now();
            // S3 6개월치 데이터 삭제
        // S3 안에 logs/ 밑에 있는 모든 파일 리스트 불러오기
        List<S3ObjectSummary> allObjects = amazonS3.listObjects(bucketName, "logs/")
                .getObjectSummaries();

        // 모든 로그 파일에서 key 값(날짜) 이 특정 날짜 범위에 맞다면 리스트에 담는다
        List<String> keysToDelete = allObjects.stream()
                .filter(obj -> {
                    // 오브젝트 key 값(날짜) 예: logs/2024-10-27.log
                    String key = obj.getKey().replace("logs/", "").replace(".log", "");
                    // 날짜 부분만 파싱하여 datePart 문자열로 저장
                    String[] parts = key.split("\\.");
                    if (parts.length < 2) return false;

                    String datePart = parts[0]; // 2025-04-01
                    String namePart = parts[1]; // news, error, gpt 등
                    try {
                        LocalDate fileDate = LocalDate.parse(datePart);

                        // 각 월 1일마다 실행으로 1일전 데이터 && 이름이 리스트에 포함된 경우만 true
                        return fileDate.isBefore(LocalDate.now().minusDays(1)) && logNames.contains(namePart);
                    } catch (Exception e) {
                        return false; // 날짜 파싱 실패한 파일은 무시
                    }
                })
                // S3에 있는 파일 하나에 대한 요약정보를 담은 객체를 key 값으로 설정하여 순회
                .map(S3ObjectSummary::getKey)
                // 최종 데이터 리스트에 저장
                .collect(Collectors.toList());

        // 만약 삭제할 데이터 리스트가 비어있다면
        if (keysToDelete.isEmpty()) {
            // 해당 기간 로그 데이터를 찾을 수 없다는 로그 남기기
            logStorageService.logAndStoreWithError("No logs found for deletion between {} and {}", rangeDateAgo.toString(), today, logName);
            return;
        }

        // 한 번에 여러 객체 삭제
        DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName)
                .withKeys(keysToDelete.toArray(new String[0]));

        int retryCount = 0;
        while (true) {
            try {
                amazonS3.deleteObjects(deleteRequest);
                logStorageService.logAndStoreWithError("Deleted {} log files from last 6 months",
                        String.valueOf(keysToDelete.size()), logName);
                break;
            } catch (AmazonServiceException e) {
                retryCount++;
                logStorageService.logAndStoreWithError("S3 삭제 실패 - 재시도 {}회 (최대 {}회)", logName, String.valueOf(retryCount), String.valueOf(3));
                if (retryCount >= 3) {
                    logStorageService.logAndStoreWithError("S3 삭제 최종 실패", logName, e.getMessage(), e);
                    throw new BusinessLogicException(ExceptionCode.S3_DELETE_FAILED);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new BusinessLogicException(ExceptionCode.S3_DELETE_FAILED);
                }
            }
        }
    }
}
