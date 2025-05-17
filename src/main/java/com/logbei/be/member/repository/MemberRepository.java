package com.logbei.be.member.repository;

import com.logbei.be.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByMemberId(long memberId);
    //관리자 Web - 조회 로직
    //금일 날짜로 조회
    List<Member> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}
