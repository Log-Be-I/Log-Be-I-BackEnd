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

        // CLOVA 에서 허용하는 음성데이터 확장자 목록
        List<String> allowedExtensions = List.of("mp3", "acc", "ac3", "ogg", "flac", "wav", "m4a");

        // 파일 이름에서 확장자 추출
        String originalFilename = audioFile.getOriginalFilename();
        // 확장자명을 담을 문자열 객체 생성
        String extension = "";
        // 확장자명이 비어있지 않고 . 을 포함하고있다면
        if(originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename
                    // 파일 이름에서 .의 인덱스 번호에 +1 을 더해 순수한 확장자 이름만 찾는다
                    .substring(originalFilename.lastIndexOf(".") + 1)
                    // 보통 소문자로 이뤄지지만 대문자가 섞일 수 있으니 소문자로 변경
                    .toLowerCase(); // 컴퓨터는 확장자명의 대소문자 구분을 못함 ex) MP3 == mp3 => true
        }

        // 임시 파일 생성 (확장자 포함)
        // 업로드된 MultipartFile(현재 로직에서는 음성데이터) 을 저장할 임시 파일 객체 생성
        File tempFile = File.createTempFile("clova_", "." + extension);
        // 사용자가 업로드한 오디오 파일 데이터를 임시 파일에 저장
        audioFile.transferTo(tempFile); // 이렇게 담아줘야 file 객체로 API 에 보낼 수 있다.

        // 네이버 클로바 음성인식 서버에 요청 보낼 때 사용할 헤더 설정
        // header 객체 생성
        HttpHeaders headers = new HttpHeaders();
        // Content-Type 은 바이너리 데이터임으로 application/octet-stream
        // 음성 파일은 사람이 직접 읽을 수 없는 0과 1의 데이터로 저장되기 때문이다.
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        // Accept = 응답 __ 응답을 JSON 으로 받겠다는 의미
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // api 요청을 위한 key 설정
        headers.set("X-NCP-APIGW-API-KEY-ID", CLIENT_ID);  // 네이버 콘솔 Client ID
        headers.set("X-NCP-APIGW-API-KEY", API_KEY);       // 네이버 콘솔 Secret Key

        // tempFile 을 CLOVA 에 전송해서 음성 -> 텍스트 변환 결과 받아오기
        String result = clovaSpeechService.recognizeSpeech(tempFile);
        // CLOVA 전송을 위해 임시저장해둔 파일 삭제
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
