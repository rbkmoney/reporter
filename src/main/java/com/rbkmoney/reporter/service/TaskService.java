package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.domain.tables.pojos.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ReportService reportService;

    @Scheduled(fixedDelay = 500)
    public void processPendingReports() {
        List<Report> reports = reportService.getPendingReports();
        log.debug("Trying to process {} pending reports", reports.size());
        for (Report report : reports) {
            reportService.generateReport(report);
        }
    }

}
