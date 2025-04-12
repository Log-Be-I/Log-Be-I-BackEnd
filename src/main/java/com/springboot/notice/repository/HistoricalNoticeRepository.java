package com.springboot.notice.repository;

import com.springboot.notice.entity.HistoricalNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HistoricalNoticeRepository extends JpaRepository<HistoricalNotice, Long> {
    Optional<HistoricalNotice> findByMemberId(long memberId);
}
