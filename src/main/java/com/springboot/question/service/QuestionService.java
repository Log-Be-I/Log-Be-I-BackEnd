package com.springboot.question.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.question.entity.Question;
import com.springboot.question.repository.QuestionRepository;
import com.springboot.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final MemberService memberService;

    public Question createQuestion(Question question, Long memberId){
        //회원이 존재하는지 확인
        memberService.validateExistingMember(memberId);

        return questionRepository.save(question);
    }

    public Question updateQuestion(Question question, Long memberId){
        Question findQuestion = findVerifiedQuestion(question.getQuestionId());
        // 작성자인지 확인
        AuthorizationUtils.isOwner(findQuestion.getMember().getMemberId(), memberId);
        // 답변 완료시 수정 불가능
        isAnswered(question.getQuestionId());
        // 제목, 내용, visibility

        Optional.ofNullable(question.getTitle())
                .ifPresent(title -> findQuestion.setTitle(title));
        Optional.ofNullable(question.getContent())
                .ifPresent(content -> findQuestion.setContent(content));
        Optional.ofNullable(question.getImage())
                .ifPresent(image ->findQuestion.setImage(image));
        return questionRepository.save(findQuestion);
    }


    //관리자의 질문글 전체 조회
    public Page<Question> findQuestions(int page, int size, String sortType, Member currentMember){
        // 페이지 번호 검증
        if(page < 1){
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        // 정렬 조건 설정
        if(sortType == null || sortType.isBlank()){
            sortType = "newest";
        }
        Sort sort = getSortType(sortType);
        Pageable pageable = PageRequest.of(page -1, size, sort);
        // 비활성화 글 제외하고 조회
        Page<Question> questionPage = questionRepository.findAllQuestionsWithoutDeactivated(pageable);
        return questionPage;
    }

    //회원의 질문 글 전체 조회
    public Page<Question> findMyQuestions(int page, int size, long memberId){
        memberService.validateExistingMember(memberId);

        if(page < 1){
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        //페이징 및 정렬 정보 생성
        Pageable pageable = PageRequest.of(page -1, size, Sort.by("questionId").descending());
        //특정 회원이 작성한 질문글을 페이징 처리하여 조회
        return questionRepository.findAllByMember_MemberId(memberId, pageable);
    }

    //질문글 상세 조회
    public Question findQuestion(Long questionId, Long memberId){
        Question findQuestion = findVerifiedQuestion(questionId);
        // Authentication 통해서 memberId와 관리자인지 받아와서 권한 없는 글에 접근 시 예외처리
        AuthorizationUtils.isAdminOrOwner(findQuestion.getMember().getMemberId(), memberId);
        return findQuestion;
    }

    public void deleteQuestion(Long questionId, long memberId){
        // 질문 존재 확인해서 가져오고
        Question findQuestion = findVerifiedQuestion(questionId);
        // 관리자 또는 작성자와 현재 사용자 같은지 확인
        AuthorizationUtils.isAdminOrOwner(findQuestion.getMember().getMemberId(), memberId);
        // 이미 삭제 상태인지 확인
        verifyQuestionStatus(findQuestion);
        // 상태 변경
        findQuestion.setQuestionStatus(Question.QuestionStatus.QUESTION_DELETED);
        // 저장
        questionRepository.save(findQuestion);

    }

    // 질문 존재하는지 검증
    public Question findVerifiedQuestion(Long questionId){
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        return optionalQuestion.orElseThrow(() -> new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND));
    }

    // 답변은 하나밖에 못하기 때문에 있는지 검증
    public void isAnswered(Long questionId){
        if(findVerifiedQuestion(questionId).getQuestionAnswerStatus() == Question.QuestionAnswerStatus.DONE_ANSWER){
            throw new BusinessLogicException(ExceptionCode.CANNOT_CHANGE_QUESTION);
        }
    }

    // 질문이 삭제 상태인지 검증
    public void verifyQuestionStatus(Question question){
        if(question.getQuestionStatus() == Question.QuestionStatus.QUESTION_DELETED){
            throw new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND);
        }
    }

    // 정렬조건 설정
    private Sort getSortType(String sortType){
        switch (sortType.toUpperCase()){
            case "NEWEST":
                return Sort.by(Sort.Direction.DESC, "createdAt");
            case "OLDEST":
                return Sort.by(Sort.Direction.ASC, "createdAt");
            default:
                throw new IllegalArgumentException("올바른 정렬 조건을 입력해 주세요: " + sortType);
        }
    }

    // 답변 삭제 시 질문의 answer null로 만드는 메서드
    public void setAnswerNull(long questionId){
        Question findQuestion = findVerifiedQuestion(questionId);
        findQuestion.setAnswer(null);
        findQuestion.setQuestionAnswerStatus(Question.QuestionAnswerStatus.NONE_ANSWER);

    }
}
