package com.springboot.utils;

import com.springboot.record.entity.Record;
import com.springboot.report.dto.ReportAnalysisRequest;
import com.springboot.report.entity.Report;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;

public class ReportUtil {
    /**
     * 주어진 날짜가 해당 월의 몇 번째 주인지 반환
     * @param dateTime LocalDate 객체
     * @return 1~5 (1주차~5주차)
     */
    public static int getWeekOfMonth(LocalDateTime dateTime) {
      //항상 월요일 시작으로 고정
        return dateTime.toLocalDate().get(WeekFields.of(DayOfWeek.MONDAY, 1).weekOfMonth());
    }

    /**
     * Record 리스트를 주차별로 그룹핑
     * @param records Record 리스트
     * @return 주차(1~5)를 key로, 해당 주차의 Record 리스트를 value로 하는 Map
     */
//    public static Map<String, List<Record>> groupRecordsByWeek(List<Record> records) {
//       //주차별 <1주차, 기록>
//        Map<String, List<Record>> weekMap = new HashMap<>();
//        for (Record record : records) {
//            LocalDateTime dateTime = record.getRecordDateTime();
//            int year = dateTime.getYear();
//            int month = dateTime.getMonthValue();
//            int week = getWeekOfMonth(dateTime); // 주차 계산 (예 : 1 ) -> 1주차
//            String key = String.format("%d년 %02d월 %d주차", year, month, week); // 예) 2025-04-1
//            // 해당 주차의 리스트가 없으면 새로 생성 후 추가
//            weekMap.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
//        }
//        return weekMap;
//    }
//
//    /**
//     * Record 리스트를 월별로 그룹핑
//     * @param records Record 리스트
//     * @return 월별 key로, 해당 월의 Record 리스트를 value로 하는 Map
//     */
//    public static Map<String, List<Record>> groupRecordsByYearMonthWeek(List<Record> records) {
//        //주차별 <1주차, 기록>
//        Map<String, List<Record>> weekMap = new HashMap<>();
//        for (Record record : records) {
//            LocalDateTime dateTime = record.getRecordDateTime();
//            int year = dateTime.getYear();
//            int month = dateTime.getMonthValue();
//            String key = String.format("%d년 %02d월", year, month); // 예) 2025년 04월
//            // 해당 주차의 리스트가 없으면 새로 생성 후 추가
//            weekMap.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
//        }
//        return weekMap;
//    }

    /**
     * 주간 Report의 title 생성
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
    public static List<ReportAnalysisRequest> createWeeklyReportRequests(List<Record> records) {
        // Map<memberId, Map<주차/월별 title, List<Record>>>
        Map<Long, Map<String, List<Record>>> grouped = new HashMap<>();
        Report.ReportType reportType = Report.ReportType.REPORT_WEEKLY;

        //records순회하면서
        for (Record record : records) {
            Long memberId = record.getMember().getMemberId();
            String reportTitle = getWeeklyReportTitle(record.getRecordDateTime());
            String monthlyReportTitle = getMonthlyReportTitle(record.getRecordDateTime());
            // 해당 주차의 리스트가 없으면 새로 생성 후 추가
            grouped.computeIfAbsent(memberId, k -> new HashMap<>())
                    .computeIfAbsent(reportTitle, k -> new ArrayList<>())
                    .add(record);
        }

        //ai 전달 데이터 List로 생성
        List<ReportAnalysisRequest> result = new ArrayList<>();
        //이거
        for (Map.Entry<Long, Map<String, List<Record>>> memberEntry : grouped.entrySet()) {
            Long memberId = memberEntry.getKey();
            for (Map.Entry<String, List<Record>> reportEntry : memberEntry.getValue().entrySet()) {
                String reportTitle = reportEntry.getKey();
                List<Record> recs = reportEntry.getValue();
                String monthlyTitle = getMonthlyReportTitle(recs.get(0).getRecordDateTime());

                result.add(new ReportAnalysisRequest(reportTitle, monthlyTitle, memberId, reportType, recs));
            }
        }

        return result;
    }

    public static List<ReportAnalysisRequest> createMonthlyReportRequests(List<Record> records) {
        // Map<memberId, Map<월별 title, List<Record>>>
        Map<Long, Map<String, List<Record>>> grouped = new HashMap<>();
        Report.ReportType reportType = Report.ReportType.REPORT_MONTHLY;

        for (Record record : records) {
            Long memberId = record.getMember().getMemberId(); // 또는 record.getMemberId()
            String monthlyTitle = getMonthlyReportTitle(record.getRecordDateTime());
            // 해당 주차의 리스트가 없으면 새로 생성 후 추가
            grouped
                    .computeIfAbsent(memberId, k -> new HashMap<>())
                    .computeIfAbsent(monthlyTitle, k -> new ArrayList<>())
                    .add(record);
        }

        List<ReportAnalysisRequest> result = new ArrayList<>();
        //Map.Entry<K,V> : key-value 쌍을 표햔한 객체
            //K -> memberId, V -> Map<String, List<Record>>
        for (Map.Entry<Long, Map<String, List<Record>>> memberEntry : grouped.entrySet()) {
            Long memberId = memberEntry.getKey();
            for (Map.Entry<String, List<Record>> reportEntry : memberEntry.getValue().entrySet()) {
                String monthlyTitle = reportEntry.getKey();
                List<Record> recs = reportEntry.getValue();

                // 월간 보고서는 주간 title이 없으므로 같은 title로 채움
                result.add(new ReportAnalysisRequest(monthlyTitle, monthlyTitle, memberId, reportType, recs));
            }
        }

        return result;
    }



}
