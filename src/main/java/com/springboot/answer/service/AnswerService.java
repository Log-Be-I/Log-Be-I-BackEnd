package com.springboot.answer.service;

import com.springboot.answer.entity.Answer;
import com.springboot.answer.repository.AnswerRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.service.MemberService;
import com.springboot.question.entity.Question;
import com.springboot.question.service.QuestionService;
import com.springboot.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final MemberService memberService;
    private final QuestionService questionService;


    public Answer createAnswer(Answer answer){
        memberService.validateExistingMember(answer.getMember().getMemberId());
        //로그인한 회원이 관리자인지 확인
        AuthorizationUtils.verifyAdmin();
        Question question = verifyExistsAnswerInQuestion(answer);
        question.setQuestionAnswerStatus(Question.QuestionAnswerStatus.DONE_ANSWER);
        return answerRepository.save(answer);
    }

    public Answer updateAnswer(Answer answer, long adminId){
        Answer findAnswer = findVerifiedAnswer(answer.getAnswerId());
        //수정 하려는 사람이 관리자인지 확인
        AuthorizationUtils.verifyAuthorIsAdmin(findAnswer.getMember().getMemberId(), adminId);

        Optional.ofNullable(answer.getContent())
                .ifPresent(content -> findAnswer.setContent(answer.getContent()));
        findAnswer.setAnswerStatus(Answer.AnswerStatus.ANSWER_UPDATED);

        return answerRepository.save(findAnswer);
    }

    @Transactional
    public void deleteAnswer(long answerId, long adminId){
        //답변 존재 여부 확인
        Answer answer = findVerifiedAnswer(answerId);
        //사용자가 관리자인지 확인
        AuthorizationUtils.verifyAuthorIsAdmin(answer.getMember().getMemberId(), adminId);
        //해당 답변이 달린 Question의 답변 상태 변경
        questionService.setAnswerNull(answer.getQuestion().getQuestionId());
        //답변의 상태 반환
        answer.setAnswerStatus(Answer.AnswerStatus.ANSWER_DELETED);
        answerRepository.save(answer);
    }

    // 질문에 답변이 있는지 검증 후 질문 객체 반환
    private Question verifyExistsAnswerInQuestion(Answer answer) {
        // answer에 담긴 questionId로 question 있는지 검증 후 있으면 객체에 답변 있는지 검증
        Question question = questionService.findVerifiedQuestion(answer.getQuestion().getQuestionId());
        if (question.getAnswer() != null) {
            throw new BusinessLogicException(ExceptionCode.ANSWER_EXISTS);
        }
        return question;
    }

    // 답변이 존재하는지 검증
    private Answer findVerifiedAnswer(long answerId){
        Optional<Answer> optionalAnswer = answerRepository.findById(answerId);
        return optionalAnswer.orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.ANSWER_NOT_FOUND)
        );
    }
}
