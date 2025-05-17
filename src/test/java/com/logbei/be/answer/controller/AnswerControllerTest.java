package com.logbei.be.answer.controller;

import com.logbei.be.answer.entity.Answer;
import com.logbei.be.answer.mapper.AnswerMapper;
import com.logbei.be.answer.service.AnswerService;
import com.logbei.be.auth.utils.CustomPrincipal;
import com.logbei.be.responsedto.SingleResponseDto;
import com.logbei.be.answer.dto.AnswerPostDto;
import com.logbei.be.answer.dto.AnswerResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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