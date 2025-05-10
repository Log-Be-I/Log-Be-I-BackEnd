package com.springboot.question.service;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.dashboard.dto.UnansweredQuestion;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.service.MemberService;
import com.springboot.question.entity.Question;
import com.springboot.question.repository.QuestionRepository;
import com.springboot.record.entity.Record;
import com.springboot.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final MemberService memberService;

    public Question createQuestion(Question question, Long memberId){
        //회원이 존재하는지 확인
        question.setMember(memberService.findVerifiedExistsMember(memberId));

        return questionRepository.save(question);
    }

    public Question updateQuestion(Question question, Long memberId){
        Question findQuestion = findVerifiedExistsQuestion(question.getQuestionId());
        // 작성자인지 확인
        AuthorizationUtils.isOwner(findQuestion.getMember().getMemberId(), memberId);
        findQuestion.setMember(memberService.findVerifiedExistsMember(memberId));
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
    public Page<Question> findQuestions(int page, int size, String sortType , boolean onlyNotAnswer, String email, String title){

        // 페이지 번호 검증
        if(page < 1){
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        Sort sort = getSortType(sortType);
        Pageable pageable = PageRequest.of(page -1, size, sort);

        Page<Question> questions;

        if(onlyNotAnswer) {
            //true 면 답변 없는 것만 조회
           questions = questionRepository.findAllByQuestionStatusAndQuestionAnswerStatus(
                    Question.QuestionStatus.QUESTION_REGISTERED, Question.QuestionAnswerStatus.NONE_ANSWER, pageable);
        } else {
            //false 면 전체 조회
            questions = questionRepository.findAllByQuestionStatus(Question.QuestionStatus.QUESTION_REGISTERED, pageable);
        }

        //email, title 조건 추가 필터링
        List<Question> filteredQuestions = questions.getContent().stream().filter(
                question -> {
                    //사용자가 email 검색어를 안넣거나, 넣었을 경우 포함하는지
                    boolean emailMatch = (email == null || question.getMember().getEmail().contains(email));
                    //사용자가 title 검색어를 안넣거나, 넣었을 경우 포함하는지
                    boolean titleMatch = (title == null || question.getTitle().contains(title));
                    //조회하고자하는 내용과 검색어가 유사한 글+이메일만 조회할 수 있도록 가능
                    return emailMatch && titleMatch;
                })
                .collect(Collectors.toList());
        return new PageImpl<>(filteredQuestions, pageable, questions.getTotalElements());
    }

    //회원의 질문 글 전체 조회
    public Page<Question> findMyQuestions(int page, int size, long memberId, String orderBy){

        memberService.findVerifiedExistsMember(memberId);

        if(page < 1 && Objects.equals(orderBy, "DESC")){
            //페이징 및 정렬 정보 생성
            Pageable pageable = PageRequest.of(page -1, size, Sort.by("createdAt").descending());
            //특정 회원이 작성한 질문글을 페이징 처리하여 조회
            return questionRepository.findAllByMember_MemberId(memberId, pageable);
        } else {
            //페이징 및 정렬 정보 생성
            Pageable pageable = PageRequest.of(page -1, size, Sort.by("createdAt"));
            //특정 회원이 작성한 질문글을 페이징 처리하여 조회
            return questionRepository.findAllByMember_MemberId(memberId, pageable);
        }
    }

    //질문글 상세 조회
    public Question findQuestion(Long questionId, Long memberId){
        Question findQuestion = findVerifiedExistsQuestion(questionId);
        // Authentication 통해서 memberId와 관리자인지 받아와서 권한 없는 글에 접근 시 예외처리
        AuthorizationUtils.isAdminOrOwner(findQuestion.getMember().getMemberId(), memberId);
        // 삭제 상태 검증 이후 반환
        return verifyExistsQuestion(findQuestion);
    }

    public void deleteQuestion(Long questionId, long memberId){
        // 질문 존재 확인해서 가져오고
        Question findQuestion = findVerifiedExistsQuestion(questionId);
        // 삭제 상태일 경우 예외 발생
        verifyExistsQuestion(findQuestion);
        // 관리자 또는 작성자와 현재 사용자 같은지 확인
        AuthorizationUtils.isAdminOrOwner(findQuestion.getMember().getMemberId(), memberId);
        // 상태 변경
        findQuestion.setQuestionStatus(Question.QuestionStatus.QUESTION_DELETED);
        // 이미 삭제 상태인지 확인하고 저장
        questionRepository.save(verifyExistsQuestion(findQuestion));
    }

    // 질문 존재하는지 검증
    public Question findVerifiedExistsQuestion(Long questionId){
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);

        return optionalQuestion.orElseThrow(() -> new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND));
    }

    // 답변은 하나밖에 못하기 때문에 있는지 검증
    public void isAnswered(Long questionId){
        if(findVerifiedExistsQuestion(questionId).getQuestionAnswerStatus() == Question.QuestionAnswerStatus.DONE_ANSWER){
            throw new BusinessLogicException(ExceptionCode.CANNOT_CHANGE_QUESTION);
        }
    }

    // 질문이 삭제 상태인지 검증
    public Question verifyExistsQuestion(Question question){
        if(question.getQuestionStatus() == Question.QuestionStatus.QUESTION_DELETED){
            throw new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND);
        }
        return  question;
    }

//    public List<Question> nonDeletedQuestionAndAuth (List<Question> questions, Long memberId) {
//        return questions.stream().filter(question -> question.getQuestionStatus() != Question.QuestionStatus.QUESTION_DELETED)
//                .peek(question ->
//                        // 관리자 or owner 가 아니라면 예외 처리
//                        AuthorizationUtils.isAdminOrOwner(question.getMember().getMemberId(), memberId)
//                ).collect(Collectors.toList());
//
//    }

    // 정렬조건 설정
    private Sort getSortType(String sortType){
        switch (sortType.toUpperCase()){
            case "NEWEST":
                return Sort.by(Sort.Direction.DESC, "questionId");
            case "OLDEST":
                return Sort.by(Sort.Direction.ASC, "questionId");
            default:
                throw new IllegalArgumentException("올바른 정렬 조건을 입력해 주세요: " + sortType);
        }
    }

    // 답변 삭제 시 질문의 answer null 로 만드는 메서드
    public void setAnswerNull(long questionId){
        Question findQuestion = findVerifiedExistsQuestion(questionId);
        findQuestion.setAnswer(null);
        findQuestion.setQuestionAnswerStatus(Question.QuestionAnswerStatus.NONE_ANSWER);
    }

    public List<UnansweredQuestion> findUnansweredQuestions() {
        //DB에서 꺼내온 데이터중 QuestionStatus가 Deleted가 아닌 것들은 filter
        List<Question> questions = questionRepository.findAllByQuestionAnswerStatus(Question.QuestionAnswerStatus.NONE_ANSWER)
                .stream()
                .filter(question -> question.getQuestionStatus() != Question.QuestionStatus.QUESTION_DELETED)
                .collect(Collectors.toList());

        return  questions.stream().map(question -> new UnansweredQuestion(question.getTitle()))
                        .collect(Collectors.toList());
    }

}
