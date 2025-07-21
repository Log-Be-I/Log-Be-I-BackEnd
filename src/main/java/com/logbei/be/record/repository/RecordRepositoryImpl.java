package com.logbei.be.record.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.logbei.be.record.entity.QRecord;
import com.logbei.be.record.entity.Record;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class RecordRepositoryImpl implements RecordRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Long> findMemberIdsWithAtLeastTenRecords(LocalDateTime start, LocalDateTime end){
        QRecord record = QRecord.record;
        return jpaQueryFactory
                .select(record.member.memberId)
                .from(record)
                .where(
                        record.createdAt.between(start,end),
                        record.recordStatus.eq(Record.RecordStatus.RECORD_REGISTERED)
                )
                .groupBy(record.member.memberId)
                .having(record.count().goe(10))
                .fetch();
    }

    @Override
    public List<Record> findByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end, Record.RecordStatus status) {
        QRecord record = QRecord.record;

        return  jpaQueryFactory
                .selectFrom(record)
                .where(
                        record.member.memberId.eq(memberId),
                        record.createdAt.between(start,end),
                        record.recordStatus.eq(status)
                )
                .fetch();
    }

}
