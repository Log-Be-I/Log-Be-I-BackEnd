package com.springboot.answer.controller;

import com.springboot.answer.dto.AnswerPostDto;
import com.springboot.answer.dto.AnswerResponseDto;
import com.springboot.answer.entity.Answer;
import com.springboot.answer.mapper.AnswerMapper;
import com.springboot.answer.service.AnswerService;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.utils.UriCreator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnswerControllerTest {

    @InjectMocks
    private AnswerController answerController;

    @Mock
    private AnswerService answerService;

    @Mock
    private AnswerMapper answerMapper;

    @Test
    void postAnswer_success_withoutSecurityContext() {
        // given
        Long questionId = 1L;
        Long memberId = 100L;
        String content = "이것은 답변입니다";

        AnswerPostDto postDto = new AnswerPostDto();
        postDto.setContent(content);

        Answer answer = new Answer();
        answer.setAnswerId(10L);
        answer.setContent(content);

        AnswerResponseDto responseDto = new AnswerResponseDto();
        responseDto.setAnswerId(answer.getAnswerId());
        responseDto.setContent(answer.getContent());

        CustomPrincipal principal = new CustomPrincipal( "email", memberId);

        given(answerMapper.answerPostToAnswer(any())).willReturn(answer);
        given(answerService.createAnswer(answer, memberId)).willReturn(answer);
        given(answerMapper.answerToAnswerResponse(answer)).willReturn(responseDto);

        // when
        ResponseEntity<?> response = answerController.postAnswer(questionId, postDto, principal);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof SingleResponseDto);
        SingleResponseDto<?> dto = (SingleResponseDto<?>) response.getBody();
        assertEquals(10L, ((AnswerResponseDto) dto.getData()).getAnswerId());
    }
}