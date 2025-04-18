package com.springboot.answer.mapper;


import com.springboot.answer.dto.AnswerDto;
import com.springboot.answer.entity.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnswerMapper {
    @Mapping(target = "member.memberId", source = "memberId")
    @Mapping(target = "question.questionId", source = "questionId")
    Answer answerPostToAnswer(AnswerDto.Post postDto);
    Answer answerPatchToAnswer(AnswerDto.Patch patchDto);
    default AnswerDto.Response answerToAnswerResponse(Answer answer) {
        AnswerDto.Response answerResponseDto = new AnswerDto.Response();
        answerResponseDto.setAnswerId(answer.getAnswerId());
        answerResponseDto.setContent(answer.getContent());
        answerResponseDto.setMemberId(answer.getMember().getMemberId());
        answerResponseDto.setQuestionId(answer.getQuestion().getQuestionId());

        return answerResponseDto;
    }

}
