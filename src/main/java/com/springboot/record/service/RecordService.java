package com.springboot.record.service;

import com.springboot.category.service.CategoryService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.service.MemberService;
import com.springboot.record.entity.Record;
import com.springboot.record.repository.HistoricalRecordRepository;
import com.springboot.record.repository.RecordRepository;
import com.springboot.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecordService {
    private final RecordRepository repository;
    private final HistoricalRecordRepository historicalRecordRepository;
    private final MemberService memberService;
    private final CategoryService categoryService;


    public Record createRecord(Record record, long memberId){
        memberService.validateExistingMember(memberId);

        return repository.save(record);
    }
//
//    public Record updateRecord(Record record, long memberId) {
//
//        Record findRecord = findVerifiedRecord(record.getRecordId());
//        //작성자인지 확인
//        AuthorizationUtils.isOwner(findRecord.getMember().getMemberId(), memberId);
//
//        Optional.ofNullable(record.getRecordTime())
//                .ifPresent(recordTime -> findRecord.setRecordTime(recordTime));
//        //이건 그대로 덮어씌우는 방식이니까 좀 더 고민해보고 치도록 하자
//        //따로 만들어야하고 원본 ID를 줘야해(세삼 ㅊㄴ,,,)
//        return repository.save();
//    }

    public Record findVerifiedRecord(long recordId) {
        return repository.findById(recordId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.RECORD_NOT_FOUND)
        );
    }
}
