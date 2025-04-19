package com.springboot.record.controller;

import com.springboot.ai.clova.ClovaSpeechService;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.record.dto.RecordDto;
import com.springboot.record.entity.Record;
import com.springboot.record.mapper.RecordMapper;
import com.springboot.record.service.RecordService;
import com.springboot.responsedto.MultiResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Value("${clova.api.key}")
    private String API_KEY;
    @Value("${clova.api.id}")
    private String CLIENT_ID;

    @PostMapping("/audio-records")
    public ResponseEntity<String> uploadAndRecognize(@RequestParam("audio") MultipartFile audioFile) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.set("X-NCP-APIGW-API-KEY-ID", CLIENT_ID);  // 네이버 콘솔 Client ID
        headers.set("X-NCP-APIGW-API-KEY", API_KEY);       // 네이버 콘솔 Secret Key
        File tempFile = File.createTempFile("clova_", ".m4a");
        audioFile.transferTo(tempFile);

        String result = clovaSpeechService.recognizeSpeech(tempFile);
        tempFile.delete();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/text-records")
    public ResponseEntity postRecord(@RequestBody RecordDto.Post post,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        post.setMemberId(customPrincipal.getMemberId());

        //RecordDateTime을 입력 값이 있다면, 해당 문자열을 LocalDateTime으로 변환
        //문자열을 LocalDateTime 로 변환
//        LocalDateTime recordDateTime = DateUtil.parseToLocalDateTime(post.getRecordDateTime(), "yyyy-MM-dd HH:mm:ss");
        //Dto-> Entity 변환
        Record textRecord = mapper.recordPostDtoToRecord(post);
        //LocalDate 타입으로 변경된 RecordTime set
//        textRecord.setRecordDateTime(recordDateTime);


        Record record =recordService.createRecord(textRecord, customPrincipal.getMemberId());
      
//        URI location = UriCreator.createUri(RECORD_DEFAULT_URL, record.getRecordId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.recordToRecordResponse(record)), HttpStatus.CREATED);
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
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal){
        Page<Record> recordPage = recordService.findRecords(page, size, customPrincipal.getMemberId());
        List<Record> records = recordPage.getContent();

        return new ResponseEntity<>( new MultiResponseDto<>(
                mapper.recordsToRecordResponses(records), recordPage), HttpStatus.OK);
    }

    @DeleteMapping("/records/{record-id}")
    public ResponseEntity deleteRecord(@Positive @PathVariable("record-id") long recordId,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal){
        recordService.deleteRecord(recordId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
