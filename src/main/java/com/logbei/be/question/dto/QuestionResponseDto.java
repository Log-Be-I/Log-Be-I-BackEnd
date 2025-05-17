package com.logbei.be.question.dto;

import com.logbei.be.question.entity.Question;
import com.springboot.answer.dto.AnswerResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponseDto {
    @Schema(description = "문의 글 ID", example = "1")
    private Long questionId;

    @Schema(description = "문의 글 제목", example = "로그인 문의드립니다.")
    private String title;

    @Schema(description = "문의 글 내용", example = "자동로그인 할 수 있게 해주세요.")
    private String content;

    @Schema(description = "문의 글 상태", example = "QUESTION_REGISTERED")
    private Question.QuestionStatus questionStatus;

    @Schema(description = "작성자 ID", example = "1")
    private Long memberId;

    @Schema(description = "작성자 email", example = "sensibility0510@gmail.com")
    private String writerEmail;

    // implementation 으로 클래스 자체의 구조를 가져오는건 가능하지만 각 컬럼 구조마다 임의값인 데이터라도 넣어줘야한다.
    @Schema(description = "답변", example = "{\n" +
            "  \"answerId\": 1,\n" +
            "  \"content\": \"문의에 대한 답변입니다.\",\n" +
            "  \"questionId\": 1,\n" +
            "  \"memberId\": 42,\n" +
            "  \"createdAt\": \"2025-04-11T12:00:00\",\n" +
            "  \"modifiedAt\": \"2025-04-11T13:00:00\"\n" +
            "}")
    private AnswerResponseDto answer;

    @Schema(description = "생성일", example = "2025-04-11T11:30")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2025-04-11T11:30")
    private LocalDateTime modifiedAt;

    @Schema(description = "문의 글 답변 상태", example = "DONE_ANSWER")
    private Question.QuestionAnswerStatus questionAnswerStatus;
}
