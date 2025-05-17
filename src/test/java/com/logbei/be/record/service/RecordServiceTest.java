package com.logbei.be.record.service;

import com.logbei.be.category.entity.Category;
import com.logbei.be.category.service.CategoryService;
import com.logbei.be.exception.BusinessLogicException;
import com.logbei.be.member.entity.Member;
import com.logbei.be.member.service.MemberService;
import com.logbei.be.record.entity.Record;
import com.logbei.be.record.repository.HistoricalRecordRepository;
import com.logbei.be.record.repository.RecordRepository;
import com.logbei.be.record.service.RecordService;
import com.logbei.be.schedule.entity.Schedule;
import com.logbei.be.schedule.repository.ScheduleRepository;
import com.logbei.be.utils.AuthorizationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecordServiceTest {

    @InjectMocks
    private RecordService recordService;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private HistoricalRecordRepository historicalRecordRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mock 객체 초기화
    }

    @Test
    @DisplayName("saveByType - type이 'record'일 경우 Record 저장")
    void saveByType_record_success() {
        // given
        Map<String, String> data = new HashMap<>();
        data.put("type", "record");
        data.put("content", "테스트 내용");
        data.put("recordDateTime", "2025-05-10T12:00:00");
        data.put("categoryId", "1");

        Long memberId = 1L;
        Member member = new Member();
        member.setMemberId(memberId);
        Category category = new Category();

        Record savedRecord = new Record();
        savedRecord.setContent("테스트 내용");
        savedRecord.setMember(member);

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(member);
        when(categoryService.findVerifiedExistsCategory(1L)).thenReturn(category);
        when(recordRepository.save(any(Record.class))).thenReturn(savedRecord);

        // when
        Object result = recordService.saveByType(data, memberId);

        // then
        assertTrue(result instanceof Record);
        Record resultRecord = (Record) result;
        assertEquals("테스트 내용", resultRecord.getContent());
        verify(recordRepository).save(any(Record.class));
    }

    @Test
    @DisplayName("saveByType - type이 'schedule'일 경우 Schedule 저장")
    void saveByType_schedule_success() {
        // given
        Map<String, String> data = new HashMap<>();
        data.put("type", "schedule");
        data.put("title", "테스트 일정");
        data.put("startDateTime", "2025-05-10T09:00:00");
        data.put("endDateTime", "2025-05-10T10:00:00");

        Long memberId = 1L;
        Schedule savedSchedule = new Schedule();
        savedSchedule.setTitle("테스트 일정");

        when(scheduleRepository.save(any(Schedule.class))).thenReturn(savedSchedule);

        // when
        Object result = recordService.saveByType(data, memberId);

        // then
        assertTrue(result instanceof Schedule);
        Schedule resultSchedule = (Schedule) result;
        assertEquals("테스트 일정", resultSchedule.getTitle());
        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    @DisplayName("saveByType - 알 수 없는 type이면 예외 발생")
    void saveByType_unknownType_throwsException() {
        // given
        Map<String, String> data = new HashMap<>();
        data.put("type", "unknown");

        Long memberId = 1L;

        // when & then
        assertThrows(BusinessLogicException.class, () -> {
            recordService.saveByType(data, memberId);
        });
    }

    @Test
    void createRecord() {
        //given
        long memberId = 1L;
        Member mockMember = new Member();
        mockMember.setMemberId(memberId);

        Record inputRecord = new Record();
        inputRecord.setContent("Test content");

        Record savedRecord = new Record();
        savedRecord.setRecordId(100L);
        savedRecord.setContent("Test content");
        savedRecord.setMember(mockMember);

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(mockMember);
        when(recordRepository.save(any(Record.class))).thenReturn(savedRecord);

        // when
        Record result = recordService.createRecord(inputRecord, memberId);

        // then
        assertNotNull(result);
        assertEquals("Test content", result.getContent());
        assertEquals(memberId, result.getMember().getMemberId());

        verify(memberService, times(1)).findVerifiedExistsMember(memberId);
        verify(recordRepository, times(1)).save(any(Record.class));
    }

    @Test
    @DisplayName("updateRecord - 기록 수정 성공 (content 변경)")
    void updateRecord_success_withContentChange() {
        // given
        long recordId = 1L;
        long memberId = 10L;

        Member member = new Member();
        member.setMemberId(memberId);

        Category category = new Category();
        category.setCategoryId(100L);

        Record existingRecord = new Record();
        existingRecord.setRecordId(recordId);
        existingRecord.setContent("이전 내용");
        existingRecord.setMember(member);
        existingRecord.setCategory(category);

        Record updatedInput = new Record();
        updatedInput.setContent("새로운 내용");
        updatedInput.setCategory(category);

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(member);
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(existingRecord));
        when(categoryService.findVerifiedExistsCategory(category.getCategoryId())).thenReturn(category);
        when(recordRepository.save(any(Record.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Record result = recordService.updateRecord(recordId, updatedInput, memberId);

        // then
        assertNotNull(result);
        assertEquals("새로운 내용", result.getContent());
        assertEquals(memberId, result.getMember().getMemberId());
        assertEquals(category.getCategoryId(), result.getCategory().getCategoryId());

        verify(historicalRecordRepository).save(any());
        verify(recordRepository).save(any());
    }

    @DisplayName("페이지 번호가 1 미만이면 IllegalArgumentException 발생")
    @Test
    void findRecords_throwsException_whenPageIsLessThanOne() {
        // given
        int invalidPage = 0;
        int size = 10;
        Long memberId = 1L;
        Long categoryId = 1L;
        LocalDateTime startDate = LocalDateTime.of(2025, 4, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 4, 30, 23, 59);

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(new Member());
        when(categoryService.findVerifiedExistsCategory(categoryId)).thenReturn(new Category());

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                recordService.findRecords(invalidPage, size, memberId, categoryId, startDate, endDate)
        );
    }

    @Test
    @DisplayName("categoryId가 0일 경우 - 전체 카테고리에서 기록을 조회")
    void findRecords_whenCategoryIdIsZero_shouldCallCorrectRepoMethod() {
        // given
        long memberId = 1L;
        Long categoryId = 0L;
        LocalDateTime start = LocalDateTime.of(2025, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 30, 23, 59);
        int page = 1;
        int size = 5;

        Member mockMember = new Member();
        mockMember.setMemberId(memberId);
        Category mockCategory = new Category();
        mockCategory.setCategoryId(categoryId);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "recordDateTime"));
        Page<Record> emptyPage = new PageImpl<>(List.of());

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(mockMember);
        when(categoryService.findVerifiedExistsCategory(categoryId)).thenReturn(mockCategory);
        when(recordRepository.findAllByMember_MemberIdAndRecordStatusInAndRecordDateTimeBetween(
                eq(memberId),
                anyList(),
                eq(start),
                eq(end),
                eq(pageable)
        )).thenReturn(emptyPage);

        // when
        Page<Record> result = recordService.findRecords(page, size, memberId, categoryId, start, end);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();

        verify(recordRepository, times(1))
                .findAllByMember_MemberIdAndRecordStatusInAndRecordDateTimeBetween(
                        eq(memberId),
                        anyList(),
                        eq(start),
                        eq(end),
                        eq(pageable)
                );

        verify(recordRepository, never())
                .findAllByMember_MemberIdAndCategory_CategoryIdAndRecordStatusInAndRecordDateTimeBetween(
                        anyLong(), anyLong(), anyList(), any(), any(), any());
    }

    @Test
    @DisplayName("deleteRecord - 작성자가 직접 삭제할 경우 성공")
    void deleteRecord_success() {
        // given
        long recordId = 1L;
        long memberId = 10L;

        Member member = new Member();
        member.setMemberId(memberId);

        Record record = new Record();
        record.setRecordId(recordId);
        record.setMember(member);
        record.setRecordStatus(Record.RecordStatus.RECORD_REGISTERED);

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(Record.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ✅ static 메서드 모킹
        try (MockedStatic<AuthorizationUtils> mockedStatic = mockStatic(AuthorizationUtils.class)) {
            mockedStatic
                    .when(() -> AuthorizationUtils.isAdminOrOwner(memberId, memberId))
                    .thenAnswer(invocation -> null); // 아무 예외 없이 통과

            // when
            recordService.deleteRecord(recordId, memberId);

            // then
            assertEquals(Record.RecordStatus.RECORD_DELETED, record.getRecordStatus());
            verify(recordRepository).save(record);
        }
    }

    @Test
    @DisplayName("deleteRecord - 기록이 없으면 예외 발생")
    void deleteRecord_recordNotFound_throwsException() {
        // given
        long recordId = 1L;
        long memberId = 10L;

        when(recordRepository.findById(recordId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessLogicException.class, () -> {
            recordService.deleteRecord(recordId, memberId);
        });
    }

    @Test
    void findRecords_returnsRecordPage_whenValidInputsProvided() {
        // given
        int page = 1;
        int size = 10;
        long memberId = 1L;
        long categoryId = 2L;

        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        Member mockMember = new Member();
        Category mockCategory = new Category();
        mockCategory.setCategoryId(categoryId);

        List<Record> mockRecordList = List.of(new Record(), new Record());
        Page<Record> mockPage = new PageImpl<>(mockRecordList);

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(mockMember);
        when(categoryService.findVerifiedExistsCategory(categoryId)).thenReturn(mockCategory);

        when(recordRepository.findAllByMember_MemberIdAndCategory_CategoryIdAndRecordStatusInAndRecordDateTimeBetween(
                eq(memberId),
                eq(categoryId),
                anyList(),
                eq(start),
                eq(end),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // when
        Page<Record> result = recordService.findRecords(page, size, memberId, categoryId, start, end);

        // then
        assertEquals(2, result.getContent().size());
        verify(memberService).findVerifiedExistsMember(memberId);
        verify(categoryService).findVerifiedExistsCategory(categoryId);
        verify(recordRepository).findAllByMember_MemberIdAndCategory_CategoryIdAndRecordStatusInAndRecordDateTimeBetween(
                eq(memberId), eq(categoryId), anyList(), eq(start), eq(end), any(Pageable.class)
        );
    }

    @Test
    void findRecords_throwsException_whenPageIsInvalid() {
        // given
        int invalidPage = 0;

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                recordService.findRecords(invalidPage, 10, 1L, 1L, LocalDateTime.now(), LocalDateTime.now())
        );
        assertEquals("페이지의 번호는 1 이상이어야 합니다.", exception.getMessage());
    }

}