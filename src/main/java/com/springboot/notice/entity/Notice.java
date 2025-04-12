package com.springboot.notice.entity;

import com.springboot.audit.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column
    private String image;

    @Enumerated(value = EnumType.STRING)
    private NoticeType noticeType = NoticeType.NOTICE;

    @Enumerated(value = EnumType.STRING)
    private NoticeStatus noticeStatus = NoticeStatus.NOTICE_REGISERED;

    @Enumerated(value = EnumType.STRING)
    private IsPinned isPinned = IsPinned.NONE;

    public enum IsPinned {
        NONE("고정되지 않음"),
        PINNED_("상단 고정"),
        URGENT_PINNED("긴급 고정");

        @Getter
        private String status;

        IsPinned(String status) {
            this.status = status;
        }
    }

    public enum NoticeType {
        EVENT("이벤트 공지사항"),
        NOTICE("공지사항");

        @Getter
        private String status;

        NoticeType(String status) {
            this.status = status;
        }
    }

    public enum NoticeStatus{
        NOTICE_REGISERED("공지 등록"),
        NOTICE_UPDATED("공지 수정"),
        NOTICE_DELTETED("공지 삭제");

        @Getter
        private String status;

        NoticeStatus(String status) {
            this.status = status;
        }
    }

}

