//package com.logbei.be.log.controller;
//
//import com.logbei.be.log.LogReset;
//import com.logbeo.be.log.TestLogForceUploader;
//import com.logbei.be.service.log.LogStorageService;
//import io.swagger.v3.oas.annotations.Hidden;
//import lombok.RequiredArgsConstructor;
//
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@Slf4j
//@Hidden
//@RestController
//@RequestMapping("/log")
//@RequiredArgsConstructor
//public class TestLogForceController {
//
//    private final LogStorageService logStorageService;
//    private final TestLogForceUploader testLogForceUploader;
//    private final LogReset logReset;
//
//    @PostMapping
//    public String addDummyLogs() {
//        logStorageService.storeInfoLog("더미 로그 - Google", "Google_Calendar");
//        logStorageService.storeInfoLog("더미 로그 - GPT Record", "GPT_Record");
//        logStorageService.storeInfoLog("더미 로그 - GPT Report", "GPT_Report");
//        logStorageService.storeInfoLog("더미 로그 - Clova", "Clova");
//
////        log.info("요청 URI: {}", request.getRequestURI());
//        return "OK";
////        return "더미 데이터 삽입 완료!";
//    }
//
//    // 강제 업로드 실행 API
//    @PostMapping("/force-upload")
//    public String forceUploadLogs() {
//        testLogForceUploader.forceUploadToS3();
//        return "강제 업로드 완료!";
//    }
//
//    // 🔥 S3 로그 삭제 강제 실행 API
//    @PostMapping("/force-delete")
//    public String forceDeleteLogs() {
//        List<String> logNames = List.of(
//                "Google_Calendar",
//                "GPT_Record",
//                "GPT_Report",
//                "Clova"
//        ); // 삭제할 이름 리스트
//
//        logReset.logResetForS3(logNames, 6, 0); // 6개월치 기준으로 삭제
//        return "S3 로그 삭제 완료!";
//    }
//}
