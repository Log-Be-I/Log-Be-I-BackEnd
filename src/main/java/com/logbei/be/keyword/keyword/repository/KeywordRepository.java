package com.logbei.be.keyword.keyword.repository;

import com.logbei.be.keyword.keyword.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    List<Keyword> findAllByMember_MemberId(long memberId);
}
