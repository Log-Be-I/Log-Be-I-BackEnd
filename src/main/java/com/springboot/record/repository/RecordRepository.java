package com.springboot.record.repository;

import com.springboot.category.entity.Category;
import com.springboot.record.entity.Record;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {
   //특정 회원의 기록 조회
    Page<Record> findAllByMember_MemberId(Long memberId, Pageable pageable);

}
