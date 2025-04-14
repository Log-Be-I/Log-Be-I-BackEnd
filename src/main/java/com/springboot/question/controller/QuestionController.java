package com.springboot.question.controller;


import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.notice.dto.NoticeDto;
import com.springboot.question.dto.QuestionDto;
import com.springboot.question.entity.Question;
import com.springboot.question.mapper.QuestionMapper;
import com.springboot.question.service.QuestionService;
import com.springboot.responsedto.MultiResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/questions")
@Validated
@Tag(name = "Question API", description = "문의 글 API")
public class QuestionController {
    private static final String QUESTION_DEFAULT_URL = "/questions";
    private final QuestionService questionService;
    private final QuestionMapper questionMapper;
    private final MemberService memberService;

    //swagger API - 등록
    @Operation(summary = "문의 글 등록", description = "회원이 새로운 문의 글을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "새로운 문의 글 등록"),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}")))
    })

    @PostMapping
    public ResponseEntity postQuestion(@RequestBody QuestionDto.Post dto,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        // dto에 memberId set
        dto.setMemberId(customPrincipal.getMemberId());
        // mapper로 dto -> entity
        Question question = questionMapper.questionPostToQuestion(dto);
        // question만들고
        Question createdQuestion = questionService.createQuestion(question, customPrincipal.getMemberId());
        // URI
        URI location = UriCreator.createUri(QUESTION_DEFAULT_URL, createdQuestion.getQuestionId());
        return ResponseEntity.created(location).build();
    }

    //swagger API - 수정
    @Operation(summary = "문의 글 수정", description = "해당 회원이 기존에 등록된 문의 글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기존에 등록된 문의 글 수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation =  QuestionDto.Response.class))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 공지",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Not Found\", \"message\": \"QUESTION_NOT_FOUND.\"}")))
    })


    @PatchMapping("/{question-id}")
    public ResponseEntity patchQuestion(
            @PathVariable("question-id") @Positive long questionId,
            @Valid @RequestBody QuestionDto.Patch patchDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        patchDto.setQuestionId(questionId);
        patchDto.setMemberId(customPrincipal.getMemberId());
        Question updatedQuestion = questionService.updateQuestion(questionMapper
                .questionPatchToQuestion(patchDto), customPrincipal.getMemberId());
        return new ResponseEntity<>(
                new SingleResponseDto<>(questionMapper.questionToQuestionResponse(updatedQuestion)), HttpStatus.OK);
    }

    //swagger API - 관리자의 전체 조회
    @Operation(summary = "관리자의 문의 글 전체 조회", description = "등록된 문의 글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 문의 글 목록 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = QuestionDto.Response.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}")))
    })


    // 관리자용 전체조회
    //Spring Security에서 제공, 관리자만 접근하도록 설정
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/office")
    public ResponseEntity getQuestions(@Positive @RequestParam int page, @Positive @RequestParam int size,
                                       @RequestParam(defaultValue = "newest") String sortType,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        Member currentMember = memberService.validateExistingMember(customPrincipal.getMemberId());
        Page<Question> questionPage = questionService.findQuestions(page, size, sortType, currentMember);
        List<Question> questions = questionPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>
                (questionMapper.questionsToQuestionResponses(questions), questionPage), HttpStatus.OK);
    }

    //swagger API - 회원의 문의글 전체 조회
    @Operation(summary = "문의 글 목록 조회", description = "등록된 문의 글을 상세 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의 글 상세 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = QuestionDto.Response.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}")))
    })

    // 회원용 질문 목록 조회
    @GetMapping("/my")
    public ResponseEntity getMyQuestions(@Positive @RequestParam int page, @Positive @RequestParam int size,
                                         @AuthenticationPrincipal CustomPrincipal customPrincipal) {


        Page<Question> questionPage = questionService.findMyQuestions(page, size, customPrincipal.getMemberId());
        List<Question> questions = questionPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>
                (questionMapper.questionsToQuestionResponses(questions), questionPage), HttpStatus.OK);
    }

  //swagger API - 상세 조회
    @Operation(summary = "문의 글 상세 조회", description = "등록된 문의 글을 상세 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의 글 상세 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = QuestionDto.Response.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 문의 글",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Not Found\", \"message\": \"QUESTION_NOT_FOUND.\"}")))
    })
    //질문 글 상세조회
    @GetMapping("/{question-id}")
    public ResponseEntity getQuestion(
            @PathVariable("question-id") @Positive long questionId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Question question = questionService.findQuestion(
                questionId, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(questionMapper.questionToQuestionResponse(question)), HttpStatus.OK);
    }

    //swagger API - 삭제
    @Operation(summary = "문의 글 삭제", description = "등록된 문의 글을 삭제 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "문의 글 삭제"),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 문의 글",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Not Found\", \"message\": \"QUESTION_NOT_FOUND.\"}")))
    })

    // 문의글 삭제
    @DeleteMapping("/{question-id}")
    public ResponseEntity deleteQuestion(@PathVariable("question-id") long questionId,
                                         @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        questionService.deleteQuestion(questionId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
