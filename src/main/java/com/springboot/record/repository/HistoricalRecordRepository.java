package com.springboot.record.repository;

import com.springboot.record.entity.HistoricalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HistoricalRecordRepository extends JpaRepository<HistoricalRecord, Long> {
    Optional<HistoricalRecord> findByMemberId(long memberId);
}
