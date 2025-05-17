package com.logbei.be.record.mapper;

import com.logbei.be.category.entity.Category;
import com.logbei.be.member.entity.Member;
import com.logbei.be.record.dto.RecordResponseDto;
import com.logbei.be.record.entity.Record;
import com.logbei.be.record.mapper.RecordMapper;
import com.logbei.be.record.mapper.RecordMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecordMapperTest {

    private final RecordMapper mapper = new RecordMapperImpl(); // MapStruct 구현체 사용

    @Test
    @DisplayName("Record -> RecordResponseDto 변환 성공")
    void recordToRecordResponse_returnsDtoCorrectly() {
        // given
        Member member = new Member();
        member.setMemberId(1L);

        Category category = new Category();
        category.setCategoryId(2L);

        Record record = new Record();
        ReflectionTestUtils.setField(record, "createdAt", LocalDateTime.of(2025, 5, 12, 15, 0));
        ReflectionTestUtils.setField(record, "modifiedAt", LocalDateTime.of(2025, 5, 12, 16, 0));
        record.setRecordId(10L);
        record.setContent("기록 내용");
        record.setRecordStatus(Record.RecordStatus.RECORD_REGISTERED);
        record.setRecordDateTime(LocalDateTime.of(2025, 5, 12, 14, 30));
        record.setMember(member);
        record.setCategory(category);

        // when
        RecordResponseDto dto = mapper.recordToRecordResponse(record);

        // then
        assertThat(dto.getRecordId()).isEqualTo(10L);
        assertThat(dto.getContent()).isEqualTo("기록 내용");
        assertThat(dto.getRecordStatus()).isEqualTo(Record.RecordStatus.RECORD_REGISTERED);
        assertThat(dto.getMemberId()).isEqualTo(1L);
        assertThat(dto.getCategoryId()).isEqualTo(2L);
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 5, 12, 15, 0));
        assertThat(dto.getModifiedAt()).isEqualTo(LocalDateTime.of(2025, 5, 12, 16, 0));
    }

    @Test
    @DisplayName("List<Record> -> List<RecordResponseDto> 변환 성공")
    void recordsToRecordResponses_returnsListCorrectly() {
        // given
        Member member = new Member();
        member.setMemberId(1L);

        Category category = new Category();
        category.setCategoryId(2L);

        Record record1 = new Record();
        record1.setRecordId(10L);
        record1.setContent("기록1");
        record1.setRecordDateTime(LocalDateTime.now());
        record1.setMember(member);
        record1.setCategory(category);

        Record record2 = new Record();
        record2.setRecordId(11L);
        record2.setContent("기록2");
        record2.setRecordDateTime(LocalDateTime.now());
        record2.setMember(member);
        record2.setCategory(category);

        // when
        List<RecordResponseDto> result = mapper.recordsToRecordResponses(List.of(record1, record2));

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRecordId()).isEqualTo(10L);
        assertThat(result.get(1).getRecordId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("stringToLocalDateTime - null 입력 시 현재 시간 반환")
    void stringToLocalDateTime_returnsNowIfNull() {
        LocalDateTime result = mapper.stringToLocalDateTime(null);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("stringToLocalDateTime - 문자열 -> LocalDateTime 변환 성공")
    void stringToLocalDateTime_parsesCorrectly() {
        String dateTimeStr = "2025-05-12T14:30:00";
        LocalDateTime result = mapper.stringToLocalDateTime(dateTimeStr);
        assertThat(result).isEqualTo(LocalDateTime.of(2025, 5, 12, 14, 30));
    }

    @Test
    @DisplayName("updateStringToLocalDateTime - 문자열 -> LocalDateTime 변환 성공")
    void updateStringToLocalDateTime_parsesCorrectly() {
        String dateTimeStr = "2025-05-12T14:30:00";
        LocalDateTime result = mapper.updateStringToLocalDateTime(dateTimeStr);
        assertThat(result).isEqualTo(LocalDateTime.of(2025, 5, 12, 14, 30));
    }
}