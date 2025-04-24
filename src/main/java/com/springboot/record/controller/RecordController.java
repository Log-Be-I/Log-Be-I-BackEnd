package com.springboot.record.controller;

import com.springboot.ai.clova.ClovaSpeechService;
import com.springboot.ai.openai.service.OpenAiService;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.record.dto.RecordDto;
import com.springboot.record.entity.Record;
import com.springboot.record.mapper.RecordMapper;
import com.springboot.record.service.RecordService;
import com.springboot.responsedto.MultiResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.schedule.entity.Schedule;
import com.springboot.schedule.mapper.ScheduleMapper;
import com.springboot.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;


import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class RecordController {
//    private final static String RECORD_DEFAULT_URL = "/records";
    private final RecordService recordService;
    private final RecordMapper mapper;
    private final ClovaSpeechService clovaSpeechService;
    private final OpenAiService openAiService;
    private final ScheduleMapper scheduleMapper;
    private final MemberService memberService;

    @PostMapping("/audio-records")
    public ResponseEntity uploadAndRecognize(@RequestParam("audio") MultipartFile audioFile,
                                             @AuthenticationPrincipal CustomPrincipal customPrincipal) throws IOException {

        //사용자 입력 음성 -> text -> Map<String, String> 타입 변환
        Map<String, String> result = openAiService.createRecordOrSchedule( clovaSpeechService.voiceToText(audioFile));
        Object response = recordService.saveByType(result, customPrincipal);

        // response 타입이 Schedule 이라면
        if (response instanceof Schedule) {
            Schedule schedule = (Schedule) response;

            return ResponseEntity.ok( scheduleMapper.scheduleToscheduleResponseDto(schedule));
            // 만약 Record 타입이라면
        } else if (response instanceof Record) {
            Record record = (Record) response;
            return ResponseEntity.ok(mapper.recordToRecordResponse(record));
        } else {
            throw new BusinessLogicException(ExceptionCode.GPT_FAILED);
        }
    }

    @PostMapping("/text-records")
    public ResponseEntity postRecord(@RequestBody RecordDto.Post post,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal) {
//        post.setMemberId(customPrincipal.getMemberId());

        //RecordDateTime을 입력 값이 있다면, 해당 문자열을 LocalDateTime으로 변환
        //문자열을 LocalDateTime 로 변환
//        LocalDateTime recordDateTime = DateUtil.parseToLocalDateTime(post.getRecordDateTime(), "yyyy-MM-dd HH:mm:ss");
        //Dto-> Entity 변환
        Record textRecord = mapper.recordPostDtoToRecord(post);
        //LocalDate 타입으로 변경된 RecordTime set
//        textRecord.setRecordDateTime(recordDateTime);
        Record record =recordService.createRecord(textRecord, customPrincipal.getMemberId());
      
//        URI location = UriCreator.createUri(RECORD_DEFAULT_URL, record.getRecordId());
        RecordDto.Response response = mapper.recordToRecordResponse(record);
//        Member member = memberService.validateExistingMember(response.);
//        response.set
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/records/{record-id}")
    public ResponseEntity patchRecord(@Positive @PathVariable("record-id") long recordId,
                                      @Valid  @RequestBody RecordDto.Patch patch,
                                      @AuthenticationPrincipal CustomPrincipal customPrincipal){
        patch.setRecordId(recordId);
        patch.setMemberId(customPrincipal.getMemberId());
        //recordTime이 null이 아닐 때 변환
//        LocalDateTime recordDateTime = DateUtil.parseToLocalDateTime(patch.getRecordDateTime());

        Record textRecord = mapper.recordPatchDtoToRecord(patch);
//        textRecord.setRecordDateTime(recordDateTime);
        Record record = recordService.updateRecord(textRecord, customPrincipal.getMemberId());
        return new ResponseEntity<>( new SingleResponseDto<>(mapper.recordToRecordResponse(record)), HttpStatus.OK);
    }

    @GetMapping("/records/{record-id}")
    public ResponseEntity getRecord(@Positive @PathVariable("record-id") long recordId,
                                    @AuthenticationPrincipal CustomPrincipal customPrincipal){
        Record record = recordService.findRecord(recordId, customPrincipal.getMemberId());

        return new ResponseEntity<>( new SingleResponseDto<>(
                mapper.recordToRecordResponse(record)), HttpStatus.OK);
    }

    @GetMapping("/records")
    public ResponseEntity getRecords(@Positive @RequestParam("page") int page,
                                     @Positive @RequestParam("size") int size,
                                     @RequestParam("sortBy") Long sortBy,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal){
        Page<Record> recordPage = recordService.findRecords(page, size, customPrincipal.getMemberId(),sortBy);
        List<Record> records = recordPage.getContent();

        return new ResponseEntity<>( new MultiResponseDto<>(
                // 삭제상태가 아닌 record 만 필터링, 관리자 및 본인만 접근 가능
                mapper.recordsToRecordResponses(recordService.nonDeletedRecordAndAuth(records, customPrincipal)),
                recordPage), HttpStatus.OK);
    }

    @DeleteMapping("/records/{record-id}")
    public ResponseEntity deleteRecord(@Positive @PathVariable("record-id") long recordId,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal){
        recordService.deleteRecord(recordId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
