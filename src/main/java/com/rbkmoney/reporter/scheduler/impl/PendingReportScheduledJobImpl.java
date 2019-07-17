package com.rbkmoney.reporter.scheduler.impl;

import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.scheduler.ScheduledJob;
import com.rbkmoney.reporter.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class PendingReportScheduledJobImpl implements ScheduledJob {

    private final ReportService reportService;

    @Scheduled(fixedDelay = 5 * 1000, initialDelayString = "${scheduled.initialDelay:0}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void run() {
        List<Report> reports = reportService.getPendingReports();
        if (reports.isEmpty()) {
            log.info("No pending reports found, nothing to do");
            return;
        }
        log.debug("Trying to process {} pending reports", reports.size());
        for (Report report : reports) {
            reportService.generateReport(report);
        }
        log.info("End process {} pending reports", reports.size());
    }
}
