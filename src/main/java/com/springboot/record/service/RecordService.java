package com.springboot.record.service;

import com.springboot.ai.openai.service.OpenAiService;
import com.springboot.category.entity.Category;
import com.springboot.category.service.CategoryService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;

import com.springboot.log.service.LogStorageService;

import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.record.entity.HistoricalRecord;
import com.springboot.record.entity.Record;
import com.springboot.record.repository.HistoricalRecordRepository;
import com.springboot.record.repository.RecordRepository;
import com.springboot.schedule.entity.Schedule;
import com.springboot.schedule.repository.ScheduleRepository;
import com.springboot.utils.AuthorizationUtils;
import com.springboot.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecordService {
    private final RecordRepository recordRepository;
    private final HistoricalRecordRepository historicalRecordRepository;
    private final MemberService memberService;
//    private final GoogleCalendarService googleCalendarService;
    private final OpenAiService openAiService;
    private final ScheduleRepository scheduleRepository;
    private final CategoryService categoryService;
    private final LogStorageService logStorageService;

    @Transactional
    public Object saveByType (Map<String, String> data, Long memberId) {

        // type 뽑기
        if(data.get("type").equals("schedule")) {
            try{
                String startDateTime = data.get("startDateTime");
                String endDateTime = data.get("endDateTime");
                // 스케쥴 레포 save 로직
                // 스케쥴 객체 생성
                Schedule schedule = new Schedule();
                schedule.setTitle(data.get("title"));
                schedule.setStartDateTime(DateUtil.parseToLocalDateTime(startDateTime));
                schedule.setEndDateTime(DateUtil.parseToLocalDateTime(endDateTime));

                Member member = new Member();
                member.setMemberId(memberId);
                schedule.setMember(member);
                return scheduleRepository.save(schedule);
            } catch (Exception e) {
                throw new BusinessLogicException(ExceptionCode.RECORD_FAILED);
            }
        } else if (data.get("type").equals("record")) {
            String recordDateTime = data.get("recordDateTime");
            // record 레포 save 로직
            Record record = new Record();
            record.setContent(data.get("content"));
            record.setRecordDateTime(DateUtil.parseToLocalDateTime(recordDateTime));
//            (DateUtil.parseToLocalDateTime());
            record.setCategory(categoryService.findVerifiedExistsCategory(Long.parseLong(data.get("categoryId"))));
            record.setMember(memberService.findVerifiedExistsMember(memberId));
            return recordRepository.save(record);
        } else {
            throw new BusinessLogicException(ExceptionCode.GPT_FAILED);
        }
    }


    public Record createRecord(Record record, long memberId){

       Member member = memberService.findVerifiedExistsMember(memberId);
       record.setMember(member);

       return recordRepository.save(record);
    }

    //기록 수정 : content 수정 데이터는 이관한다.
    @Transactional
    public Record updateRecord(Long recordId, Record record, long memberId) {

        Member member = memberService.findVerifiedExistsMember(memberId);
        Record findRecord = findVerifiedExistsRecord(recordId);
        //작성자인지 확인
        AuthorizationUtils.isOwner(findRecord.getMember().getMemberId(), memberId);
        findRecord.setMember(member);
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

       if(record.getCategory() == null) {
           throw new BusinessLogicException(ExceptionCode.CATEGORY_NOT_FOUND);
       }
        Category category = categoryService.findVerifiedExistsCategory(record.getCategory().getCategoryId());
        findRecord.setCategory(category);
        //수정 데이터 저장
        return recordRepository.save(findRecord);
    }

    //기록 단일 조회
    public Record findRecord(long recordId, long memberId) {
        Member member = memberService.findVerifiedExistsMember(memberId);
        Record findRecord = findVerifiedExistsRecord(recordId);
        Category category = categoryService.findVerifiedExistsCategory(findRecord.getCategory().getCategoryId());
        findRecord.setCategory(category);

        //작성자 or 관리지만 조회 가능
        AuthorizationUtils.isAdminOrOwner(findRecord.getMember().getMemberId(), memberId);
        findRecord.setMember(member);
        // 삭제 상태인지 검증 후 아니라면 record 반환
        return getNotDeletedRecord(findRecord);
    }

    //기록 전체 조회
    public Page<Record> findRecords(int page, int size, long memberId, Long categoryId, LocalDateTime startDate, LocalDateTime endDate) {
        memberService.findVerifiedExistsMember(memberId);
        Category category = categoryService.findVerifiedExistsCategory(categoryId);

        if(page < 1) {
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }

        Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.ASC, "recordDateTime"));
        //deleted 상태만 제외한 Status 생성
        List<Record.RecordStatus> validStatuses = List.of(Record.RecordStatus.RECORD_REGISTERED, Record.RecordStatus.RECORD_UPDATED);
        //DB에서 조회 후, controller 에서 삭제 상태 검증
        if(category.getCategoryId() == 0){
            return recordRepository.findAllByMember_MemberIdAndRecordStatusInAndRecordDateTimeBetween(memberId, validStatuses, startDate, endDate, pageable);
        } else {
            return recordRepository.findAllByMember_MemberIdAndCategory_CategoryIdAndRecordStatusInAndRecordDateTimeBetween(memberId, categoryId, validStatuses, startDate, endDate, pageable);
        }
    }

    //기록 삭제
    public void deleteRecord(long recordId, long memberId){

        Record findRecord = findVerifiedExistsRecord(recordId);
        //작성자 또는 관리자만 삭제 가능
        AuthorizationUtils.isAdminOrOwner(findRecord.getMember().getMemberId(), memberId);
        //상태변경
        findRecord.setRecordStatus(Record.RecordStatus.RECORD_DELETED);
        recordRepository.save(findRecord);
    }

    //기록이 저장되어있는지 확인
    public Record findVerifiedExistsRecord(long recordId) {
        return recordRepository.findById(recordId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.RECORD_NOT_FOUND)
        );
    }

    // weekStart: 한 주의 기록
    public List<Record> getWeeklyRecords(LocalDateTime weekStart, LocalDateTime weekEnd) {
       // JPA 쿼리로 특정 회원의 weekStart~weekEnd 사이의 Record 조회
        List<Record> findRecords = recordRepository.findRegisteredRecordsWithMemberBetween(weekStart, weekEnd, Record.RecordStatus.RECORD_REGISTERED);

        return findRecords;
    }

    // month : 월별 기록
    public List<Record> getMonthlyRecords(LocalDateTime start, LocalDateTime end) {
        // JPA 쿼리로 특정 회원의 weekStart~weekEnd 사이의 Record 조회
        return recordRepository.findRegisteredRecordsWithMemberBetween(start, end, Record.RecordStatus.RECORD_REGISTERED);
    }

//    //삭제상태가 아닌 List<Record> 반환 + 작성자 본인 or 관리자인지 검증
//    public List<Record> nonDeletedRecordAndAuth (List<Record> records, Long memberId) {
//        return records.stream().filter(record -> record.getRecordStatus() != Record.RecordStatus.RECORD_DELETED)
//                .peek(record ->
//                        // 관리자 or owner 가 아니라면 예외 처리
//                AuthorizationUtils.isAdminOrOwner(record.getMember().getMemberId(), memberId)
//                ).collect(Collectors.toList());
//
//    }

//    // 타입이 뭐든 일단 받아서 분기 처리
//    public void handleResponse(Object response) {
//        // response 타입이 Schedule 이라면
//        if (response instanceof Schedule) {
//            Schedule schedule = (Schedule) response;
//        }
//    }
    //삭제상태가 아닌 record 반환
    public Record getNotDeletedRecord(Record record){
        if(record.getRecordStatus() == Record.RecordStatus.RECORD_DELETED) {
           //삭제상태일 경우 예외처리
            throw new BusinessLogicException(ExceptionCode.RECORD_NOT_FOUND);
        }
        return record;
    }
}
