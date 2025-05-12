package com.springboot.member;

import com.springboot.category.entity.Category;
import com.springboot.keyword.entity.Keyword;
import com.springboot.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {

    public static Member createTestMember(Long id) {
        Member member = new Member();
        member.setMemberId(id);
        member.setEmail("test@example.com");
        member.setName("테스트");
        member.setBirth("1990-01-01");
        member.setRegion("서울");
        member.setNickname("테스터");
        member.setProfile("기본프로필");
        member.setNotification(true);
        member.setRoles(List.of("USER"));
        member.setCategories(new ArrayList<>()); // 빈 카테고리 리스트 초기화
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
}