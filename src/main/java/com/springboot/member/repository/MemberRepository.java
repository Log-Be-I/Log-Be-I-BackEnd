package com.springboot.member.repository;

import com.springboot.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByMemberId(long memberId);
    Optional<Member> findByNickname(String nickname);
    Page<Member> findByName(String name, Pageable pageable);
    Page<Member> findByEmail(String email, Pageable pageable);
    Page<Member> findByBirth(String birth, Pageable pageable);
    Page<Member> findByMemberStatus(Member.MemberStatus status, Pageable pageable);
}
