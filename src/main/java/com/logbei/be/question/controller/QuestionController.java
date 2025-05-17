package com.logbei.be.question.controller;

import com.logbei.be.auth.utils.CustomPrincipal;
import com.logbei.be.member.service.MemberService;
import com.logbei.be.question.dto.QuestionDto;
import com.logbei.be.question.dto.QuestionPatchDto;
import com.logbei.be.question.dto.QuestionResponseDto;
import com.logbei.be.question.entity.Question;
import com.logbei.be.question.mapper.QuestionMapper;
import com.logbei.be.question.service.QuestionService;
import com.logbei.be.response.ErrorResponse;
import com.logbei.be.responsedto.MultiResponseDto;
import com.logbei.be.responsedto.SingleResponseDto;
import com.logbei.be.swagger.SwaggerErrorResponse;
import com.logbei.be.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import com.springboot.question.dto.QuestionPostDto;
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

    //swagger API - 등록
   @PostMapping
   @Operation(summary = "문의 글 등록", description = "회원이 새로운 문의 글을 등록합니다.",
           requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                   description = "문의 글 등록 요청", required = true,
                   content = @Content(
                           schema = @Schema(implementation = QuestionPostDto.class),
                           examples = @ExampleObject(value = "{ \"title\": \"로그인 문의\", \"content\" : \"자동로그인 해주세요\", \"image\": \"https://cdn.example.com/img.png\" }")
                   )
           )
   )
   @ApiResponses(value = {
           @ApiResponse(responseCode = "201", description = "문의 글 등록 성공",
                   content = @Content(schema = @Schema(implementation = QuestionResponseDto.class))),
           @ApiResponse(responseCode = "401", description = "인증 실패",
                   content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                           examples = @ExampleObject(value = "{ \"error\": \"Unauthorized\", \"message\": \"로그인이 필요합니다.\" }"))),
           @ApiResponse(responseCode = "403", description = "권한 없음",
                   content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                           examples = @ExampleObject(value = "{ \"error\": \"Forbidden\", \"message\": \"접근 권한이 없습니다.\" }")))
   })
    public ResponseEntity postQuestion(@Valid @RequestBody QuestionPostDto questionPostDto,
                                       @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        Question createdQuestion = questionService.createQuestion(
                questionMapper.questionPostToQuestion(questionPostDto), customPrincipal.getMemberId());
        // URI
        URI location = UriCreator.createUri(QUESTION_DEFAULT_URL, createdQuestion.getQuestionId());
        return ResponseEntity.created(location).body(new SingleResponseDto<>(questionMapper.questionToQuestionResponse(createdQuestion)));
    }

    //swagger API - 수정
    @Operation(summary = "문의 글 수정", description = "해당 회원이 기존에 등록된 문의 글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기존에 등록된 문의 글 수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation =  QuestionResponseDto.class))),
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
            @Valid @RequestBody QuestionPatchDto questionPatchDto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        Question question =  questionService.updateQuestion(questionMapper
                .questionPatchToQuestion(questionPatchDto), customPrincipal.getMemberId());
        return new ResponseEntity<>(
                new SingleResponseDto<>(
                        questionMapper.questionToQuestionResponse(question)), HttpStatus.OK);
    }

    //swagger API - 관리자의 전체 조회
    //관리자용 전체조회
    //Spring Security에서 제공, 관리자만 접근하도록 설정
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/office")
    @Operation(summary = "관리자용 문의 글 목록 조회", description = "관리자가 등록된 문의 글을 페이징, 필터링하여 조회합니다.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (1부터 시작)", example = "1", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "페이지당 조회 수", example = "10", in = ParameterIn.QUERY),
            @Parameter(name = "sortType", description = "정렬 기준 (newest 또는 oldest)", example = "newest", in = ParameterIn.QUERY),
            @Parameter(name = "onlyNotAnswer", description = "미답변 문의만 조회 여부", example = "false", in = ParameterIn.QUERY),
            @Parameter(name = "email", description = "회원 이메일(부분 일치 검색)", example = "user@example.com", in = ParameterIn.QUERY),
            @Parameter(name = "title", description = "문의 제목(부분 일치 검색)", example = "로그인 오류", in = ParameterIn.QUERY)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의 글 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MultiResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"관리자 권한이 필요합니다.\"}")))
    })
    public ResponseEntity getQuestions(@Positive @RequestParam(value = "page") int page,
                                       @Positive @RequestParam(value = "size") int size,
                                       @RequestParam(value = "sortType", defaultValue = "newest") String sortType,
                                       @RequestParam(value = "onlyNotAnswer", defaultValue = "false") boolean onlyNotAnswer,
                                       @RequestParam(value = "email", required = false) String email,
                                       @RequestParam(value = "title", required = false) String title,
                                       @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        Page<Question> questionPage = questionService.findQuestions(page, size, sortType, onlyNotAnswer, email, title);
        List<Question> questions = questionPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>
                (questionMapper.questionsToQuestionResponses(questions), questionPage), HttpStatus.OK);
    }

    //swagger API - 회원의 문의글 전체 조회
    @Operation(summary = "문의 글 목록 조회", description = "등록된 문의 글을 전체 조회합니다.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (1부터 시작)", example = "1", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "페이지당 조회 수", example = "10", in = ParameterIn.QUERY),
            @Parameter(name = "orderBy", description = "내림/오름차순 (DESC 또는 ASC)", example = "DESC", in = ParameterIn.QUERY)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의 글 전체 조회",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = QuestionResponseDto.class)))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"BAD_REQUEST\", \"message\": \"BAD_REQUEST\"}"))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}")))
    })
    //회원용 질문 목록 조회
    @GetMapping("/my")
    public ResponseEntity getMyQuestions(@Positive @RequestParam int page, @Positive @RequestParam int size,
                                         @RequestParam String orderBy,
                                         @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        Page<Question> questionPage = questionService.findMyQuestions(page, size, customPrincipal.getMemberId(), orderBy);
        return new ResponseEntity<>(new MultiResponseDto<>
                (questionMapper.questionsToQuestionResponses(questionPage.getContent()), questionPage), HttpStatus.OK);
    }

  //swagger API - 상세 조회
    @Operation(summary = "문의 글 상세 조회", description = "등록된 문의 글을 상세 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의 글 상세 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = QuestionResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"작성 권한이 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 문의 글",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"QUESTION_NOT_FOUND.\"}"))),
    })
    @Parameter(name = "question-id", description = "조회할 문의 글 ID", required = true, example = "1", in = ParameterIn.PATH)
    //질문 글 상세조회
    @GetMapping("/{question-id}")
    public ResponseEntity getQuestion(
            @PathVariable("question-id") @Positive long questionId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        Question question = questionService.findQuestion(questionId, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(
                questionMapper.questionToQuestionResponse(question)), HttpStatus.OK);
    }

    //swagger API - 삭제
    @Operation(summary = "문의 글 삭제", description = "등록된 문의 글을 삭제 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "문의 글 삭제",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": \"NO_CONTENT\", \"message\": \"DELETED_DONE\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"작성 권한이 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 문의 글",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"QUESTION_NOT_FOUND.\"}"))),
    })
    @Parameter(name = "question-id", description = "삭제할 문의 글 ID", required = true, example = "1", in = ParameterIn.PATH)
    //문의글 삭제
    @DeleteMapping("/{question-id}")
    public ResponseEntity deleteQuestion(@PathVariable("question-id") long questionId,
                                         @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        questionService.deleteQuestion(questionId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
