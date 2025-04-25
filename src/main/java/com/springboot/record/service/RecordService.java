package com.springboot.record.service;

import com.springboot.category.service.CategoryService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
        findRecord.getRecordDateTime();
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
            historicalRecord.setContent(findRecord.getContent());
            historicalRecord.setMemberId(findRecord.getMember().getMemberId());
            //수정 상태로 변경
            historicalRecord.setRecordStatus(HistoricalRecord.RecordStatus.RECORD_UPDATED);
            //원본 데이터 ID 추적
            historicalRecord.setOriginalRecordId(findRecord.getRecordId());
            //content 변경 시, 데이터 이관
            historicalRecordRepository.save(historicalRecord);
        }

    //수정 내용
        Optional.ofNullable(record.getContent())
                .ifPresent(content -> findRecord.setContent(content));
        Optional.ofNullable(record.getRecordDateTime())
                .ifPresent(time -> findRecord.setRecordDateTime(time));
        Optional.ofNullable(record.getRecordStatus())
                .ifPresent(status -> findRecord.setRecordStatus(status));
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
    public Page<Record> findRecords(int page, int size, long memberId, String categoryName, LocalDate startDate, LocalDate endDate) {
        Member foundmember = memberService.validateExistingMember(memberId);
        categoryService.verifyExistsCategory(foundmember, categoryName);

        if(page < 1) {
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }

        Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.ASC));

        if(categoryName.equals("전체")){
            return repository.findAllByMember_MemberIdAndRecordDateBetween(memberId, startDate, endDate, pageable);
        } else {
            return repository.findAllByMember_MemberIdAndCategory_NameIdAndRecordDateBetween(memberId, categoryName, startDate, endDate, pageable);
        }
    }

    //기록 카테고리별 조회

    //기록 삭제
    public void deleteRecord(long recordId, long memberId){

        Record findRecord = findVerifiedRecord(recordId);
        //작성자 또는 관리자만 삭제 가능
        AuthorizationUtils.isAdminOrOwner(findRecord.getMember().getMemberId(), memberId);
        //상태변경
        findRecord.setRecordStatus(Record.RecordStatus.RECORD_DELETED);
        repository.save(findRecord);
    }

    //기록이 저장되어있는지 확인
    public Record findVerifiedRecord(long recordId) {
        return repository.findById(recordId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.RECORD_NOT_FOUND)
        );
    }

    // weekStart: 한 주의 기록
    public List<Record> getWeeklyRecords(LocalDateTime weekStart, LocalDateTime weekEnd) {
       // JPA 쿼리로 특정 회원의 weekStart~weekEnd 사이의 Record 조회
        return repository.findByRecordDateTimeBetween(weekStart, weekEnd);
    }

    // month : 월별 기록
    public List<Record> getMonthlyRecords(LocalDateTime start, LocalDateTime end) {
        // JPA 쿼리로 특정 회원의 weekStart~weekEnd 사이의 Record 조회
        return repository.findByRecordDateTimeBetween(start, end);
    }


}
