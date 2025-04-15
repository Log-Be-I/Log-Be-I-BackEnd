package com.springboot.record.controller;

import com.springboot.record.mapper.RecordMapper;
import com.springboot.record.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordController {
    private final static String RECORD_DEFAULT_URL = "/records";
    private final RecordService recordService;
    private final RecordMapper mapper;



}
