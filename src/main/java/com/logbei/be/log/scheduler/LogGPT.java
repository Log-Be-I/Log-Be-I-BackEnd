package com.logbei.be.log.scheduler;


import com.logbei.be.log.LogPost;
import com.logbei.be.log.LogReset;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@RequiredArgsConstructor
public class LogGPT {

    private final LogReset logReset;
    private final LogPost logPost;

    // 6개월마다 GPT 로그 삭제 스케쥴러 (1월, 7월 의 1일마다 실행)
    @Scheduled(cron = "0 0 0 1 1,7 *")
    public void scheduleMonthlyLogReset() {
        List<String> logNames = List.of( "GPT_Record", "GPT_Report"); // 삭제할 이름 리스트
        logReset.logResetForS3(logNames, 6,0); // 여기서 호출!
    }

    // 1개월마다 GPT 로그 S3 등록 스케쥴러 (각 월 1일마다 실행)
    @Scheduled(cron = "0 0 0 1 * * ")
    public void scheduleMonthlyLogPost() {
        List<String> logNames = List.of("GPT_Record", "GPT_Report");
        logPost.logPostForS3(logNames, 1, 0);
    }
}
