package com.springboot.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    //"yyyy-MM-dd" 문자열을 LocalDate 타입으로 변환
//    public static LocalDate parseToLocalDate(String dateStr) {
//        if (dateStr == null || dateStr.isBlank()) {
//            throw new IllegalArgumentException("날짜 문자열이 비어 있습니다.");
//        }
//        //기본 포맷(yyyy-mm-dd) 으로 파싱
//        return LocalDate.parse(dateStr);
//    }

    // 기본 포맷이 아닌 다른 형태로 지정하고 싶을 경우
    //원하는 패턴 지정하여 문자열을 LocalDate로 변환 (예: LocalDate.of(2025.04.16))
//    public static LocalDate parseToLocalDate(String dateStr, String pattern) {
//        if (dateStr == null || dateStr.isBlank()) {
//            throw new IllegalArgumentException("날짜 문자열이 비어 있습니다.");
//        }
//
//        //페턴에 맞는 형태로 생성
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
//        return LocalDate.parse(dateStr, formatter);
//
//    }

    //원하는 패턴 지정하여 LocalDate를 문자열로 변환
//    public static String formatLocalDateToString(LocalDate date, String pattern) {
//        if (date == null) {
//            throw new IllegalArgumentException("날짜 데이터가 없습니다.");
//        }
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
//        return date.format(formatter);
//    }

    // "yyyy-MM-dd HH:mm:ss" 문자열을 LocalDateTime 타입으로 변환
    public static LocalDateTime parseToLocalDateTime(String dateTimeStr) {
        //입력 값이 없다면
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return LocalDateTime.now();
            //입력 값이 있다면 문자열을 변환
        } else {
            // 기본 포맷(yyyy-MM-dd HH:mm:ss)으로 파싱
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        }
    }

    public static LocalDateTime parseToLocalDateTime(String dateTimeStr, String pattern) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDateTime.parse(dateTimeStr, formatter);

    }


}
