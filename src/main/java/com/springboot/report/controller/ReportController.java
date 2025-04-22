package com.springboot.report.controller;

import com.springboot.ai.googleTTS.GoogleTextToSpeechService;
import com.springboot.auth.utils.CustomPrincipal;

import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.report.dto.ReportDto;

import com.springboot.report.entity.Report;
import com.springboot.report.mapper.ReportMapper;
import com.springboot.report.service.ReportService;
import com.springboot.responsedto.ListResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.utils.AuthorizationUtils;
import com.springboot.utils.UriCreator;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;
    private final ReportMapper mapper;
    private final GoogleTextToSpeechService googleTextToSpeechService;



    @PostMapping
    public ResponseEntity postReport(@RequestBody ReportDto.Post post,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        post.setMemberId(customPrincipal.getMemberId());
        Report report = reportService.createReport(mapper.reportPostToReport(post), customPrincipal.getMemberId());
        URI location = UriCreator.createUri(REPORT_DEFAULT_URL);
        return ResponseEntity.created(location).body(new SingleResponseDto<>(mapper.reportToReportResponse(report)));


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
//    @PostMapping("/tts")
//    public ResponseEntity<String> generateTts(@RequestBody String text) {
//        try {
//
//            googleTextToSpeechService.synthesizeText(text, "output01.mp3");
//            return ResponseEntity.ok("음성 생성 완료");
//        } catch (Exception e) {
//            log.error("Google TTS 오류 발생", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("음성 생성 중 오류가 발생했습니다.");
//        }
//    }

//     }



//    @GetMapping
//    public ResponseEntity getReportList(@Positive @RequestParam(value = "year", required = false) Integer year,  // year를 안보내도 기본값 처리 하도록 설정
//                                         @AuthenticationPrincipal CustomPrincipal customPrincipal){
//        //year 값이 설정되지 않았다면 올해 기준으로 정렬
//        int searchYear =(year != null) ? year : LocalDate.now().getYear();
//
//        List<Report> reports = reportService.findMonthlyReports(searchYear, customPrincipal.getMemberId());
//
//        return new ResponseEntity<>(new ListResponseDto<>(mapper.monthliesToMonthlyResponses(monthlyReports)), HttpStatus.OK);
//    }
//
//
//    @GetMapping("{monthly-id}")
//    public ResponseEntity getReport(@Positive @PathVariable("monthly-id") long monthlyId,
//                                     @AuthenticationPrincipal CustomPrincipal customPrincipal){
//
//        MonthlyReport monthlyReport = monthlyReportService.findMonthlyReport(monthlyId, customPrincipal.getMemberId());
//        return new ResponseEntity<>( new SingleResponseDto<>(mapper.monthlyToMonthlyResponse(monthlyReport)), HttpStatus.OK);
//    }
}
