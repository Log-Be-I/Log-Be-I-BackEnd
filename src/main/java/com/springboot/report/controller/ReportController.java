package com.springboot.report.controller;

import com.springboot.ai.googleTTS.GoogleTextToSpeechService;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.report.dto.ReportDto;
import com.springboot.report.entity.Report;
import com.springboot.report.mapper.ReportMapper;
import com.springboot.report.service.ReportService;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.utils.UriCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ReportController {
    private final static String REPORT_DEFAULT_URL = "/reports";
    private final ReportService reportService;
    private final ReportMapper mapper;
    private final MemberService memberService;
//    private final ChatGptService chatGptService;

    private final GoogleTextToSpeechService googleTextToSpeechService;
//    private final ChatGptService chatGptService;

    @PostMapping
    public ResponseEntity postReport(@RequestBody ReportDto.Post post,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        post.setMemberId(customPrincipal.getMemberId());
        Report report = reportService.createReport(mapper.reportPostToReport(post), customPrincipal.getMemberId());
        URI location = UriCreator.createUri(REPORT_DEFAULT_URL);
        return ResponseEntity.created(location).body(new SingleResponseDto<>(mapper.reportToReportResponse(report)));
    }

    @PostMapping("/tts")
// 구글 TTS (유저가 선택한 reportId 리스트를 받는다)
    public ResponseEntity<List<String>> generateTts(@RequestBody List<Long> reportsId,
                                                    @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        // 유효한 회원인지 검증
        Member member = memberService.validateExistingMember(customPrincipal.getMemberId());
        memberService.validateMemberStatus(member);

        try {
            // reportId 로 report 를 찾아서 List<Report> 생성
            List<Report> reportList = reportsId.stream()
                    .map(reportId ->
                            reportService.findReport(reportId))
                    .collect(Collectors.toList());
            // 생성된 파일 이름을 담을 리스트
            List<String> filePathList = new ArrayList<>();
            // 리포트 리스트를 돌면서 하나하나 TTS 변환기에 넣기
            reportList.stream().forEach(record ->
            {
                try {
                    // UUID 로 겹치지 않는 파일명 생성
                    String fileName = UUID.randomUUID().toString() + ".mp3";
                    // 제목과 내용을 같이 전달해서 시작하는 글의 날짜를 말하게 함
                    googleTextToSpeechService.synthesizeText(record.getTitle() + record.getContent(), fileName);
                    // 생성된 파일 경로 복사
                    filePathList.add(fileName);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return ResponseEntity.ok(filePathList);
        } catch (Exception e) {
            log.error("Google TTS 오류 발생", e);
            // 에러 터졌을때는 빈배열 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
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
