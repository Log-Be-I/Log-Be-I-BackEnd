package com.logbei.be.record.repository;

import com.logbei.be.record.entity.HistoricalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricalRecordRepository extends JpaRepository<HistoricalRecord, Long> {
//    Optional<HistoricalRecord> findByMemberId(long memberId);
}
