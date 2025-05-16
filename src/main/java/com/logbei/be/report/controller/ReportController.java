package com.logbei.be.report.controller;

<<<<<<< HEAD:src/main/java/com/springboot/report/controller/ReportController.java
import com.springboot.ai.openai.service.OpenAiService;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.record.entity.Record;
import com.springboot.record.service.RecordService;
import com.springboot.report.dto.ReportAnalysisRequest;
import com.springboot.report.dto.ReportResponseDto;
import com.springboot.report.dto.SummaryResponseDto;
import com.springboot.report.entity.Report;
import com.springboot.report.mapper.ReportMapper;
import com.springboot.report.service.ReportService;
import com.springboot.responsedto.ListResponseDto;
import com.springboot.schedule.dto.ScheduleResponseDto;
import com.springboot.swagger.SwaggerErrorResponse;
import com.springboot.utils.ReportUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
=======
import com.logbei.be.ai.openai.service.OpenAiService;
import com.logbei.be.auth.utils.CustomPrincipal;
import com.logbei.be.record.entity.Record;
import com.logbei.be.record.service.RecordService;
import com.logbei.be.report.dto.ReportAnalysisRequest;
import com.logbei.be.report.entity.Report;
import com.logbei.be.report.mapper.ReportMapper;
import com.logbei.be.report.service.ReportService;
import com.logbei.be.responsedto.ListResponseDto;
import com.logbei.be.utils.ReportUtil;
>>>>>>> 3cfffea (패키지명 변경):src/main/java/com/logbei/be/report/controller/ReportController.java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reports")
@Validated
@RequiredArgsConstructor
@Tag(name = "분석 API", description = "분석 등록, 조회 관련 API")
public class ReportController {
    private final ReportService reportService;
    private final ReportMapper mapper;

    //postman Test 진행
    private final OpenAiService openAiService;
    private final RecordService recordService;

