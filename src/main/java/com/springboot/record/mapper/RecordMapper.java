package com.springboot.record.mapper;

import com.springboot.category.dto.CategoryDto;
import com.springboot.category.entity.Category;
import com.springboot.record.dto.RecordDto;
import com.springboot.record.entity.Record;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public interface RecordMapper {
    @Mapping(target = "member.memberId", source = "memberId")
    @Mapping(target = "category.categoryId", source = "categoryId")
    Record recordPostDtoToRecord(RecordDto.Post post);
    @Mapping(target = "member.memberId", source = "memberId")
    @Mapping(target = "category.categoryId", source = "categoryId")
    Record recordPatchDtoToRecord(RecordDto.Patch patch);

    default RecordDto.Response recordToRecordResponse(Record record) {
        return new RecordDto.Response(
                record.getRecordId(),
                record.getRecordDateTime(),
                record.getContent(),
                record.getRecordStatus(),
                record.getMember().getMemberId(),
                categoryToCategoryResponse(record.getCategory())
        );
    }

    default List<RecordDto.Response> recordsToRecordResponses(List<Record> records) {
        return records.stream()
                .map(record -> recordToRecordResponse(record))
                .collect(Collectors.toList());
    }

    default CategoryDto.Response categoryToCategoryResponse(Category category){
        return new CategoryDto.Response(
                category.getCategoryId(),
                category.getName(),
                category.getImage(),
                category.getMember().getMemberId()
        );
    }

}
