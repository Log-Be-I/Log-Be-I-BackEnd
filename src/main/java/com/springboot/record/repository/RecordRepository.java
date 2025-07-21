package com.springboot.record.repository;

import com.springboot.record.entity.Record;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long>, RecordRepositoryCustom {
   //특정 회원의 기록 조회
   List<Record> findByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end, Record.RecordStatus status);

    // memberId, 날짜 범위, categoryId 받아서 데이터 탐색
    Page<Record> findAllByMember_MemberIdAndCategory_CategoryIdAndRecordStatusInAndRecordDateTimeBetween(
            Long memberId,
            Long categoryId,
            List<Record.RecordStatus> recordStatus,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // Category 전체 선택일 경우 memberId, 날짜 범위로만 탐색
    Page<Record> findAllByMember_MemberIdAndRecordStatusInAndRecordDateTimeBetween(
            Long memberId,
            List<Record.RecordStatus> recordStatus,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    List<Long> findMemberIdsWithAtLeastTenRecords(LocalDateTime start, LocalDateTime end);

}