    //test
//    (@RequestParam("weekStart") LocalDateTime weekStart,
//    @RequestParam("weekEnd") LocalDateTime weekEnd
//    @PostMapping("/test")
//    public ResponseEntity testGenerateReports() {
//
//        ZoneId koreaZone = ZoneId.of("Asia/Seoul");
//        LocalDateTime today = LocalDateTime.now(koreaZone);
//
////        LocalDateTime today = LocalDateTime.now();
//        //전 주 월요일(4/7) 00:00:00
//        LocalDateTime weekStart = today.minusWeeks(4).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
////        //전 주 일요일(4/13) 23:59:59
//        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
//
//        List<Record> weeklyRecords = recordService.   getWeeklyRecords(weekStart, weekEnd);
//
//        List<ReportAnalysisRequest> weeklies = ReportUtil.toReportRequests(weeklyRecords, Report.ReportType.REPORT_WEEKLY);
//        // GPT 분석 → Report 생성 -> DB 저장
////        List<Report> reports = openAiService.createReportsFromAi(weeklies);
//        List<Report> reports = openAiService.createReportsFromAiInBatch(weeklies);
//
//        return new ResponseEntity<>(new ListResponseDto<>(
//                mapper.reportsToReportsResponseDtos(reports)), HttpStatus.CREATED);
//    }
//
//    @PostMapping("/test1")
//    public ResponseEntity testGenerateReportss() {
//
//        ZoneId koreaZone = ZoneId.of("Asia/Seoul");
//        LocalDateTime today = LocalDateTime.now(koreaZone);
//
////        LocalDateTime today = LocalDateTime.now();
//        //전 주 월요일(4/7) 00:00:00
//        LocalDateTime weekStart = today.minusWeeks(3).with(DayOfWeek.MONDAY).minusDays(1).toLocalDate().atStartOfDay();//        //전 주 일요일(4/13) 23:59:59
//        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
//
//        List<Record> weeklyRecords = recordService.getWeeklyRecords(weekStart, weekEnd);
//
//        List<ReportAnalysisRequest> weeklies = ReportUtil.toReportRequests(weeklyRecords, Report.ReportType.REPORT_WEEKLY);
//        // GPT 분석 → Report 생성 -> DB 저장
////        List<Report> reports = openAiService.createReportsFromAi(weeklies);
//        List<Report> reports = openAiService.createReportsFromAiInBatch(weeklies);
//
//        return new ResponseEntity<>(new ListResponseDto<>(
//                mapper.reportsToReportsResponseDtos(reports)), HttpStatus.CREATED);
//    }
//
//    @PostMapping("/test2")
//    public ResponseEntity testGenerateReportsss() {
//
//        ZoneId koreaZone = ZoneId.of("Asia/Seoul");
//        LocalDateTime today = LocalDateTime.now(koreaZone);
//
////        LocalDateTime today = LocalDateTime.now();
//        //전 주 월요일(4/7) 00:00:00
//        LocalDateTime weekStart = today.minusWeeks(2).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
////        //전 주 일요일(4/13) 23:59:59
//        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
//
//        List<Record> weeklyRecords = recordService.getWeeklyRecords(weekStart, weekEnd);
//
//        List<ReportAnalysisRequest> weeklies = ReportUtil.toReportRequests(weeklyRecords, Report.ReportType.REPORT_WEEKLY);
//        // GPT 분석 → Report 생성 -> DB 저장
////        List<Report> reports = openAiService.createReportsFromAi(weeklies);
//        List<Report> reports = openAiService.createReportsFromAiInBatch(weeklies);
//
//        return new ResponseEntity<>(new ListResponseDto<>(
//                mapper.reportsToReportsResponseDtos(reports)), HttpStatus.CREATED);
//    }
//
//    @PostMapping("/test3")
//    public ResponseEntity testGenerateReportssss() {
//
//        ZoneId koreaZone = ZoneId.of("Asia/Seoul");
//        LocalDateTime today = LocalDateTime.now(koreaZone);
//
////        LocalDateTime today = LocalDateTime.now();
//        //전 주 월요일(4/7) 00:00:00
//        LocalDateTime weekStart = today.minusWeeks(1).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
////        //전 주 일요일(4/13) 23:59:59
//        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
//
//        List<Record> weeklyRecords = recordService.getWeeklyRecords(weekStart, weekEnd);
//
//        List<ReportAnalysisRequest> weeklies = ReportUtil.toReportRequests(weeklyRecords, Report.ReportType.REPORT_WEEKLY);
//        // GPT 분석 → Report 생성 -> DB 저장
////        List<Report> reports = openAiService.createReportsFromAi(weeklies);
//        List<Report> reports = openAiService.createReportsFromAiInBatch(weeklies);
//
//        return new ResponseEntity<>(new ListResponseDto<>(
//                mapper.reportsToReportsResponseDtos(reports)), HttpStatus.CREATED);
//    }
//    @PostMapping("/test4")
//    public ResponseEntity testGenerateReportssssw() {
//
//        ZoneId koreaZone = ZoneId.of("Asia/Seoul");
//        LocalDateTime today = LocalDateTime.now(koreaZone);
//
////        LocalDateTime today = LocalDateTime.now();
//        //전 주 월요일(4/7) 00:00:00
//        LocalDateTime weekStart = today.minusWeeks(8).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
////        //전 주 일요일(4/13) 23:59:59
//        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
//
//        List<Record> weeklyRecords = recordService.getWeeklyRecords(weekStart, weekEnd);
//
//        List<ReportAnalysisRequest> weeklies = ReportUtil.toReportRequests(weeklyRecords, Report.ReportType.REPORT_WEEKLY);
//        // GPT 분석 → Report 생성 -> DB 저장
////        List<Report> reports = openAiService.createReportsFromAi(weeklies);
//        List<Report> reports = openAiService.createReportsFromAiInBatch(weeklies);
//
//        return new ResponseEntity<>(new ListResponseDto<>(
//                mapper.reportsToReportsResponseDtos(reports)), HttpStatus.CREATED);
//    }
//    @PostMapping("/test5")
//    public ResponseEntity testGenerateReportsssd() {
//
//        ZoneId koreaZone = ZoneId.of("Asia/Seoul");
//        LocalDateTime today = LocalDateTime.now(koreaZone);
//
////        LocalDateTime today = LocalDateTime.now();
//        //전 주 월요일(4/7) 00:00:00
//        LocalDateTime weekStart = today.minusWeeks(7).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
////        //전 주 일요일(4/13) 23:59:59
//        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
//
//        List<Record> weeklyRecords = recordService.getWeeklyRecords(weekStart, weekEnd);
//
//        List<ReportAnalysisRequest> weeklies = ReportUtil.toReportRequests(weeklyRecords, Report.ReportType.REPORT_WEEKLY);
//        // GPT 분석 → Report 생성 -> DB 저장
////        List<Report> reports = openAiService.createReportsFromAi(weeklies);
//        List<Report> reports = openAiService.createReportsFromAiInBatch(weeklies);
//
//        return new ResponseEntity<>(new ListResponseDto<>(
//                mapper.reportsToReportsResponseDtos(reports)), HttpStatus.CREATED);
//    }
//    @PostMapping("/test6")
//    public ResponseEntity testGenerateReportsssddd() {
//
//        ZoneId koreaZone = ZoneId.of("Asia/Seoul");
//        LocalDateTime today = LocalDateTime.now(koreaZone);
//
////        LocalDateTime today = LocalDateTime.now();
//        //전 주 월요일(4/7) 00:00:00
//        LocalDateTime weekStart = today.minusWeeks(6).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
////        //전 주 일요일(4/13) 23:59:59
//        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
//
//        List<Record> weeklyRecords = recordService.getWeeklyRecords(weekStart, weekEnd);
//
//        List<ReportAnalysisRequest> weeklies = ReportUtil.toReportRequests(weeklyRecords, Report.ReportType.REPORT_WEEKLY);
//        // GPT 분석 → Report 생성 -> DB 저장
////        List<Report> reports = openAiService.createReportsFromAi(weeklies);
//        List<Report> reports = openAiService.createReportsFromAiInBatch(weeklies);
//
//        return new ResponseEntity<>(new ListResponseDto<>(
//                mapper.reportsToReportsResponseDtos(reports)), HttpStatus.CREATED);
//    }
//    @PostMapping("/test11")
//public ResponseEntity testGenerateReportsssdddd() {
//
//        ZoneId koreaZone = ZoneId.of("Asia/Seoul");
//        LocalDateTime today = LocalDateTime.now(koreaZone);
//
////        LocalDateTime today = LocalDateTime.now();
//        //전 주 월요일(4/7) 00:00:00
//        LocalDateTime weekStart = today.minusWeeks(6).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
////        //전 주 일요일(4/13) 23:59:59
//        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
//
//        List<Record> weeklyRecords = recordService.getWeeklyRecords(weekStart, weekEnd);
//
//        List<ReportAnalysisRequest> weeklies = ReportUtil.toReportRequests(weeklyRecords, Report.ReportType.REPORT_WEEKLY);
//        // GPT 분석 → Report 생성 -> DB 저장
////        List<Report> reports = openAiService.createReportsFromAi(weeklies);
//        List<Report> reports = openAiService.createReportsFromAiInBatch(weeklies);
//
//        return new ResponseEntity<>(new ListResponseDto<>(
//                mapper.reportsToReportsResponseDtos(reports)), HttpStatus.CREATED);
//    }
//    @PostMapping("/test12")
//    public ResponseEntity testGenerateReportsssdasfsdddd() {
//
//        ZoneId koreaZone = ZoneId.of("Asia/Seoul");
//        LocalDateTime today = LocalDateTime.now(koreaZone);
//
////        LocalDateTime today = LocalDateTime.now();
//        //전 주 월요일(4/7) 00:00:00
//        LocalDateTime weekStart = today.minusWeeks(5).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
////        //전 주 일요일(4/13) 23:59:59
//        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
//
//        List<Record> weeklyRecords = recordService.getWeeklyRecords(weekStart, weekEnd);
//
//        List<ReportAnalysisRequest> weeklies = ReportUtil.toReportRequests(weeklyRecords, Report.ReportType.REPORT_WEEKLY);
//        // GPT 분석 → Report 생성 -> DB 저장
////        List<Report> reports = openAiService.createReportsFromAi(weeklies);
//        List<Report> reports = openAiService.createReportsFromAiInBatch(weeklies);
//
//        return new ResponseEntity<>(new ListResponseDto<>(
//                mapper.reportsToReportsResponseDtos(reports)), HttpStatus.CREATED);
//    }
//
//    @PostMapping("/test7")
//    public ResponseEntity testMonthlyReport(){
//
//        YearMonth lastMonth = YearMonth.now().minusMonths(1);
//        //전 달 1일 00:00:00
//        LocalDateTime monthStart = lastMonth.atDay(1).atStartOfDay();
//        //전 달 말일 23:59:59
//        LocalDateTime monthEnd = lastMonth.atEndOfMonth().atTime(23, 59, 59);
//
//
//        List<Record> monthlyRecords = recordService.getMonthlyRecords(monthStart, monthEnd);
//        List<ReportAnalysisRequest> monthlies = ReportUtil.toReportRequests(monthlyRecords, Report.ReportType.REPORT_MONTHLY);
//        log.info("✅ 월간 리포트 생성 시작");
//        //ai에 해당 데이터 전달
//        List<Report> reports = openAiService.createReportsFromAiInBatch(monthlies);
//
//        return new ResponseEntity<>(new ListResponseDto<>(
//                mapper.reportsToReportsResponseDtos(reports)), HttpStatus.CREATED);
//    }
//
//
//    @PostMapping("/test8")
//    public ResponseEntity testMonthlyReports(){
//
//        YearMonth lastMonth = YearMonth.now().minusMonths(3);
//        //전 달 1일 00:00:00
//        LocalDateTime monthStart = lastMonth.atDay(1).atStartOfDay();
//        //전 달 말일 23:59:59
//        LocalDateTime monthEnd = lastMonth.atEndOfMonth().atTime(23, 59, 59);
//
//
//        List<Record> monthlyRecords = recordService.getMonthlyRecords(monthStart, monthEnd);
//        List<ReportAnalysisRequest> monthlies = ReportUtil.toReportRequests(monthlyRecords, Report.ReportType.REPORT_MONTHLY);
//        log.info("✅ 월간 리포트 생성 시작");
//        //ai에 해당 데이터 전달
//        List<Report> reports = openAiService.createReportsFromAiInBatch(monthlies);
//
//        return new ResponseEntity<>(new ListResponseDto<>(
//                mapper.reportsToReportsResponseDtos(reports)), HttpStatus.CREATED);
//    }

