package com.springboot.report.controller;

import com.springboot.ai.googleTTS.GoogleTextToSpeechService;
import com.springboot.auth.utils.CustomPrincipal;
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
@Slf4j
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {
    private final static String REPORT_DEFAULT_URL = "/reports";
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
    }

    @PostMapping("/tts")
    public ResponseEntity<String> generateTts(@RequestParam String text) {
        try {
            googleTextToSpeechService.synthesizeText(text, "output01.mp3");
            return ResponseEntity.ok("음성 생성 완료");
        } catch (Exception e) {
            log.error("Google TTS 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("음성 생성 중 오류가 발생했습니다.");
        }
    }
}
