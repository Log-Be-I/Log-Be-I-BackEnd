package com.springboot.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Map;

//Map<String, String> 타입을 DB에 String(JSON 문자열)으로 저장
//읽을 때 JSON 문자열을 Map으로 역직렬화
@Converter
public class ContentMapConverter implements AttributeConverter<Map<String, String>, String> {
    //Jackson의 ObjectMapper를 사용해 JSON 직렬화/역직렬화 처리
    private final ObjectMapper mapper = new ObjectMapper();
    //Map -> JSON 문자열로 변환하여 DB에 저장
    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Map to JSON 변환 실패", e);
        }
    }

    //DB에 저장된 JSON 문자열을 Map으로 역직렬화
    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) { // DB에 저장된 JSON 문자열
        try {
            return mapper.readValue(dbData, new TypeReference<Map<String, String>>() {});
        } catch (IOException | JsonProcessingException e) {
            throw new RuntimeException("JSON to Map 변환 실패", e);
        }
    }

}
