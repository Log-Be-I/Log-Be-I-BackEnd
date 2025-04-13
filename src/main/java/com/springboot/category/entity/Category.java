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


    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST)
    private List<Record> records = new ArrayList<>();

    // member 영속성
    public void setMember(Member member) {
        this.member = member;
        if(!member.getCategories().contains(this)) {
            member.setCategory(this);
        }
    }

    // record 영속성
    public void setRecord(Record record) {
        if(record.getCategory() != this) {
            record.setCategory(this);
        }
        if(!this.records.contains(record)) {
            records.add(record);
        }
    }
}
