package com.springboot.record.entity;

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
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String recordTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    @Enumerated(value = EnumType.STRING)
    private RecordStatus recordStatus = RecordStatus.RECORD_REGISTERED;

    public enum RecordStatus{
        RECORD_REGISTERED("기록 등록"),
        RECORD_UPDATED("기록 수정"),
        RECORD_DELETED("기록 삭제");

        @Setter
        private String status;

        RecordStatus(String status) {
            this.status = status;
        }
    }
}
