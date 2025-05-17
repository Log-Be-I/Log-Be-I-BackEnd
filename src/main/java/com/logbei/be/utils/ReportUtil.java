package com.logbei.be.utils;


import com.logbei.be.exception.BusinessLogicException;
import com.logbei.be.exception.ExceptionCode;
import com.logbei.be.record.entity.Record;
import com.logbei.be.report.dto.RecordForAnalysisDto;
import com.logbei.be.report.dto.ReportAnalysisRequest;
import com.logbei.be.report.entity.Report;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;


//@RequiredArgsConstructor
public class ReportUtil {
    /**
     * 주어진 날짜가 해당 월의 몇 번째 주인지 반환
     *
     * @param dateTime LocalDate 객체
     * @return 1~5 (1주차~5주차)
     */
//    private final LogStorageService  logStorageService;

    public static int getWeekOfMonth(LocalDateTime dateTime) {
        //항상 월요일 시작으로 고정
        return dateTime.toLocalDate().get(WeekFields.of(DayOfWeek.MONDAY, 1).weekOfMonth());
    }

    /**
     * 주간 Report의 title 생성
     *
     * @param dateTime 주간 대표 날짜(예: 월요일)
     * @return "YYYY년 MM월 N주차" 형태의 문자열
     */
    public static String getWeeklyReportTitle(LocalDateTime dateTime) {
        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int week = getWeekOfMonth(dateTime);
        return String.format("%d년 %02d월 %d주차", year, month, week);
    }

    /**
     * 월간 Report의 title 생성
     *
     * @param dateTime 주간 대표 날짜(예: 월요일)
     * @return "YYYY년 MM월" 형태의 문자열
     */
    public static String getMonthlyReportTitle(LocalDateTime dateTime) {
        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        return String.format("%d년 %02d월", year, month);
    }


    //List<Rcord> -> List<ReportAnalysisRequest>
    public static List<ReportAnalysisRequest> toReportRequests(List<Record> records, Report.ReportType type) {
        //Map : 필요한 정보만 뽑아오기 위한 중간다리 역할
        Map<Long, List<Record>> grouped = records.stream()
                .collect(Collectors.groupingBy(record -> record.getMember().getMemberId()));  // Map<K,V> 반환
        //Key : memberId , value : List<Record>
        return grouped.entrySet().stream().map(entry -> {
            Long memberId = entry.getKey();
            List<Record> memberRecords = entry.getValue();
            //Record 의 content, recordDateTime, categoryName 옮겨서 List
            List<RecordForAnalysisDto> dtos = memberRecords.stream()
                    .map(record -> new RecordForAnalysisDto(record.getContent(), record.getRecordDateTime(), record.getCategory().getName()))
                    .collect(Collectors.toList());

            LocalDateTime baseTime = memberRecords.get(0).getRecordDateTime();

            if(type.equals(Report.ReportType.REPORT_WEEKLY)) {
                //List<ReportAnalysisRequest>  생성 및 반환
                return new ReportAnalysisRequest(
                        getWeeklyReportTitle(baseTime),
                        getMonthlyReportTitle(baseTime),
                        memberId,
                        Report.ReportType.REPORT_WEEKLY,
                        dtos
                );

            } else if (type.equals(Report.ReportType.REPORT_MONTHLY)) {
                return new ReportAnalysisRequest(
                        getMonthlyReportTitle(baseTime), // 또는 getMonthlyTitle
                        getMonthlyReportTitle(baseTime),
                        memberId,
                        Report.ReportType.REPORT_MONTHLY,
                        dtos
                );
            } else {
                throw new BusinessLogicException(ExceptionCode.REPORT_TYPE_NOT_FOUND);
            }
        }).collect(Collectors.toList());
    }

//    public static List<ReportAnalysisRequest> toMonthlyReportRequests(List<Record> records, Report.ReportType type) {
//        //Map : 필요한 정보만 뽑아오기 위한 중간다리 역할
//        Map<Long, List<Record>> grouped = records.stream()
//                .collect(Collectors.groupingBy(record -> record.getMember().getMemberId()));  // Map<K,V> 반환
//
//        //Key : memberId , value : List<Record>
//        return grouped.entrySet().stream().map(entry -> {
//            Long memberId = entry.getKey();
//            List<Record> memberRecords = entry.getValue();
//
//            //Record 의 content, recordDateTime, categoryName 옮겨서 List
//            List<RecordForAnalysisDto> dtos = memberRecords.stream()
//                    .map(record -> new RecordForAnalysisDto(record.getContent(), record.getRecordDateTime(), record.getCategory().getName()))
//                    .collect(Collectors.toList());
//
//            LocalDateTime baseTime = memberRecords.get(0).getRecordDateTime();
//
//            //List<ReportAnalysisRequest>  생성 및 반환
//            return new ReportAnalysisRequest(
//                    getMonthlyReportTitle(baseTime), // 또는 getMonthlyTitle
//                    getMonthlyReportTitle(baseTime),
//                    memberId,
//                    type,
//                    dtos
//            );
//        }).collect(Collectors.toList());
//    }

    // 요청 분할 유틸
    public static <T> List<List<T>> partitionList(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }

        return result;
    }

    //List<Record> -> List<RecordForAnalysisDto>
//    public static List<RecordForAnalysisDto> recordsToRecordsForAnalysisDto (List<Record> records) {
//        return records.stream().map(
//                record -> recordToRecordForAnalysisDto(record)).collect(Collectors.toList());
//    }

//    public static RecordForAnalysisDto recordToRecordForAnalysisDto(Record record) {
//        return new RecordForAnalysisDto(
//                record.getContent(),
//                record.getRecordDateTime(),
//                record.getCategory().getName()
//        );
//    }


}