package com.springboot.log;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@RequiredArgsConstructor
public class LogGPT {

    private final LogReset logReset;

    // 6개월마다 GPT 로그 삭제 스케쥴러
    @Scheduled(cron = "0 0 0 1 * *") // 매달 1일 0시 0분 0초
    public void scheduleMonthlyLogReset() {
        List<String> logNames = List.of( "GPT"); // 삭제할 이름 리스트
        logReset.logResetForS3(logNames); // 여기서 호출!
    }
}
