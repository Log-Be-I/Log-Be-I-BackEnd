package com.springboot.record.controller;

import com.springboot.ai.clova.ClovaSpeechService;
import com.springboot.ai.openai.service.OpenAiService;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.record.dto.RecordResponseDto;
import com.springboot.record.entity.Record;
import com.springboot.record.mapper.RecordMapper;
import com.springboot.record.service.RecordService;
import com.springboot.responsedto.MultiResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecordControllerTest {

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

    @Test
    @DisplayName("기록 페이지 조회 성공")
    void getRecords_returnsPagedRecords() {
        // given
        int page = 1;
        int size = 5;
        Long categoryId = 1L;
        Long memberId = 1L;

        LocalDateTime startDate = LocalDateTime.of(2025, 4, 11, 11, 30);
        LocalDateTime endDate = LocalDateTime.of(2025, 4, 12, 11, 30);

        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getMemberId()).thenReturn(memberId);

        Record record = new Record();
        List<Record> content = List.of(record);
        Page<Record> mockPage = new PageImpl<>(content);

        when(recordService.findRecords(page, size, memberId, categoryId, startDate, endDate))
                .thenReturn(mockPage);
        when(recordMapper.recordsToRecordResponses(content)).thenReturn(List.of(new RecordResponseDto()));

        // when
        ResponseEntity<?> response = recordController.getRecords(
                page, size, categoryId, startDate, endDate, principal);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        MultiResponseDto<?> body = (MultiResponseDto<?>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData()).hasSize(1);

        verify(recordService).findRecords(page, size, memberId, categoryId, startDate, endDate);
        verify(recordMapper).recordsToRecordResponses(content);
    }
}