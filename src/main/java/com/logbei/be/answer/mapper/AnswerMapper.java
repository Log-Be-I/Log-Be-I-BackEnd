package com.logbei.be.answer.mapper;



import com.springboot.answer.dto.AnswerResponseDto;
import com.logbei.be.answer.dto.AnswerPatchDto;
import com.logbei.be.answer.entity.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnswerMapper {
    @Mapping(target = "member.memberId", source = "memberId")
    @Mapping(target = "question.questionId", source = "questionId")
    Answer answerPostToAnswer(com.springboot.answer.dto.AnswerPostDto postDto);
    Answer answerPatchToAnswer(AnswerPatchDto patchDto);

    default AnswerResponseDto answerToAnswerResponse(Answer answer) {
        AnswerResponseDto answerResponseDto = new AnswerResponseDto();
        answerResponseDto.setAnswerId(answer.getAnswerId());
        answerResponseDto.setContent(answer.getContent());
        answerResponseDto.setMemberId(answer.getMember().getMemberId());
        answerResponseDto.setQuestionId(answer.getQuestion().getQuestionId());

        return answerResponseDto;
    }
}
