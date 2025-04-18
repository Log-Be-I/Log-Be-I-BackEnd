package com.springboot.record.mapper;

import com.springboot.record.dto.RecordDto;
import com.springboot.record.entity.Record;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RecordMapper {
    @Mapping(target = "member.memberId", source = "memberId")
    @Mapping(target = "category.categoryId", source = "categoryId")
    @Mapping(target = "recordDateTime", expression = "java(stringToLocalDateTime(post.getRecordDateTime()))")
    Record recordPostDtoToRecord(RecordDto.Post post);
    @Mapping(target = "member.memberId", source = "memberId")
    @Mapping(target = "category.categoryId", source = "categoryId")
    @Mapping(target = "recordDateTime", expression = "java(updateStringToLocalDateTime(patch.getRecordDateTime()))")
    Record recordPatchDtoToRecord(RecordDto.Patch patch);
    RecordDto.Response recordToRecordResponse(Record record);
    List<RecordDto.Response> recordsToRecordResponses(List<Record> records);

    //PostDto의 recordDateTime (String -> LocalDateTime) 타입 변환 메서드
    default LocalDateTime stringToLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return LocalDateTime.now();

            //입력 값이 있다면 문자열을 변환
        } else {
            // 기본 포맷(yyyy-MM-dd HH:mm:ss)으로 파싱
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        }
    }
    //PatchDto의 recordDateTime (String -> LocalDateTime) 타입 변환 메서드
    default LocalDateTime updateStringToLocalDateTime(String dateTimeStr){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTimeStr, formatter);

    }

}

