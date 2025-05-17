package com.logbei.be.record.controller;

import com.logbei.be.record.dto.RecordPatchDto;
import com.logbei.be.record.dto.RecordPostDto;
import com.logbei.be.swagger.SwaggerErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.logbei.be.ai.clova.ClovaSpeechService;
import com.logbei.be.ai.openai.service.OpenAiService;
import com.logbei.be.auth.utils.CustomPrincipal;
import com.logbei.be.exception.BusinessLogicException;
import com.logbei.be.exception.ExceptionCode;
import com.logbei.be.record.entity.Record;
import com.logbei.be.record.mapper.RecordMapper;
import com.logbei.be.record.service.RecordService;
import com.logbei.be.responsedto.MultiResponseDto;
import com.logbei.be.responsedto.SingleResponseDto;
import com.logbei.be.schedule.entity.Schedule;
import com.logbei.be.schedule.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.logbei.be.record.dto.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.time.LocalDateTime;


import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
@Tag(name = "기록 API", description = "기록 등록, 조회, 수정, 삭제 관련 API")
public class RecordController {
//    private final static String RECORD_DEFAULT_URL = "/records";
    private final RecordService recordService;
    private final RecordMapper recordMapper;
    private final ClovaSpeechService clovaSpeechService;
    private final OpenAiService openAiService;
    private final ScheduleMapper scheduleMapper;


    @Operation(summary = "기록 등록 (음성)", description = "음성 기반 기록 정보를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "기록 등록 성공",
                    content = @Content(schema = @Schema(implementation = RecordResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "500", description = "api 서버 에러",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"GPT_FAILED\", \"message\": \"Gpt analysis error\"}")))
    })
    @PostMapping("/audio-records")
    public ResponseEntity processVoiceInput(@RequestParam("audio") MultipartFile audioFile,
                                            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) throws IOException {

        //사용자 입력 음성 -> text -> Map<String, String> 타입 변환
        Map<String, String> result = openAiService.createRecordOrSchedule( clovaSpeechService.voiceToText(audioFile) );
        if (result == null || result.isEmpty()) {
            log.warn("GPT 분석 결과가 비어 있음. 요청 무시.");
            throw new BusinessLogicException(ExceptionCode.GPT_FAILED); // 또는 204 반환 등
        }
        Object response = recordService.saveByType(result, customPrincipal.getMemberId());

        // response 타입이 Schedule 이라면
        if (response instanceof Schedule) {
            Schedule schedule = (Schedule) response;

            return ResponseEntity.ok( scheduleMapper.scheduleToscheduleResponseDto(schedule));
            // 만약 Record 타입이라면
        } else if (response instanceof Record) {
            Record record = (Record) response;
            return ResponseEntity.ok(recordMapper.recordToRecordResponse(record));
        } else {

            throw new BusinessLogicException(ExceptionCode.GPT_FAILED);
        }
    }

    @Operation(summary = "기록 등록 (텍스트)", description = "텍스트 기반 기록 정보를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "기록 등록 성공",
                    content = @Content(schema = @Schema(implementation = RecordResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
    })
    @PostMapping("/text-records")
    public ResponseEntity postRecord(@Valid @RequestBody RecordPostDto post,
                                     @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        Record record =recordService.createRecord(recordMapper.recordPostDtoToRecord(post), customPrincipal.getMemberId());

        return new ResponseEntity<>(new SingleResponseDto<>(recordMapper.recordToRecordResponse(record)), HttpStatus.CREATED);
    }

    @Operation(summary = "기록 수정", description = "기록을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기록 수정 성공",
                    content = @Content(schema = @Schema(implementation = RecordResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
    })
    @PatchMapping("/records/{record-id}")
    public ResponseEntity patchRecord(@Positive @PathVariable("record-id") long recordId,
                                      @Valid @RequestBody RecordPatchDto patch,
                                      @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){
        patch.setRecordId(recordId);
        patch.setMemberId(customPrincipal.getMemberId());
        //recordTime이 null이 아닐 때 변환
//        LocalDateTime recordDateTime = DateUtil.parseToLocalDateTime(patch.getRecordDateTime());

        Record textRecord = recordMapper.recordPatchDtoToRecord(patch);
//        textRecord.setRecordDateTime(recordDateTime);
        Record record = recordService.updateRecord(recordId, textRecord, customPrincipal.getMemberId());
        return new ResponseEntity<>( new SingleResponseDto<>(recordMapper.recordToRecordResponse(record)), HttpStatus.OK);
    }

    @Operation(summary = "기록 조회", description = "단일 기록 조회를 위해 요청")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기록 조회 성공",
                    content = @Content(schema = @Schema(implementation = RecordResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
    })
    @GetMapping("/records/{record-id}")
    public ResponseEntity getRecord(@Positive @PathVariable("record-id") long recordId,
                                    @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){
        Record record = recordService.findRecord(recordId, customPrincipal.getMemberId());

        return new ResponseEntity<>( new SingleResponseDto<>(
                recordMapper.recordToRecordResponse(record)), HttpStatus.OK);
    }

    @Operation(summary = "기록 전체 조회", description = "기록 전체를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기록 전체 조회 요청",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RecordResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
    })
    @GetMapping("/records")
    public ResponseEntity getRecords(@Parameter(description = "page", example = "1")
                                         @Positive @RequestParam("page") int page,
                                     @Parameter(description = "size", example = "1")
                                     @Positive @RequestParam("size") int size,
                                     @Parameter(description = "카테고리 ID", example = "1")
                                     @RequestParam("categoryId") Long categoryId,
                                     @Parameter(description = "시작 날짜", example = "2025-04-11T11:30")
                                     @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                     @Parameter(description = "종료 날짜", example = "2025-04-11T11:30")
                                     @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                     @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){
        Page<Record> recordPage = recordService.findRecords(page, size, customPrincipal.getMemberId(), categoryId, startDate, endDate);

        List<Record> records = recordPage.getContent();
        return new ResponseEntity<>( new MultiResponseDto<>(
                // 삭제상태가 아닌 record 만 필터링, 관리자 및 본인만 접근 가능
                recordMapper.recordsToRecordResponses(records), recordPage), HttpStatus.OK);
    }

    @Operation(summary = "특정 기록 삭제", description = "특정 기록을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "기록 삭제 요청",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": \"NO_CONTENT\", \"message\": \"DELETED_DONE\"}"))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
    })
    @DeleteMapping("/records/{record-id}")
    public ResponseEntity deleteRecord(@Positive @PathVariable("record-id") long recordId,
                                       @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){
        recordService.deleteRecord(recordId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
