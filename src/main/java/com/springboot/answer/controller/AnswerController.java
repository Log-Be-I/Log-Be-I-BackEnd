package com.springboot.answer.controller;

import com.springboot.answer.dto.AnswerDto;
import com.springboot.answer.entity.Answer;
import com.springboot.answer.mapper.AnswerMapper;
import com.springboot.answer.service.AnswerService;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.utils.UriCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;

@RestController
@RequestMapping("/questions/{question-id}/answers")
@Validated
@RequiredArgsConstructor
public class AnswerController {
    private final static String ANSWER_DEFAULT_URL = "/questions/{question-id}/answers";
    private final AnswerService answerService;
    private final AnswerMapper mapper;

    @PostMapping
    public ResponseEntity postAnswer(@PathVariable("question-id") Long questionId,
                                     @Valid @RequestBody AnswerDto.Post postDto,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        postDto.setQuestionId(questionId);
        postDto.setMemberId(customPrincipal.getMemberId());
        Answer answer = mapper.answerPostToAnswer(postDto);
        Answer createdAnswer = answerService.createAnswer(answer);
        URI location = UriCreator.createUri(ANSWER_DEFAULT_URL, createdAnswer.getQuestion().getQuestionId());
        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{answer-id}")
    public ResponseEntity patchAnswer(@PathVariable("answer-id") @Positive long answerId,
                                      @Valid @RequestBody AnswerDto.Patch patchDto,
                                      @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        patchDto.setAnswerId(answerId);
        Answer updatedAnswer = answerService.updateAnswer(mapper.answerPatchToAnswer(patchDto), customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(updatedAnswer), HttpStatus.OK);
    }

    @DeleteMapping("/{answer-id}")
    public ResponseEntity deleteAnswer(@PathVariable("answer-id") long answerId,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        answerService.deleteAnswer(answerId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
