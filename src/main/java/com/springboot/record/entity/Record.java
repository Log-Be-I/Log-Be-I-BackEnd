package com.springboot.record.entity;

import com.springboot.audit.BaseEntity;
import com.springboot.category.entity.Category;
import com.springboot.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Record extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String recordTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    @Enumerated(value = EnumType.STRING)
    private RecordStatus recordStatus = RecordStatus.RECORD_REGISTERED;

    @ManyToOne
    @JoinColumn(name = "member_id")
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
    public void setMember(Member member) {
        this.member = member;
        if(!member.getRecords().contains(this)) {
            member.setRecord(this);
        }
    }

    // category 영속성
    public void setCategory(Category category) {
        this.category = category;
        if(!category.getRecords().contains(this)) {
            category.setRecord(this);
        }
    }
}
