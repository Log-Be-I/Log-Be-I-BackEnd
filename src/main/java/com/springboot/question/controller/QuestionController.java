package com.springboot.question.controller;


import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.question.dto.QuestionDto;
import com.springboot.question.entity.Question;
import com.springboot.question.mapper.QuestionMapper;
import com.springboot.question.service.QuestionService;
import com.springboot.responsedto.MultiResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.utils.UriCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/questions")
@Validated
@Slf4j
public class QuestionController {
    private static final String QUESTION_DEFAULT_URL = "/questions";
    private final QuestionService questionService;
    private final QuestionMapper questionMapper;
    private final MemberService memberService;

    public QuestionController(QuestionService questionService, QuestionMapper questionMapper, MemberService memberService) {
        this.questionService = questionService;
        this.questionMapper = questionMapper;
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity postQuestion(@RequestBody QuestionDto.Post dto,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        // dto에 memberId set
        dto.setMemberId(customPrincipal.getMemberId());
        // mapper로 dto -> entity
        Question question = questionMapper.questionPostToQuestion(dto);
        // question만들고
        Question createdQuestion = questionService.createQuestion(question, customPrincipal.getMemberId());
        // URI 만들기
        URI location = UriCreator.createUri(QUESTION_DEFAULT_URL, createdQuestion.getQuestionId());
        return ResponseEntity.created(location).build();
    }

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

    @GetMapping("/{question-id}")
    public ResponseEntity getQuestion(
            @PathVariable("question-id") @Positive long questionId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Question question = questionService.findQuestion(
                questionId);
        return new ResponseEntity<>(new SingleResponseDto<>(questionMapper.questionToQuestionResponse(question)), HttpStatus.OK);
    }

    // 관리자용 전체조회
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

    // 회원용 질문 목록 조회
    @GetMapping("/my-questions/{member-id}")
    public ResponseEntity getMyQuestions(@PathVariable("member-id") Long memberId,
                                         @Positive @RequestParam int page, @Positive @RequestParam int size,
                                         @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        if(customPrincipal.getMemberId() != memberId){
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_OPERATION);
        }
        Page<Question> questionPage = questionService.findMyQuestions(page, size, memberId);
        List<Question> questions = questionPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>
                (questionMapper.questionsToQuestionResponses(questions), questionPage), HttpStatus.OK);
    }

    // 문의글 삭제
    @DeleteMapping("/{question-id}")
    public ResponseEntity deleteQuestion(@PathVariable("question-id") long questionId,
                                         @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        questionService.deleteQuestion(questionId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
