package com.springboot.keyword.repository;

import com.springboot.keyword.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    List<Keyword> findAllByMember_MemberId(long memberId);
}
