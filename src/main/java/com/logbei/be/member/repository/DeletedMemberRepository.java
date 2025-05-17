package com.logbei.be.member.repository;


import com.logbei.be.member.entity.DeletedMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeletedMemberRepository extends JpaRepository<DeletedMember, Long> {
    Optional<DeletedMember> findByMemberId(long memberId);
    Optional<DeletedMember> findByEmail(String email);
}
