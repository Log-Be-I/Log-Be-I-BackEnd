package com.logbei.be.keyword.entity;

<<<<<<< HEAD:src/main/java/com/springboot/keyword/entity/Keyword.java
import com.springboot.audit.BaseEntity;
import com.springboot.member.entity.Member;
import lombok.*;
=======
import com.logbei.be.audit.BaseEntity;
import com.logbei.be.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
>>>>>>> 3cfffea (패키지명 변경):src/main/java/com/logbei/be/keyword/entity/Keyword.java

import javax.persistence.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "name", callSuper = false)  //name 값만 같으면 동일한 객체로 판단, 경고 방지
public class Keyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long keywordId;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Transient // DB에는 저장 안됨
    private Long memberId;

    public Keyword(String name, Member member) {
        this.name = name;
        this.member = member;
    }

}
