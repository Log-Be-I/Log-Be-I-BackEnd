package com.springboot.monthlyreport.service;

import com.springboot.monthlyreport.entity.MonthlyReport;
import com.springboot.monthlyreport.repository.MonthlyReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MonthlyReportService {

    private final MonthlyReportRepository repository;

//    public MonthlyReport createMonthlyReport(MonthlyReport monthlyReport, ) {
//
//    }
}
