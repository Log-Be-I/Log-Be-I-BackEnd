package com.logbei.be.record.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.logbei.be.audit.BaseEntity;
import com.logbei.be.category.entity.Category;
import com.logbei.be.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Record extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = true)
//    private String recordTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    private LocalDateTime recordDateTime;

    @Enumerated(value = EnumType.STRING)
    private RecordStatus recordStatus = RecordStatus.RECORD_REGISTERED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonBackReference
    private Member member;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public enum RecordStatus{
        RECORD_REGISTERED("기록 등록"),
        RECORD_UPDATED("기록 수정"),
        RECORD_DELETED("기록 삭제");

        @Getter
        private String status;

        RecordStatus(String status) {
            this.status = status;
        }
    }

    // member 영속성
//    public void setMember(Member member) {
//        this.member = member;
//        if(!member.getRecords().contains(this)) {
//            member.setRecord(this);
//        }
//    }

}
