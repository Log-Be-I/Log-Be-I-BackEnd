package com.logbei.be.notice.repository;

<<<<<<< HEAD:src/main/java/com/springboot/notice/repository/NoticeRepository.java
import com.springboot.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
=======
import com.logbei.be.notice.entity.Notice;
>>>>>>> 3cfffea (패키지명 변경):src/main/java/com/logbei/be/notice/repository/NoticeRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findTop5ByOrderByCreatedAtDesc();
    //noticeStatus != NOTICE_DELETED 상태만 조회
    Page<Notice> findAllByNoticeStatusNot(
            Notice.NoticeStatus noticeStatus, Pageable pageable);
}
