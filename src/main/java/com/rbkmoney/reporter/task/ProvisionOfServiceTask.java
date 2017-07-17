package com.rbkmoney.reporter.task;

import com.rbkmoney.reporter.service.ReportService;

import java.time.Instant;
import java.time.YearMonth;

/**
 * Created by tolkonepiu on 17/07/2017.
 */
public class ProvisionOfServiceTask implements Runnable {

    private final Task task;
    private final ReportService reportService;

    public ProvisionOfServiceTask(Task task, ReportService reportService) {
        this.task = task;
        this.reportService = reportService;
    }

    @Override
    public void run() {
        YearMonth currentYearMonth = YearMonth.now(task.getTimezone().toZoneId());
        Instant fromTime = currentYearMonth.minusMonths(1).atDay(1).atStartOfDay(task.getTimezone().toZoneId()).toInstant();
        Instant toTime = currentYearMonth.atDay(1).atStartOfDay(task.getTimezone().toZoneId()).toInstant();

        reportService.generateProvisionOfServiceReport(fromTime, toTime);
    }
}
