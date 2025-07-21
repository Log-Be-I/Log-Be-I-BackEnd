package com.logbei.be.audit;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "MODIFIED_AT", columnDefinition = "DATETIME(0)")

    private LocalDateTime modifiedAt;

    @javax.persistence.PrePersist
    public void prePersist() {
        this.createdAt = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Seoul"));
        this.modifiedAt = this.createdAt;
    }

    @javax.persistence.PreUpdate
    public void preUpdate() {
        this.modifiedAt = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Seoul"));
    }

    // Lombok으로 접근자 제공
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
