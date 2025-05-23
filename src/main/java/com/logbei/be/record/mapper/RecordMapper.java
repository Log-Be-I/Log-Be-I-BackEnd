package com.logbei.be.record.mapper;


import com.logbei.be.record.dto.RecordResponseDto;
import com.logbei.be.record.entity.Record;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.logbei.be.record.dto.RecordPatchDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import com.logbei.be.record.dto.RecordPostDto;

@Mapper(componentModel = "spring")
public interface RecordMapper {
    @Mapping(target = "member.memberId", source = "memberId")
    @Mapping(target = "category.categoryId", source = "categoryId")
    @Mapping(target = "recordDateTime", expression = "java(stringToLocalDateTime(post.getRecordDateTime()))")
    Record recordPostDtoToRecord(RecordPostDto post);

    @Mapping(target = "member.memberId", source = "memberId")
    @Mapping(target = "category.categoryId", source = "categoryId")
    @Mapping(target = "recordDateTime", expression = "java(updateStringToLocalDateTime(patch.getRecordDateTime()))")
    Record recordPatchDtoToRecord(RecordPatchDto patch);

    default RecordResponseDto recordToRecordResponse(Record record) {
        RecordResponseDto response = new RecordResponseDto(
                record.getRecordId(), record.getRecordDateTime(),
                record.getContent(),
                record.getRecordStatus(),
                record.getMember().getMemberId(),
                record.getCategory().getCategoryId(),
                record.getCreatedAt(),
                record.getModifiedAt()
        );
        return response;
    }

    default List<RecordResponseDto> recordsToRecordResponses(List<Record> records) {
        return records.stream().map(
                record -> recordToRecordResponse(record))
                .collect(Collectors.toList());

    }

    //PostDto의 recordDateTime (String -> LocalDateTime) 타입 변환 메서드
    default LocalDateTime stringToLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return LocalDateTime.now();

            //입력 값이 있다면 문자열을 변환
        } else {
            // 기본 포맷(yyyy-MM-dd HH:mm:ss)으로 파싱
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        }
    }
    //PatchDto의 recordDateTime (String -> LocalDateTime) 타입 변환 메서드
    default LocalDateTime updateStringToLocalDateTime(String dateTimeStr){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return LocalDateTime.parse(dateTimeStr, formatter);

    }

}

