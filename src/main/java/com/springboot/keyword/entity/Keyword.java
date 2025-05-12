package com.springboot.keyword.entity;

import com.springboot.audit.BaseEntity;
import com.springboot.member.entity.Member;
import lombok.*;

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
