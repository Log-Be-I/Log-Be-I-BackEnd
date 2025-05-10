package com.springboot.notice.repository;

import com.springboot.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findTop5ByOrderByCreatedAtDesc();
    //noticeStatus != NOTICE_DELETED 상태만 조회
    Page<Notice> findAllByNoticeStatusNot(
            Notice.NoticeStatus noticeStatus, Pageable pageable);
}
