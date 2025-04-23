package com.springboot.record.repository;

import com.springboot.category.entity.Category;
import com.springboot.record.entity.Record;
import com.springboot.report.entity.Report;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {
   //특정 회원의 기록 조회
    Page<Record> findAllByMember_MemberId(Long memberId, Pageable pageable);
    //지정한 두 날짜(시각) 사이에 있는 Record 엔티티들을 모두 조회하는 역할
    //start 이상 end 이하인 기록 데이터를 가져온다.
    List<Record> findByRecordDateTimeBetween(LocalDateTime start, LocalDateTime end);

    Page<Record> findAllByMember_MemberIdAndCategory_CategoryId(Long memberId, Long categoryId, Pageable pageable);
}
