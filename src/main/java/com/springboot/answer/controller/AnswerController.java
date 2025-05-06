package com.springboot.answer.controller;

import com.springboot.answer.dto.AnswerPatchDto;
import com.springboot.answer.dto.AnswerPostDto;
import com.springboot.answer.dto.AnswerResponseDto;
import com.springboot.answer.entity.Answer;
import com.springboot.answer.mapper.AnswerMapper;
import com.springboot.answer.service.AnswerService;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Answer API", description = "답변 API")
public class AnswerController {
    private final AnswerService answerService;
    private final AnswerMapper answerMapper;
    //swagger API - 등록
    @Operation(summary = "답변 등록", description = "관리자가 문의 글에 답변을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "관리자가 문의 글에 답변 등록 성공"),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 문의글",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Not Found\", \"message\": \"QUESTION_NOT_FOUND.\"}")))
    })

    @PostMapping
    public ResponseEntity postAnswer(@PathVariable("question-id") Long questionId,
                                     @Valid @RequestBody AnswerPostDto answerPostDto,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        answerPostDto.setQuestionId(questionId);
        Answer answer = answerService.createAnswer(answerMapper.answerPostToAnswer(answerPostDto), customPrincipal.getMemberId());
        URI location = UriCreator.createUri(questionId);
        return ResponseEntity.created(location).body(new SingleResponseDto<>(
                answerMapper.answerToAnswerResponse(answer)));
    }

    //swagger API - 수정
    @Operation(summary = "답변 수정", description = "관리자가 문의 글에 답변을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기존에 등록된 답변 수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation =  AnswerResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}")))
    })

    @PatchMapping("/{answer-id}")
    public ResponseEntity patchAnswer(@PathVariable("answer-id") @Positive long answerId,
                                      @Valid @RequestBody AnswerPatchDto answerPatchDto,
                                      @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        answerPatchDto.setAnswerId(answerId);
        Answer answer =  answerService.updateAnswer(answerMapper.answerPatchToAnswer(answerPatchDto), customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(
                answerMapper.answerToAnswerResponse(answer)), HttpStatus.OK);
    }

    //swagger API - 삭제
    @Operation(summary = "답변 삭제", description = "관리자가 등록된 답변을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "답변 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 답변",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Not Found\", \"message\": \"ANSWER_NOT_FOUND.\"}")))
    })

    @DeleteMapping("/{answer-id}")
    public ResponseEntity deleteAnswer(@PathVariable("answer-id") long answerId,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        answerService.deleteAnswer(answerId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
