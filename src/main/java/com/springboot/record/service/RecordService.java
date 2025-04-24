package com.springboot.record.service;

import com.springboot.ai.clova.ClovaSpeechService;
import com.springboot.ai.openai.service.OpenAiService;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.category.service.CategoryService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.googleCalendar.dto.GoogleEventDto;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecordService {
    private final RecordRepository repository;
    private final HistoricalRecordRepository historicalRecordRepository;
    private final MemberService memberService;
    private final OpenAiService openAiService;
    private final ScheduleRepository scheduleRepository;
    private final CategoryService categoryService;

    @Value("${clova.api.key}")
    private String API_KEY;
    @Value("${clova.api.id}")
    private String CLIENT_ID;


    public Object saveByType (Map<String, String> data, CustomPrincipal customPrincipal) {

        // type 뽑기
        if(data.get("type").equals("schedule")) {
            // 스케쥴 레포 save 로직
            // 스케쥴 객체 생성
            Schedule schedule = new Schedule();
            schedule.setTitle(data.get("title"));
            schedule.setStartDateTime(data.get("startDateTime"));
            schedule.setEndDateTime(data.get("endDateTime"));

            GoogleEventDto googleEventDto = new GoogleEventDto();
            googleEventDto.setStartDateTime(schedule.getStartDateTime());
            googleEventDto.setEndDateTime(schedule.getEndDateTime());
            googleEventDto.setSummary(schedule.getTitle());
            googleEventDto.setCalendarId(customPrincipal.getEmail());
            // 스케쥴 저장
            scheduleRepository.save(schedule);
            // 스케쥴 객체 리턴
            return schedule;
        } else if (data.get("type").equals("record")) {
            // record 레포 save 로직
            Record record = new Record();
            record.setContent(data.get("content"));
            record.setRecordDateTime(DateUtil.parseToLocalDateTime(data.get("recordDateTime")));
            record.setCategory(categoryService.findCategory(Long.parseLong(data.get("categoryId")), customPrincipal.getMemberId()));
            return record;
        } else {
            throw new BusinessLogicException(ExceptionCode.GPT_FAILED);
        }
    }


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
    public Page<Record> findRecords(int page, int size, long memberId) {
        memberService.validateExistingMember(memberId);

        if(page < 1) {
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }

        Pageable pageable = PageRequest.of(page-1, size, Sort.by("recordTime"));
        //특정 회원이 작성한 질문 목록 조회
        return repository.findAllByMember_MemberId(memberId, pageable);
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

    // Json 으로 schedule 과 record 구분하여 저장
    public void voiceTextTo(String result) throws IOException {
        // 결과 값을 JSON 으로 변경
        String prompt = openAiService.chatWithScheduleAndRecord(result);

        // prompt 넣어주기
        String json = openAiService.sendRecord(prompt);

        // json 역직렬화
        Map<String, String> response = openAiService.jsonToString(json);

        // type 뽑기
        if(response.get("type").equals("schedule")) {
            // 스케쥴 레포 save 로직
        } else if(response.get("type").equals("record")) {
            // record 레포 save 로직
        }
    }

    // 타입이 뭐든 일단 받아서 분기 처리
    public void handleResponse(Object response) {
        // response 타입이 Schedule 이라면
        if (response instanceof Schedule) {
            Schedule schedule = (Schedule) response;
            // Schedule 에 저장


            // 만약 Record 타입이라면
        } else if (response instanceof Record) {
            Record record = (Record) response;
            repository.save(record);
        }
    }

}
