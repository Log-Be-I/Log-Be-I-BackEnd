package com.springboot.category.entity;

import com.springboot.audit.BaseEntity;
import com.springboot.member.entity.Member;
import com.springboot.record.entity.Record;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String image;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    //기본 카테고리 여부 등록(수정 및 삭제 금지)
    // ture = 기본 카테고리(삭제&수정 불가)
    // false = 커스텀 카테고리 (삭제&수정 가능)
    @Column(nullable = false)
    private boolean isDefault = false;


    // member 영속성
    public void setMember(Member member) {
        this.member = member;
        if(!member.getCategories().contains(this)) {
            member.setCategory(this);
        }
    }

    public Category(String name, String image, Member member, boolean isDefault) {
        this.name = name;
        this.image = image;
        this.member = member;
        this.isDefault = isDefault;
    }

}
