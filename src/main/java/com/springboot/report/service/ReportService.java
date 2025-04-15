package com.springboot.report.service;

import com.springboot.report.entity.Report;
import com.springboot.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository repository;

    public Report createReport(Report report) {

        return repository.save(report);
    }

}
