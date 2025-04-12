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

    @Enumerated(value = EnumType.STRING)
    private KeywordStatus keywordStatus = KeywordStatus.KEYWORD_REGISTERED;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public enum KeywordStatus {
        KEYWORD_REGISTERED("키워드 등록"),
        KEYWORD_DELETED("키워드 삭제");

        @Getter
        private String status;

        KeywordStatus(String status) {
            this.status = status;
        }
    }

    // member 영속성
    public void setMember(Member member) {
        this.member = member;
        if(!member.getKeywords().contains(this)) {
            member.setKeyword(this);
        }
    }
}
