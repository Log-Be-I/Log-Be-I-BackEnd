package com.logbei.be.member;

import com.logbei.be.category.entity.Category;
import com.logbei.be.keyword.entity.Keyword;
import com.logbei.be.member.entity.Member;
import com.logbei.be.question.entity.Question;

import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {

//    public static Member createTestMember(Long id) {
//        Member member = new Member();
//        member.setMemberId(id);
//        member.setEmail("test@example.com");
//        member.setName("테스트");
//        member.setBirth("1990-01-01");
//        member.setRegion("서울");
//        member.setNickname("테스터");
//        member.setProfile("기본프로필");
//        member.setNotification(true);
//        member.setRoles(List.of("USER"));
//        member.setCategories(new ArrayList<>()); // 빈 카테고리 리스트 초기화
//        member.setRefreshToken("mock-refresh-token");
//        return member;
//    }

    public static Member createTestMember(Long id) {
        return createTestMember(id, false);
    }

    public static Member createTestMember(Long id, boolean isAdmin) {
        Member member = new Member();
        member.setMemberId(id);
        member.setEmail(isAdmin ? "admin@example.com" : "user" + id + "@example.com");
        member.setName(isAdmin ? "관리자" : "사용자" + id);
        member.setNickname(isAdmin ? "admin_nick" : "user" + id + "_nick");
        member.setRegion("서울");
        member.setBirth(isAdmin ? "1980-01-01" : "1990-01-01");
        member.setProfile(isAdmin ? "admin.png" : "user.png");
        member.setNotification(true);
        member.setRoles(isAdmin ? List.of("ADMIN") : List.of("USER"));
        member.setMemberStatus(Member.MemberStatus.MEMBER_ACTIVE);
        member.setQuestions(new ArrayList<>());
        return member;
    }
    // 5개의 기본 카테고리를 포함한 테스트용 Member
    public static Member createTestMemberWithDefaultCategories(Long id) {
        Member member = createTestMember(id);

        // 👉 여기서 기본 카테고리 5개 만들어서 넣기
        List<Category> categoryList = List.of(
            new Category("일상", "url", member, true),
            new Category("소비", "url", member, true),
            new Category("건강", "url", member, true),
            new Category("할 일", "url", member, true),
            new Category("기타", "url", member, true)
        );
        member.setCategories(new ArrayList<>(categoryList));

        return member;
    }

    public static Category createTestCategory(String name, Member member) {
        Category category = new Category();
        category.setName(name);
        category.setImage("기본.png");
        category.setMember(member);
        return category;
    }
    public static Keyword createTestKeyword(String name, Member member) {
        Keyword keyword = new Keyword();
        keyword.setName(name);
        if (member != null) {
            keyword.setMember(member);
        }
        return keyword;
    }

    public static Question createTestQuestion(Member member) {
        Question question = new Question();
        question.setQuestionId(1L);
        question.setContent("예제 질문입니다.");
        question.setQuestionStatus(Question.QuestionStatus.QUESTION_REGISTERED);
        question.setMember(member);
        return question;
    }

}