package com.logbei.be.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeletedMember {
    @Id //JPA에서 PK 없는 엔티티 관리 어려움, 기본키 없이 데이터 저장하려면 Native Query 사용
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long dMemberId;
    //외래키
    @Column(nullable = false)
    private Long memberId;

    // 생성될때 검증해야하는데 memberId가 없음 그래서 email 보조키로 들고있게함
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime deletedAt = LocalDateTime.now();
}
