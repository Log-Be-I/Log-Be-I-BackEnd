package com.springboot.report.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.report.dto.ReportDto;
import com.springboot.report.entity.Report;
import com.springboot.report.mapper.ReportMapper;
import com.springboot.report.service.ReportService;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.utils.UriCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.net.URI;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {
    private final static String REPORT_DEFAULT_URL = "/reports";
    private final ReportService reportService;
    private final ReportMapper mapper;
//    private final ChatGptService chatGptService;


    @PostMapping
    public ResponseEntity postReport(@RequestBody ReportDto.Post post,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        post.setMemberId(customPrincipal.getMemberId());
        Report report = reportService.createReport(mapper.reportPostToReport(post), customPrincipal.getMemberId());
        URI location = UriCreator.createUri(REPORT_DEFAULT_URL);
        return ResponseEntity.created(location).body(new SingleResponseDto<>(mapper.reportToReportResponse(report)));
    }

//    @PostMapping("/gpt-test")
//    public ResponseEntity testGpt(@RequestBody GptRequest request) {
////        try {
////            String content = request.getContent();
////            String response = chatGptService.getGptResponse(content).block(); // 여기서 예외 터지면 catch 됨
////            System.out.println("🎯 GPT 응답: " + response);
////            return ResponseEntity.ok(response);
////        } catch (Exception e) {
////            System.out.println("❌ 예외 발생: " + e.getMessage());
////            e.printStackTrace();
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("GPT 호출 실패: " + e.getMessage());
////        }
//
////                String content = request.getContent();
////        ReportAnalysisResponse response = chatGptService.getGptResponse(content).block();
////        Mono<ReportAnalysisResponse> response = chatGptService.getGptResponse(request.getContent());
//
////        String content = request.getContent();
////        ReportAnalysisResponse response = chatGptService.getGptResponse(content).block();
////        ReportAnalysisResponse response = chatGptService.getGptResponse(content)
////                .doOnNext(result -> System.out.println("✅ GPT 결과 수신 완료"))
////                .doOnError(error -> {
////                    System.err.println("❌ GPT 호출 중 에러: " + error.getMessage());
////                    error.printStackTrace();
////                })
////                .block();
//
//        return ResponseEntity.ok(response);
////        return chatGptService.getGptResponse(content)
////
//// 분석결과를 200(OK) 로 감싸줌
////                .map(result -> ResponseEntity.ok(result))
////               //동기 -> 결과가 나올때까지 기다린다.
////                .block();
//
//    }
}
