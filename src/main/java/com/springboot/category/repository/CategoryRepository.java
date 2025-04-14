package com.springboot.category.repository;

import com.springboot.category.entity.Category;
import com.springboot.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CategoryRepository extends JpaRepository<Category, Long> {
    //특정 회원이 가진 카테고리 이름과 중복되는지 확인
    boolean existsByMemberAndName(Member member, String name);
    //특정 회원의 카테고리 조회
    Page<Category> findByMember_MemberId(Long memberId, Pageable pageable);
}
