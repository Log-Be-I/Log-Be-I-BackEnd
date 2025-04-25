package com.springboot.keyword.entity;

import com.springboot.audit.BaseEntity;
import com.springboot.member.entity.Member;
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
public class Keyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long keywordId;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public Keyword(String name, Member member) {
        this.name = name;
        this.member = member;
    }

    // member 영속성
    public void setMember(Member member) {
        this.member = member;
        if(!member.getKeywords().contains(this)) {
            member.setKeyword(this);
        }
    }
}
