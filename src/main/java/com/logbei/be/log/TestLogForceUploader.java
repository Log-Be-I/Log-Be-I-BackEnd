<<<<<<< HEAD:src/main/java/com/springboot/log/TestLogForceUploader.java
<<<<<<< HEAD
//package com.springboot.log;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@RequiredArgsConstructor
//@Component
//public class TestLogForceUploader {
//
//    private final LogPost logPost;
//
//    // 강제 업로드 메서드
//    public void forceUploadToS3() {
//        List<String> logNames = List.of(
//                "Clova",
//                "Google_Calendar",
//                "Google_TTS",
//                "GPT_Record",
//                "GPT_Report"
//        ); // 원하는 로그 종류 다 나열
//
//        // 어제까지 생성된 Redis 로그를 S3로 강제 업로드
//        logPost.logPostForS3(logNames, 0, 1); // (monthRange=0, dayRange=1)
//    }
//}
=======
package com.springboot.log;
=======
package com.logbei.be.log;
>>>>>>> 3cfffea (패키지명 변경):src/main/java/com/logbei/be/log/TestLogForceUploader.java

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class TestLogForceUploader {

    private final LogPost logPost;

    // 강제 업로드 메서드
    public void forceUploadToS3() {
        List<String> logNames = List.of(
                "Clova",
                "Google_TTS",
                "GPT_Record",
                "GPT_Report"
        ); // 원하는 로그 종류 다 나열

        // 어제까지 생성된 Redis 로그를 S3로 강제 업로드
        logPost.logPostForS3(logNames, 0, 1); // (monthRange=0, dayRange=1)
    }
}
>>>>>>> 4b61f95 (log 지우기)
