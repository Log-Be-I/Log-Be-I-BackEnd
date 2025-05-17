package com.logbei.be.keyword.mapper;

import com.logbei.be.keyword.dto.KeywordPostDto;
import com.logbei.be.keyword.dto.KeywordResponseDto;
import com.logbei.be.keyword.entity.Keyword;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface KeywordMapper {
    Keyword keywordPostDtoToKeyword (KeywordPostDto keywordPostDto);

    default KeywordResponseDto keywordToKeywordResponseDto (Keyword keyword) {
        KeywordResponseDto keywordResponseDto = new KeywordResponseDto();
        keywordResponseDto.setName(keyword.getName());
        keywordResponseDto.setKeywordId(keyword.getKeywordId());
        return keywordResponseDto;
    }

    default List<Keyword> KeywordPostDtoListToKeywordList (List<KeywordPostDto> keywordPostList) {
        List<Keyword> keywordList = keywordPostList.stream().map(keywordPostDto ->
                        keywordPostDtoToKeyword(keywordPostDto))
                .collect(Collectors.toList());

        return keywordList;
    }

    default List<KeywordResponseDto> keywordListToKeywordResponseDtoList (List<Keyword> keywordList) {
        List<KeywordResponseDto> keywordResponseDtoList = keywordList.stream().map(keyword ->
                keywordToKeywordResponseDto(keyword))
                .collect(Collectors.toList());

        return keywordResponseDtoList;
    }
}
