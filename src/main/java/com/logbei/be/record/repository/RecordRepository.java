package com.logbei.be.record.repository;

import com.logbei.be.record.entity.Record;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {
   //특정 회원의 기록 조회
//    Page<Record> findAllByMember_MemberId(Long memberId, Pageable pageable);
    //지정한 두 날짜(시각) 사이에 있는 Record 엔티티들을 모두 조회하는 역할
    //start 이상 end 이하인 기록 데이터를 가져온다.
//    List<Record> findByRecordDateTimeBetween(LocalDateTime start, LocalDateTime end);
//    @Query("SELECT r FROM Record r JOIN FETCH r.member WHERE r.recordDateTime BETWEEN :start AND :end")
//    List<Record> findWithMemberByRecordDateTimeBetween(LocalDateTime start, LocalDateTime end);
//    @Query("SELECT r FROM Record r JOIN FETCH r.member WHERE r.recordDateTime BETWEEN :start AND :end AND r.recordStatus = :status ORDER BY r.recordDateTime ASC")
//    List<Record> findRegisteredRecordsWithMemberBetween(LocalDateTime start, LocalDateTime end, Record.RecordStatus status);

// @Query("SELECT r FROM Record r WHERE r.recordDateTime BETWEEN :start AND :end AND r.recordStatus = :status")
// List<Record> findRegisteredRecordsWithMemberBetween(LocalDateTime start, LocalDateTime end, Record.RecordStatus status);
@Query("SELECT r FROM Record r JOIN FETCH r.member WHERE r.recordDateTime BETWEEN :start AND :end AND r.recordStatus = :status ORDER BY r.recordDateTime ASC")
List<Record> findRegisteredRecordsWithMemberBetween(@Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end,
                                                    @Param("status") Record.RecordStatus status);

//    @Query("SELECT r FROM Record r JOIN FETCH r.member WHERE r.recordDateTime BETWEEN :start AND :end ORDER BY r.recordDateTime ASC")
//    List<Record> findRecordsWithMemberBetween(@Param("start") LocalDateTime start,
//                                              @Param("end") LocalDateTime end);
//    Page<Record> findAllByMember_MemberIdAndCategory_CategoryId(Long memberId, Long categoryId, Pageable pageable);

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

}
