package com.springboot.record.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.record.dto.RecordDto;
import com.springboot.record.entity.Record;
import com.springboot.record.mapper.RecordMapper;
import com.springboot.record.service.RecordService;
import com.springboot.utils.UriCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.net.URI;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
@Validated
public class RecordController {
    private final static String RECORD_DEFAULT_URL = "/records";
    private final RecordService recordService;
    private final RecordMapper mapper;

    @PostMapping
    public ResponseEntity postRecord(@RequestBody RecordDto.Post post,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        post.setMemberId(customPrincipal.getMemberId());
        Record record = recordService.createRecord(mapper.recordPostDtoToRecord(post), customPrincipal.getMemberId());
        URI location = UriCreator.createUri(RECORD_DEFAULT_URL, record.getRecordId());
        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/record-id")
    public ResponseEntity patchRecord(@RequestBody RecordDto.Patch patch,
                                      @AuthenticationPrincipal CustomPrincipal customPrincipal){

    }



}