    // 구글 TTS (유저가 선택한 reportId 리스트를 받는다)
    @Operation(summary = "TTS 변환", description = "회원이 선택한 레포트들을 음성 데이터로 변환하는 요청.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "변환 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            examples = @ExampleObject(
                                    value = "[\"https://s3.amazonaws.com/bucket/audio1.mp3\", \"https://s3.amazonaws.com/bucket/audio2.mp3\"]"))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 기록 요청시",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"Not Found\"}"))),
            @ApiResponse(responseCode = "500", description = "api 요청시 서버 통신 에러",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"INVALID_SERVER_ERROR\", \"message\": \"Invalid Report type\"}")))
    })
    @PostMapping("/audio")
    public ResponseEntity<List<String>> generateTts(@RequestBody List<Long> reportsId,
                                                    @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        List<String> audioReports =  reportService.reportToGoogleAudio(reportsId, customPrincipal.getMemberId());

        return new ResponseEntity<>(audioReports, HttpStatus.OK);
    }

    //연도별 그룹 조회
    @GetMapping
    @Operation(summary = "연도별 분석 레포트 조회", description = "회원이 선택한 연도별 분석 레포트 조회 요청.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SummaryResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 기록 요청시",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"Not Found\"}")))
    })
    public ResponseEntity getReportList(@Positive @RequestParam(value = "year", required = false) Integer year,  // year를 안보내도 기본값 처리 하도록 설정
                                        @AuthenticationPrincipal CustomPrincipal customPrincipal){
        //year 값이 설정되지 않았다면 올해 기준으로 정렬
        int searchYear =(year != null) ? year : LocalDate.now().getYear();

        List<Report> reports =
                reportService.findMonthlyReports(customPrincipal.getMemberId(), searchYear);

        return new ResponseEntity<>(new ListResponseDto<>(mapper.reportToSummaryResponse(reports)), HttpStatus.OK);
    }

    //Report - MonthlyTitle 상세 그룹 조회
    @GetMapping("/detail")
    @Operation(summary = "특정 월 레포트 데이터 조회", description = "회원이 선택한 월 레포트가 포함하고있는 주 단위 레포트 데이터 조회 요청.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReportResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 기록 요청시",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"Not Found\"}")))
    })
    public ResponseEntity getReports(@RequestParam("monthly-title") String monthlyTitle,
                                     @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        List<Report> reports = reportService.findMonthlyTitleWithReports(monthlyTitle, customPrincipal.getMemberId());

        return new ResponseEntity<>(new ListResponseDto<>(mapper.reportsToReportsResponseDtos(reports)), HttpStatus.OK);
    }

}
