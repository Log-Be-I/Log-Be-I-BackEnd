package com.springboot.notice.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeAttachmentCleanup {

    private final AmazonS3 amazonS3;
    private final String bucketName = "logbe-i-images";

    // 매월 1일 0시에 실행됨 (cron: 0 0 0 1 * ?)
    // deletedImages/ 디렉토리 안의 파일들 중 90일이 지난 파일을 완전히 삭제하는 스케줄러
    @Scheduled(cron = "0 0 0 1 * ?")
    public void deleteExpiredNoticeAttachments() {
        String prefix = "deletedImages/";
        LocalDate thresholdDate = LocalDate.now().minusDays(90);

        // deletedImages/ 경로의 모든 S3 객체 조회
        List<S3ObjectSummary> allObjects = amazonS3.listObjects(bucketName, prefix)
                .getObjectSummaries();

        // 파일의 lastModified 날짜를 기준으로 90일이 지난 파일만 필터링
        List<String> keysToDelete = allObjects.stream()
                .filter(obj -> {
                    Date lastModified = obj.getLastModified();
                    LocalDate fileDate = lastModified.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return fileDate.isBefore(thresholdDate);
                })
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());

        if (keysToDelete.isEmpty()) {
            log.info("[NOTICE-CLEANUP] 삭제할 첨부파일이 없습니다 (deletedImages/, 90일 기준)");
            return;
        }

        // 삭제할 key 목록으로 S3 객체 삭제 요청 생성
        //DeleteObjectsRequest : S3 에서 여러 파일을 한꺼번에 삭제할 수 있게 함
        DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName)
                .withKeys(keysToDelete.toArray(new String[0]));

        int retryCount = 0; //실패 시, 재시도 로직
        while (true) {
            try {
                //S3에서 지정된 객체들 한번에 삭제
                amazonS3.deleteObjects(deleteRequest);
                log.info("[NOTICE-CLEANUP] deletedImages/에서 {}개의 첨부파일을 완전히 삭제했습니다.", keysToDelete.size());
                break;
            } catch (AmazonServiceException e) {
                //삭제 실패 시 재시도 카운트 증가
                retryCount++;
                log.error("[NOTICE-CLEANUP] S3 첨부파일 삭제 실패 (재시도 {}회)", retryCount);
                //3회 이상 실패시 예외 발생 후 종료
                if (retryCount >= 3) {
                    log.error("[NOTICE-CLEANUP] S3 첨부파일 삭제 최종 실패: {}", e.getMessage());
                    throw new BusinessLogicException(ExceptionCode.S3_DELETE_FAILED);
                }
                try {
                    //잠시 대기 후 재시도 (2초)
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    //인터럽트 상태 복구
                    Thread.currentThread().interrupt();
                    throw new BusinessLogicException(ExceptionCode.S3_DELETE_FAILED);
                }
            }
        }
    }
}
