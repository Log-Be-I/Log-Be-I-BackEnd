package com.springboot.log.scheduler;

import com.springboot.log.LogPost;
import com.springboot.log.LogReset;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@RequiredArgsConstructor
public class LogGoogle {
    private final LogReset logReset;
    private final LogPost logPost;

    // 6개월마다 S3 초기화
//    @Scheduled(cron = "0 0 0 1 1,7 *")
//    public void googleCalendarMonthlyLogReset() {
//        List<String> logNames = List.of( "Google_Calendar"); // 삭제할 이름 리스트
//        logReset.logResetForS3(logNames, 6,0); // 여기서 호출!
//    }

    // 1달마다 S3 갱신
//    @Scheduled(cron = "0 0 0 1 * * ")
//    public void googleCalendarMonthlyLogPost() {
//        List<String> logNames = List.of("Google_Calendar");
//        logPost.logPostForS3(logNames, 1, 0);
//    }

    // 1달마다 S3 갱신
    @Scheduled(cron = "0 0 0 1 * * ")
    public void googleTTSMonthlyLogPost () {
        List<String> logNames = List.of("Google_TTS");
        logPost.logPostForS3(logNames, 1, 0);
    }

    // 6개월마다 S3 초기화
    @Scheduled(cron = "0 0 0 1 1,7 *")
   public void googleTTSMonthlyLogReset() {
        List<String> logNames = List.of( "Google_TTS"); // 삭제할 이름 리스트
        logReset.logResetForS3(logNames, 6,0); // 여기서 호출!
    }
}
