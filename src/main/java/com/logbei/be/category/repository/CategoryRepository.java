package com.logbei.be.category.repository;

import com.logbei.be.category.entity.Category;
import com.logbei.be.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CategoryRepository extends JpaRepository<Category, Long> {
    //Post, Patch -> 특정 회원이 가진 카테고리 이름과 중복되는지 확인
    boolean existsByMemberAndName(Member member, String name);
    //특정 회원의 카테고리 조회
    List<Category> findByMember_MemberId(Long memberId);

}
