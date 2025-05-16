package com.logbei.be.answer.mapper;


<<<<<<< HEAD:src/main/java/com/springboot/answer/mapper/AnswerMapper.java
import com.springboot.answer.dto.AnswerPatchDto;
import com.springboot.answer.dto.AnswerPostDto;
import com.springboot.answer.dto.AnswerResponseDto;
import com.springboot.answer.entity.Answer;
=======
import com.logbei.be.answer.dto.AnswerDto;
import com.logbei.be.answer.entity.Answer;
>>>>>>> 3cfffea (패키지명 변경):src/main/java/com/logbei/be/answer/mapper/AnswerMapper.java
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnswerMapper {
    @Mapping(target = "member.memberId", source = "memberId")
    @Mapping(target = "question.questionId", source = "questionId")
    Answer answerPostToAnswer(AnswerPostDto postDto);
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
