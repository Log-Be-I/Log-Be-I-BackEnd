package com.springboot.record.repository;

import com.springboot.record.entity.Record;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordRepositoryCustom {
    List<Long> findMemberIdsWithAtLeastTenRecords(LocalDateTime start, LocalDateTime end);

    List<Record> findByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end, Record.RecordStatus status);
}