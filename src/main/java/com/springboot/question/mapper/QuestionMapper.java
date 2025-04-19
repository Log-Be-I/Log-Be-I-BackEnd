package com.springboot.question.mapper;

import com.springboot.answer.dto.AnswerDto;
import com.springboot.answer.entity.Answer;
import com.springboot.question.dto.QuestionDto;
import com.springboot.question.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionMapper {
    @Mapping(target = "member.memberId", source = "memberId")
    Question questionPostToQuestion(QuestionDto.Post postDto);
    @Mapping(target = "member.memberId", source = "memberId")
    Question questionPatchToQuestion(QuestionDto.Patch patchDto);
    AnswerDto.Response answerToAnswerResponse(Answer answer);
    @Mapping(target = "answer", source = "answer")
    @Mapping(target = "memberId", source = "member.memberId")
    default QuestionDto.Response questionToQuestionResponse(Question question) {
        AnswerDto.Response answerResponse = new AnswerDto.Response();
        if(question.getAnswer() != null) {
            answerResponse.setQuestionId(question.getQuestionId());
            answerResponse.setMemberId(question.getAnswer().getMember().getMemberId());
            answerResponse.setContent(question.getAnswer().getContent());
        } else {
            answerResponse.setAnswerId(null);
            answerResponse.setQuestionId(null);
            answerResponse.setContent(null);
            answerResponse.setMemberId(null);
        }

        QuestionDto.Response questionResponse =
                new QuestionDto.Response(
                        question.getQuestionId(),
                        question.getTitle(),
                        question.getContent(),
                        question.getQuestionStatus(),
                        question.getImage(),
                        question.getMember().getMemberId(),
                        answerResponse,
                        question.getCreatedAt(),
                        question.getModifiedAt(),
                        question.getQuestionAnswerStatus()
                );
        return questionResponse;
    }
    List<QuestionDto.Response> questionsToQuestionResponses(List<Question> questions);
}
