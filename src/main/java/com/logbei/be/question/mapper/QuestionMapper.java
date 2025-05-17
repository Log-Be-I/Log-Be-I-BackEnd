package com.logbei.be.question.mapper;

import com.logbei.be.question.dto.QuestionPatchDto;
import com.logbei.be.question.dto.QuestionResponseDto;
import com.logbei.be.question.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.logbei.be.question.dto.QuestionPostDto;
import java.util.List;
import java.util.stream.Collectors;
import com.logbei.be.answer.dto.AnswerResponseDto;
@Mapper(componentModel = "spring")
public interface QuestionMapper {
    @Mapping(target = "member.memberId", source = "memberId")
    Question questionPostToQuestion(QuestionPostDto questionPostDto);
    @Mapping(target = "member.memberId", source = "memberId")
    Question questionPatchToQuestion(QuestionPatchDto patchDto);
    @Mapping(target = "answer", source = "answer")
    @Mapping(target = "memberId", source = "member.memberId")
    default QuestionResponseDto questionToQuestionResponse(Question question) {
        AnswerResponseDto answerResponseDto = new AnswerResponseDto();
        if(question.getAnswer() != null) {
            answerResponseDto.setAnswerId(question.getAnswer().getAnswerId());  // ✅ 추가!
            answerResponseDto.setQuestionId(question.getQuestionId());
            answerResponseDto.setMemberId(question.getAnswer().getMember().getMemberId());
            answerResponseDto
                    .setContent(question.getAnswer().getContent());
        } else {
            answerResponseDto.setAnswerId(null);
            answerResponseDto.setQuestionId(null);
            answerResponseDto.setContent(null);
            answerResponseDto.setMemberId(null);
        }

        QuestionResponseDto questionResponseDto =
                new QuestionResponseDto(
                        question.getQuestionId(),
                        question.getTitle(),
                        question.getContent(),
                        question.getQuestionStatus(),
                        question.getMember().getMemberId(),
                        question.getMember().getEmail(),
                        answerResponseDto,
                        question.getCreatedAt(),
                        question.getModifiedAt(),
                        question.getQuestionAnswerStatus()
                );
        return questionResponseDto;
    }
    default List<QuestionResponseDto> questionsToQuestionResponses(List<Question> questions) {

            return questions.stream().filter(question -> question.getQuestionStatus() == Question.QuestionStatus.QUESTION_REGISTERED)
                    .map(question -> questionToQuestionResponse(question))
                    .collect(Collectors.toList());

    }
}
