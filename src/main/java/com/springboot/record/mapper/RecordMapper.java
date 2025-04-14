package com.springboot.record.mapper;

import com.springboot.record.dto.RecordDto;
import com.springboot.record.entity.Record;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecordMapper {
    Record recordPostDtoToRecord(RecordDto.Post post);
    Record recordPatchDtoToRecord(RecordDto.Patch patch);
    RecordDto.Response recordToRecordResponse(Record record);
    List<RecordDto.Response> recordsToRecordResponses(List<Record> records);
}
