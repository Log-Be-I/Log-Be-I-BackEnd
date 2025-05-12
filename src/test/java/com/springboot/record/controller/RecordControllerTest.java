package com.springboot.record.controller;

import com.springboot.ai.clova.ClovaSpeechService;
import com.springboot.ai.openai.service.OpenAiService;
import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.record.dto.RecordResponseDto;
import com.springboot.record.entity.Record;
import com.springboot.record.mapper.RecordMapper;
import com.springboot.record.service.RecordService;
import com.springboot.schedule.mapper.ScheduleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AudioRecordControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private RecordController recordController;

    @Mock
    private ClovaSpeechService clovaSpeechService;

    @Mock
    private OpenAiService openAiService;

    @Mock
    private RecordService recordService;

    @Mock
    private RecordMapper recordMapper;


    @Test
    void processVoiceInput_returnsRecordResponse() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(recordController).build();

        // given
        String mockText = "오늘은 커피를 마셨다";
        Map<String, String> gptResult = Map.of(
                "type", "record",
                "content", "커피 마셨다",
                "recordDateTime", "2025-05-12T12:00:00",
                "categoryId", "2"
        );

        Record mockRecord = new Record();
        RecordResponseDto mockDto = new RecordResponseDto();
        mockDto.setContent("커피 마셨다"); // 테스트 검증용 값 세팅

        MockMultipartFile file = new MockMultipartFile("audio", "voice.wav", "audio/wav", new byte[1]);

        when(clovaSpeechService.voiceToText(any())).thenReturn(mockText);
        when(openAiService.createRecordOrSchedule(mockText)).thenReturn(gptResult);
        when(recordService.saveByType(anyMap(), any())).thenReturn(mockRecord); // ✅ 핵심 수정
        when(recordMapper.recordToRecordResponse(mockRecord)).thenReturn(mockDto);

        // when & then
        mockMvc.perform(multipart("/audio-records")
                        .file(file)
                        .principal(() -> "customPrincipal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("커피 마셨다"));
    }
}