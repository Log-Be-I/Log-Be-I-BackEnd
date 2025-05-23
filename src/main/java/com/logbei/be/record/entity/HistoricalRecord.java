package com.logbei.be.record.entity;

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
public class HistoricalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hRecordId;

    @Column(nullable = false)
    private String content;
//    @Column(nullable = false)
//    private String recordTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    @Column
    private Long memberId;
    //데이터 이관 시점
    @Column(nullable = false)
    private LocalDateTime operationTime = LocalDateTime.now();

    @Column
    private Long originalRecordId;

    @Enumerated(value = EnumType.STRING)
    private RecordStatus recordStatus = RecordStatus.RECORD_UPDATED;


    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

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
}
