package com.springboot.utils;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.record.entity.Record;
import com.springboot.report.dto.RecordForAnalysisDto;
import com.springboot.report.dto.ReportAnalysisRequest;
import com.springboot.report.entity.Report;

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


    // ... 기존 코드 유지

    //모든 사용자들의 data를 가져온다.
//    public static List<ReportAnalysisRequest> createWeeklyReportRequests(List<Record> records) {
////       List<Member> members = records.stream().map(record -> record.getMember())
////               .collect(Collectors.toList());
////       List<Long> memberIds = members.stream().map(member -> member.getMemberId())
////               .collect(Collectors.toList());
////       Long recordMemberId =  memberIds.get(0);
//
//
//        // Map<memberId, Map<주차/월별 title, List<Record>>>
//        Map<Long, Map<String, List<Record>>> grouped = new HashMap<>();
//        Report.ReportType reportType = Report.ReportType.REPORT_WEEKLY;
//        //records순회하면서
//        for (Record record : records) {
//            if(record.getMember().getMemberId() != null) {
    ////                Long memberId = record.getMember().getMemberId();
//                String reportTitle = getWeeklyReportTitle(record.getRecordDateTime());
//                String monthlyReportTitle = getMonthlyReportTitle(record.getRecordDateTime());
//
//                for (RecordForAnalysisDto analysisDto : analysisDtos) {
//                    // 해당 주차의 리스트가 없으면 새로 생성 후 추가
//                    grouped.computeIfAbsent(memberId, k -> new HashMap<>())
//                            .computeIfAbsent(reportTitle, k -> new ArrayList<>())
//                            .add(analysisDto);
//                }
//            }
//        }
//
//
//        //ai 전달 데이터 List로 생성
//        List<ReportAnalysisRequest> result = new ArrayList<>();
//        //이거
//        for (Map.Entry<Long, Map<String, List<Record>>> memberEntry : grouped.entrySet()) {
//            Long memberId = memberEntry.getKey();
//            for (Map.Entry<String, List<Record>> reportEntry : memberEntry.getValue().entrySet()) {
//                String reportTitle = reportEntry.getKey();
//                List<Record> recs = reportEntry.getValue();
//                String monthlyTitle = getMonthlyReportTitle(recs.get(0).getRecordDateTime());
//
//                result.add(new ReportAnalysisRequest(reportTitle, monthlyTitle, memberId, reportType, recs));
//            }
//        }
//
//        return result;
//    }
    //주간 Report 생성 준비
//    public static List<ReportAnalysisRequest> createWeeklyReportRequests(List<Record> records) {
//
//        List<RecordForAnalysisDto> analysisDtoList = recordsToRecordsForAnalysisDto(records);
//        return records.stream().map(record -> recordToWeeklyAnalysisRequest(record,analysisDtoList)).collect(Collectors.toList());
//
//    }
//
//    //월간 Report 생성 준비
//    public static List<ReportAnalysisRequest> createMonthlyReportRequests(List<Record> records) {
//
//        List<RecordForAnalysisDto> analysisDtoList = recordsToRecordsForAnalysisDto(records);
//        return records.stream().map(record -> recordToMonthlyAnalysisRequest(record, analysisDtoList)).collect(Collectors.toList());
//    }

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
                        getWeeklyReportTitle(baseTime), // 또는 getMonthlyTitle
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

    public static List<ReportAnalysisRequest> toMonthlyReportRequests(List<Record> records, Report.ReportType type) {
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

            //List<ReportAnalysisRequest>  생성 및 반환
            return new ReportAnalysisRequest(
                    getMonthlyReportTitle(baseTime), // 또는 getMonthlyTitle
                    getMonthlyReportTitle(baseTime),
                    memberId,
                    type,
                    dtos
            );
        }).collect(Collectors.toList());
    }

    // 요청 분할 유틸
    public static <T> List<List<T>> partitionList(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }

        return result;
    }



    //List<Record> -> List<RecordForAnalysisDto>
    public static List<RecordForAnalysisDto> recordsToRecordsForAnalysisDto (List<Record> records) {
        return records.stream().map(
                record -> recordToRecordForAnalysisDto(record)).collect(Collectors.toList());
    }

    public static RecordForAnalysisDto recordToRecordForAnalysisDto(Record record) {
        return new RecordForAnalysisDto(
                record.getContent(),
                record.getRecordDateTime(),
                record.getCategory().getName()
        );
    }

//    public static List<ReportAnalysisRequest> createMonthlyReportRequests(List<Record> records) {
//        // Map<memberId, Map<월별 title, List<Record>>>
//        Map<Long, Map<String, List<Record>>> grouped = new HashMap<>();
//        Report.ReportType reportType = Report.ReportType.REPORT_MONTHLY;
//
//        for (Record record : records) {
//            Long memberId = record.getMember().getMemberId(); // 또는 record.getMemberId()
//            String monthlyTitle = getMonthlyReportTitle(record.getRecordDateTime());
//            // 해당 주차의 리스트가 없으면 새로 생성 후 추가
//            grouped
//                    .computeIfAbsent(memberId, k -> new HashMap<>())
//                    .computeIfAbsent(monthlyTitle, k -> new ArrayList<>())
//                    .add(record);
//        }
//
//        List<ReportAnalysisRequest> result = new ArrayList<>();
//        //Map.Entry<K,V> : key-value 쌍을 표햔한 객체
//            //K -> memberId, V -> Map<String, List<Record>>
//        for (Map.Entry<Long, Map<String, List<Record>>> memberEntry : grouped.entrySet()) {
//            Long memberId = memberEntry.getKey();
//            for (Map.Entry<String, List<Record>> reportEntry : memberEntry.getValue().entrySet()) {
//                String monthlyTitle = reportEntry.getKey();
//                List<Record> recs = reportEntry.getValue();
//
//                // 월간 보고서는 주간 title이 없으므로 같은 title로 채움
//                result.add(new ReportAnalysisRequest(monthlyTitle, monthlyTitle, memberId, reportType, recs));
//            }
//        }
//
//        return result;
//    }



}