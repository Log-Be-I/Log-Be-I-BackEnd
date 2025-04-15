package com.springboot.record.mapper;

import com.springboot.record.dto.RecordDto;
import com.springboot.record.entity.Record;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


import java.util.List;

@Mapper(componentModel = "spring")
public interface RecordMapper {
    @Mapping(target = "category.categoryId", source = "categoryId")
    Record recordPostDtoToRecord(RecordDto.Post post);
    @Mapping(target = "category.categoryId", source = "categoryId")
    Record recordPatchDtoToRecord(RecordDto.Patch patch);
    RecordDto.Response recordToRecordResponse(Record record);
    List<RecordDto.Response> recordsToRecordResponses(List<Record> records);
}
