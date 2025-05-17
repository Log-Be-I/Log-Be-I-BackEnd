
package com.logbei.be.log;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@RequiredArgsConstructor
public class LogClova {
    private final LogReset logReset;
    private final LogPost logPost;

    // 6개월마다 S3 초기화
    @Scheduled(cron = "0 0 0 1 1,7 *")
    public void googleCalendarMonthlyLogReset() {
        List<String> logNames = List.of( "Clova"); // 삭제할 이름 리스트
        logReset.logResetForS3(logNames, 6,0); // 여기서 호출!
    }

    // 1달마다 S3 갱신
    @Scheduled(cron = "0 0 0 1 * * ")
    public void googleCalendarMonthlyLogPost() {
        List<String> logNames = List.of("Clova");
        logPost.logPostForS3(logNames, 1, 0);
    }
}
