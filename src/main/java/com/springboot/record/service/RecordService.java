package com.springboot.record.service;

import com.springboot.category.service.CategoryService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.service.MemberService;
import com.springboot.record.entity.HistoricalRecord;
import com.springboot.record.entity.Record;
import com.springboot.record.repository.HistoricalRecordRepository;
import com.springboot.record.repository.RecordRepository;
import com.springboot.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;
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

    //기록 수정 : content 수정 데이터는 이관한다.
    @Transactional
    public Record updateRecord(Record record, long memberId) {

        Record findRecord = findVerifiedRecord(record.getRecordId());
        //작성자인지 확인
        AuthorizationUtils.isOwner(findRecord.getMember().getMemberId(), memberId);
        //content 변경 사항 확인
            //Objects.equals(a,b) => a, b 둘 다 null (true), 하나만 null (false)
                // 둘 다 null이 아니면  a.equals(b) 결과 반환
                // NPE 없이 안전하게 비교 가능!
        boolean isContentChanged = !Objects.equals(findRecord.getContent(), record.getContent());

        //content 가 수정되면, 수정 전 데이터 이관
        if (isContentChanged) {
            //데이터 이관을 위해 HistorycalRecord 빈 객체생성
            HistoricalRecord historicalRecord = new HistoricalRecord();
            //기존 Record 필드 복사
            historicalRecord.setHRecordId(findRecord.getRecordId());
            historicalRecord.setContent(findRecord.getContent());
            historicalRecord.setMemberId(findRecord.getMember().getMemberId());
            //수정 상태로 변경
            historicalRecord.setRecordStatus(HistoricalRecord.RecordStatus.RECORD_UPDATED);
            //원본 데이터 ID 추적
            historicalRecord.setOriginalRecordId(findRecord.getRecordId());
            //content 변경 시, 데이터 이관
            historicalRecordRepository.save(historicalRecord);
        }

        // 수정 내용
        Optional.ofNullable(record.getContent())
                .ifPresent(content -> findRecord.setContent(content));
        Optional.ofNullable(record.getRecordTime())
                .ifPresent(time -> findRecord.setRecordTime(time));
        Optional.ofNullable(record.getRecordStatus())
                .ifPresent(status -> findRecord.setRecordStatus(status));
        Optional.ofNullable(record.getMember())
                .ifPresent(member -> findRecord.setMember(member));
        Optional.ofNullable(record.getCategory())
                .ifPresent(category -> findRecord.setCategory(category));

        //수정 데이터 저장
        return repository.save(findRecord);
    }

    //기록 단일 조회
    public Record findRecord(long recordId, long memberId) {
        Record findRecord = findVerifiedRecord(recordId);
        //작성자 or 관리지만 조회 가능
        AuthorizationUtils.isAdminOrOwner(findRecord.getMember().getMemberId(), memberId);
        return findRecord;
    }

    //기록 전체 조회
    public Page<Record> findRecords(int page, int size, long memberId) {
        memberService.validateExistingMember(memberId);

        if(page < 1) {
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }

        Pageable pageable = PageRequest.of(page-1, size, Sort.by("recordTime"));
        //특정 회원이 작성한 질문 목록조 회
        return repository.findAllByMember_MemberId(memberId, pageable);
    }

    //기록 카테고리별 조회

    public void deleteRecord(long recordId, long memberId){

        Record findRecord = findVerifiedRecord(recordId);
        //작성자 또는 관리자만 삭제 가능
        AuthorizationUtils.isAdminOrOwner(findRecord.getMember().getMemberId(), memberId);
        //상태변경
        findRecord.setRecordStatus(Record.RecordStatus.RECORD_DELETED);
        repository.save(findRecord);
    }

    public Record findVerifiedRecord(long recordId) {
        return repository.findById(recordId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.RECORD_NOT_FOUND)
        );
    }
}
